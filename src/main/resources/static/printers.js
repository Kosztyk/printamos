(function () {
  const addPrinterForm = document.getElementById('addPrinterForm');
  const printerStatusMessage = document.getElementById('printerStatusMessage');
  const printersList = document.getElementById('printersList');
  const printerSelect = document.getElementById('printerSelect');
  const showOptionsLink = document.getElementById('showOptionsLink');
  const showOptionsSpinner = document.getElementById('showOptionsSpinner');
  const listOptionsBox = document.getElementById('listOptionsBox');
  const listOptionsCollapse = document.getElementById('listOptionsCollapse');

  function showStatus(message, color = 'text-light') {
    printerStatusMessage.textContent = message;
    printerStatusMessage.className = color;
  }

  // Helper: send form-urlencoded
  async function sendForm(url, method, data) {
    const formBody = new URLSearchParams(data);
    const resp = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: formBody
    });
    const text = await resp.text().catch(() => '');
    return { ok: resp.ok, status: resp.status, text };
  }

  // Render printer list
  function renderPrinters(printers) {
    printersList.innerHTML = '';
    if (printerSelect) printerSelect.innerHTML = '';

    if (!Array.isArray(printers) || printers.length === 0) {
      printersList.innerHTML = '<li class="list-group-item text-muted bg-dark-subtle">No printers found</li>';
      if (printerSelect) {
        printerSelect.innerHTML = '<option value="" disabled selected>No printers available</option>';
      }
      return;
    }

    printers.forEach((printer) => {
      const li = document.createElement('li');
      li.className = 'list-group-item d-flex justify-content-between align-items-center bg-dark-subtle text-light';

      const info = document.createElement('div');
      info.innerHTML = `<strong>${printer.name}</strong><br><span class="text-muted small">${printer.description || ''}</span>`;

      const delBtn = document.createElement('button');
      delBtn.className = 'btn btn-outline-danger btn-sm';
      delBtn.textContent = 'Delete';
      delBtn.addEventListener('click', () => removePrinter(printer.name));

      li.appendChild(info);
      li.appendChild(delBtn);
      printersList.appendChild(li);
    });

    // Populate the select in Upload & Print
    if (printerSelect) {
      printerSelect.innerHTML = '<option value="" disabled selected>Select a printer...</option>';
      printers.forEach((printer) => {
        const opt = document.createElement('option');
        opt.value = printer.name;
        opt.textContent = `${printer.name} – ${printer.description || ''}`;
        printerSelect.appendChild(opt);
      });
      // Update Show Options button state after populating
      setShowOptionsDisabledState();
    }
  }

  // Show/hide Show Options link depending on selection
  function setShowOptionsDisabledState() {
    if (!showOptionsLink || !printerSelect) return;
    if (printerSelect.value) {
      // show link
      showOptionsLink.classList.remove('d-none');
      showOptionsLink.removeAttribute('aria-disabled');
      showOptionsLink.classList.remove('disabled');
    } else {
      // hide link
      showOptionsLink.classList.add('d-none');
      showOptionsLink.setAttribute('aria-disabled', 'true');
    }
  }

  // Load printers
  async function loadPrinters() {
    showStatus('Loading printers...');
    try {
      const resp = await fetch('/api/v1/printers');
      if (!resp.ok) {
        showStatus(`Error (${resp.status})`, 'text-danger');
        return;
      }
      const data = await resp.json();
      renderPrinters(data);
      showStatus('Printers loaded.', 'text-success');
    } catch (err) {
      showStatus('Failed to load printers: ' + err.message, 'text-danger');
    }
  }

  // Remove printer
  async function removePrinter(printerName) {
    if (!confirm(`Remove printer "${printerName}"?`)) return;
    showStatus(`Removing ${printerName}...`);
    try {
      const res = await sendForm('/api/v1/remove-printer', 'DELETE', { printer_name: printerName });
      if (res.ok) {
        showStatus(`Removed ${printerName}`, 'text-success');
        await loadPrinters();
      } else {
        showStatus(`Error removing (${res.status})`, 'text-danger');
      }
    } catch (err) {
      showStatus('Network error: ' + err.message, 'text-danger');
    }
  }

  // Add printer
  addPrinterForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!addPrinterForm.checkValidity()) {
      addPrinterForm.classList.add('was-validated');
      showStatus('Fix form errors.', 'text-warning');
      return;
    }

    const data = {
      printer_name: document.getElementById('addPrinterName').value.trim(),
      printer_uri: document.getElementById('addPrinterUri').value.trim(),
      printer_model: document.getElementById('addPrinterModel').value.trim()
    };

    showStatus('Adding printer...');
    try {
      const res = await sendForm('/api/v1/add-printer', 'POST', data);
      if (res.ok) {
        showStatus('Printer added successfully.', 'text-success');
        addPrinterForm.reset();
        await loadPrinters();
      } else {
        showStatus(`Error (${res.status})`, 'text-danger');
      }
    } catch (err) {
      showStatus('Network error: ' + err.message, 'text-danger');
    }
  });

  // Initialize printer list on page load
  document.addEventListener('DOMContentLoaded', loadPrinters);

  // Ensure Show Options button state updates when selection changes
  if (printerSelect) {
    printerSelect.addEventListener('change', () => {
      setShowOptionsDisabledState();
      // hide options collapse if no selection
      if (!printerSelect.value && listOptionsCollapse) {
        const existing = bootstrap.Collapse.getInstance(listOptionsCollapse) || new bootstrap.Collapse(listOptionsCollapse, { toggle: false });
        existing.hide();
      }
    });
  }

  // Show Options link click handler
  if (showOptionsLink) {
    showOptionsLink.addEventListener('click', async (e) => {
      e.preventDefault();
      if (!printerSelect || !printerSelect.value) return;
      showStatus('Listing printer options...');
      // visually disable link while loading
      showOptionsLink.classList.add('disabled');
      showOptionsLink.setAttribute('aria-disabled', 'true');
      if (showOptionsSpinner) showOptionsSpinner.classList.remove('visually-hidden');
      try {
        const res = await sendForm('/api/v1/list-options', 'POST', { printer_name: printerSelect.value });
        if (res.ok) {
          let display = res.text || '';
          try {
            const parsed = JSON.parse(display);
            display = JSON.stringify(parsed, null, 2);
          } catch (e) {
            // not JSON; keep as-is
          }

          if (listOptionsBox) listOptionsBox.textContent = display || 'No options returned.';
          // show collapse
          if (listOptionsCollapse) {
            const bsCol = bootstrap.Collapse.getInstance(listOptionsCollapse) || new bootstrap.Collapse(listOptionsCollapse, { toggle: false });
            bsCol.show();
          }
          showStatus('Options loaded.', 'text-success');
        } else {
          if (listOptionsBox) listOptionsBox.textContent = `Error loading options (${res.status})`;
          showStatus('Error loading options.', 'text-danger');
        }
      } catch (err) {
        if (listOptionsBox) listOptionsBox.textContent = 'Network error: ' + err.message;
        showStatus('Network error: ' + err.message, 'text-danger');
      } finally {
        if (showOptionsSpinner) showOptionsSpinner.classList.add('visually-hidden');
        showOptionsLink.classList.remove('disabled');
        showOptionsLink.removeAttribute('aria-disabled');
      }
    });
  }
})();
