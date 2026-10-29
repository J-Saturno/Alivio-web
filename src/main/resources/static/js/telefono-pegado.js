// Limpia el celular antes de que maxlength recorte un número pegado con espacios.
document.addEventListener('paste', (event) => {
  const campo = event.target;
  if (!(campo instanceof HTMLInputElement) || campo.type !== 'tel') return;
  const texto = event.clipboardData?.getData('text/plain');
  if (!texto) return;
  const limpio = texto.replace(/[\s\u200b-\u200d\u2060\ufeff]/gu, '');
  if (limpio === texto || !/^9[0-9]{8}$/.test(limpio)) return;
  event.preventDefault();
  campo.setRangeText(limpio, campo.selectionStart, campo.selectionEnd, 'end');
  campo.dispatchEvent(new Event('input', { bubbles: true }));
});
