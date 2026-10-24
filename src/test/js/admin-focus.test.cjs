const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const test = require('node:test');

test('detail modal focuses its visible heading when assignment is hidden', () => {
  const listeners = new Map();
  const focused = [];
  const heading = { focus: () => focused.push('heading') };
  const hiddenSelect = { focus: () => focused.push('hidden assignment') };
  const detail = {
    querySelector: selector => ({
      '[aria-describedby$="Error"]': null,
      '[data-focus-fallback]': heading,
      'input, select, button:not(.btn-close)': hiddenSelect,
    })[selector],
    addEventListener: (name, callback) => listeners.set(name, callback),
  };
  const document = { querySelector: () => null, querySelectorAll: () => [detail] };
  vm.runInNewContext(fs.readFileSync('src/main/resources/static/js/admin.js', 'utf8'), { document });
  listeners.get('shown.bs.modal')();
  assert.deepEqual(focused, ['heading']);
});

test('assignment modal shows only candidates returned for the selected request', async () => {
  const listeners = new Map();
  const options = [];
  const select = { length: 1, value: '', appendChild: option => options.push(option) };
  const assignButton = { disabled: true };
  const help = { textContent: '' };
  const assignment = {
    hidden: false,
    querySelector: selector => ({
      select,
      'button[type="submit"]': assignButton,
      '#asignacionAyuda': help,
    })[selector],
  };
  const statusSelect = { value: '', options: [{ value: '' }, { value: 'CANCELADA' }] };
  const status = { querySelector: () => statusSelect };
  const detail = {
    dataset: {},
    addEventListener: (name, callback) => listeners.set(name, callback),
    querySelector: selector => {
      if (selector === '[data-form="asignar"]') return assignment;
      if (selector === '[data-form="estado"]') return status;
      if (selector.startsWith('[data-detail=')) return { textContent: '' };
      return null;
    },
  };
  const document = {
    querySelector: selector => selector === '#modalSolicitudDetalle' ? detail : null,
    querySelectorAll: () => [detail],
    createElement: () => ({}),
  };
  let requestedUrl;
  const fetch = url => {
    requestedUrl = url;
    return Promise.resolve({ ok: true, json: async () => [
      { codigo: 'PRO-001', nombre: 'Ana Torres', zonaCobertura: 'Surco' },
    ] });
  };
  vm.runInNewContext(fs.readFileSync('src/main/resources/static/js/admin.js', 'utf8'), { document, fetch });
  listeners.get('show.bs.modal')({ currentTarget: detail, relatedTarget: { dataset: {
    codigo: 'SOL-001', estado: 'EN_EVALUACION',
  } } });
  await new Promise(resolve => setImmediate(resolve));

  assert.equal(requestedUrl, '/admin/solicitudes/SOL-001/candidatas');
  assert.equal(options.length, 1);
  assert.equal(options[0].value, 'PRO-001');
  assert.equal(assignButton.disabled, false);
  assert.match(help.textContent, /1 profesional/);
});
