const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');

test('pegar celular con espacios conserva los nueve digitos antes del limite HTML', () => {
  let pegar;
  class Campo {
    type = 'tel'; value = ''; selectionStart = 0; selectionEnd = 0;
    setRangeText(valor) { this.value = valor; }
    dispatchEvent() {}
  }
  vm.runInNewContext(fs.readFileSync(path.join(__dirname, '../../main/resources/static/js/telefono-pegado.js'), 'utf8'), {
    document: { addEventListener: (evento, listener) => { assert.equal(evento, 'paste'); pegar = listener; } },
    HTMLInputElement: Campo,
    Event: class {},
  });
  const campo = new Campo();
  let evitado = false;
  pegar({ target: campo, clipboardData: { getData: () => ' 912\u00a0345 678 ' }, preventDefault: () => { evitado = true; } });
  assert.equal(campo.value, '912345678');
  assert.equal(evitado, true);
  campo.value = '';
  evitado = false;
  pegar({ target: campo, clipboardData: { getData: () => 'abc912345678' }, preventDefault: () => { evitado = true; } });
  assert.equal(evitado, false);
  assert.equal(campo.value, '');
});
