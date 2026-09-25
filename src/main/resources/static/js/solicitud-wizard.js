document.addEventListener('DOMContentLoaded', () => {
  const form = document.querySelector('#formSolicitud');
  if (!form) return;

  const panels = [...form.querySelectorAll('[data-wizard-panel]')];
  const next = document.querySelector('#wizardNext');
  const back = document.querySelector('#wizardBack');
  const submit = document.querySelector('#wizardSubmit');
  const date = document.querySelector('#fechaRequerida');
  let current = 0;

  function showPanel(index) {
    current = index;
    panels.forEach((panel, position) => { panel.hidden = position !== index; });
    document.querySelector('#wizardStep').textContent = `Paso ${index + 1} de 2`;
    back.hidden = index === 0;
    next.hidden = index === 1;
    submit.hidden = index === 0;
  }

  function firstStepIsValid() {
    return ['nombreContacto', 'telefonoContacto', 'nombrePaciente', 'edadPaciente', 'tipoServicio']
      .map(id => document.getElementById(id))
      .every(field => field.reportValidity());
  }

  if (date) {
    const now = new Date();
    const localDate = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
    date.min = localDate.toISOString().slice(0, 10);
  }

  next.addEventListener('click', () => { if (firstStepIsValid()) showPanel(1); });
  back.addEventListener('click', () => showPanel(0));
  form.addEventListener('submit', event => {
    if (!firstStepIsValid()) { event.preventDefault(); showPanel(0); }
  });

  showPanel(0);
  if (document.querySelector('#modalConfirmacion')) {
    bootstrap.Modal.getOrCreateInstance(document.querySelector('#modalConfirmacion')).show();
  } else if (document.querySelector('#modalSolicitud .alert-danger')) {
    bootstrap.Modal.getOrCreateInstance(document.querySelector('#modalSolicitud')).show();
  }
});
