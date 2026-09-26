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

const modalConError = document.querySelector('[data-error-modal]');
if (modalConError) bootstrap.Modal.getOrCreateInstance(modalConError).show();
