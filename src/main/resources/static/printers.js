(function () {
  const printerSelect = document.getElementById('printerSelect');

  // Render printer list
  function renderPrinters(printers) {
    // Populate the select in Upload & Print
    if (printerSelect) {
      printerSelect.innerHTML = '<option value="" disabled selected>Select a printer...</option>';
      printers.forEach((printer) => {
        const opt = document.createElement('option');
        opt.value = printer.name;
        opt.textContent = `${printer.name}`;
        printerSelect.appendChild(opt);
      });
    }
  }

  // Load printers
  async function loadPrinters() {
    console.info('Loading printers...');
    try {
      const resp = await fetch('/api/v1/printers');
      if (!resp.ok) {
        const errorMsg = `Error (${resp.statusText})`;
        console.error(errorMsg);
        printerSelect.innerHTML = `<option value="" disabled selected>${errorMsg}</option>`;
        return;
      }
      const data = await resp.json();
      renderPrinters(data);
      console.info('Printers loaded successfully.');
    } catch (err) {
      console.error('Failed to load printers: ' + err.message);
    }
  }

  // Initialize printer list on page load
  document.addEventListener('DOMContentLoaded', loadPrinters);

})();
