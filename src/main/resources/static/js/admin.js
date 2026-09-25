document.querySelector('#modalEditarProfesional')?.addEventListener('show.bs.modal', event => {
  const button = event.relatedTarget;
  if (!button) return;
  const form = event.currentTarget.querySelector('form');
  form.action = `/admin/profesionales/${encodeURIComponent(button.dataset.codigo)}`;
  form.elements.nombreCompleto.value = button.dataset.nombreCompleto;
  form.elements.telefono.value = button.dataset.telefono;
  form.elements.zonaCobertura.value = button.dataset.zonaCobertura;
  form.elements.tipoProfesional.value = button.dataset.tipo;
  form.elements.especialidad.value = button.dataset.especialidad;
  form.elements.disponible.checked = button.dataset.disponible === 'true';
});
