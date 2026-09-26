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
