document.querySelector('#modalEditarProfesional')?.addEventListener('show.bs.modal', event => {
  const button = event.relatedTarget;
  const form = event.currentTarget.querySelector('form');
  if (!button) {
    if (event.currentTarget.dataset.codigoEdicion) {
      form.action = `/admin/profesionales/${encodeURIComponent(event.currentTarget.dataset.codigoEdicion)}`;
    }
    return;
  }
  form.action = `/admin/profesionales/${encodeURIComponent(button.dataset.codigo)}`;
  form.elements.nombreCompleto.value = button.dataset.nombreCompleto;
  form.elements.telefono.value = button.dataset.telefono;
  form.elements.zonaCobertura.value = button.dataset.zonaCobertura;
  form.elements.tipoProfesional.value = button.dataset.tipo;
  form.elements.especialidad.value = button.dataset.especialidad;
  if (form.elements.disponible.type === 'checkbox') {
    form.elements.disponible.checked = button.dataset.disponible === 'true';
  } else {
    form.elements.disponible.value = button.dataset.disponible;
  }
});

document.querySelector('#modalSolicitudDetalle')?.addEventListener('show.bs.modal', event => {
  const source = event.relatedTarget;
  const modal = event.currentTarget;
  if (!source) return;
  for (const field of ['codigo', 'contacto', 'telefono', 'paciente', 'edad', 'servicio',
    'distrito', 'fecha', 'turno', 'descripcion', 'estado', 'profesional']) {
    modal.querySelector(`[data-detail="${field}"]`).textContent = source.dataset[field] || 'No indicado';
  }
  const codigo = encodeURIComponent(source.dataset.codigo);
  modal.querySelector('[data-form="asignar"]').action = `/admin/solicitudes/${codigo}/asignar`;
  modal.querySelector('[data-form="estado"]').action = `/admin/solicitudes/${codigo}/estado`;

  const asignacion = modal.querySelector('[data-form="asignar"]');
  asignacion.hidden = source.dataset.estado !== 'EN_EVALUACION';
  asignacion.querySelector('select').value = '';
  const transiciones = {
    PENDIENTE: ['EN_EVALUACION', 'CANCELADA'],
    EN_EVALUACION: ['CANCELADA'],
    ASIGNADA: ['EN_ATENCION', 'CANCELADA'],
    EN_ATENCION: ['FINALIZADA', 'CANCELADA'],
    FINALIZADA: [],
    CANCELADA: []
  };
  const estadoForm = modal.querySelector('[data-form="estado"]');
  const permitidos = transiciones[source.dataset.estado] || [];
  estadoForm.hidden = permitidos.length === 0;
  const selector = estadoForm.querySelector('select');
  selector.value = '';
  for (const option of selector.options) {
    if (option.value) {
      option.hidden = !permitidos.includes(option.value);
      option.disabled = !permitidos.includes(option.value);
    }
  }
});

const modalConError = document.querySelector('[data-error-modal]');
if (modalConError) bootstrap.Modal.getOrCreateInstance(modalConError).show();

for (const modal of document.querySelectorAll('#modalProfesional, #modalEditarProfesional, #modalSolicitudDetalle')) {
  modal.addEventListener('shown.bs.modal', () => {
    (modal.querySelector('[aria-describedby$="Error"]') || modal.querySelector('[data-focus-fallback]') || modal.querySelector('input, select, button:not(.btn-close)'))?.focus();
  });
}
