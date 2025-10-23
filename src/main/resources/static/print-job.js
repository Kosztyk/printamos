(function () {
  const form = document.getElementById('printForm');
  const addOptionBtn = document.getElementById('addOptionBtn');
  const optionInput = document.getElementById('optionInput');
  const optionsList = document.getElementById('optionsList');
  const statusMessage = document.getElementById('statusMessage');
  const submitBtn = document.getElementById('submitBtn');
  const submitSpinner = document.getElementById('submitSpinner');
  const submitText = document.getElementById('submitText');
  const statusBadge = document.getElementById('statusBadge');
  const resetBtn = document.getElementById('resetBtn');
  const printerSelect = document.getElementById('printerSelect');
  let options = [];

  function renderOptions() {
    optionsList.innerHTML = '';
    options.forEach((opt, idx) => {
      const badge = document.createElement('div');
      badge.className = 'badge bg-secondary d-inline-flex align-items-center gap-2 p-2';
      badge.setAttribute('data-idx', idx);

      const span = document.createElement('span');
      span.textContent = opt;
      span.className = 'me-2';

      const removeBtn = document.createElement('button');
      removeBtn.type = 'button';
      removeBtn.className = 'btn-close btn-close-white btn-sm';
      removeBtn.setAttribute('aria-label', 'Remove option');
      removeBtn.addEventListener('click', () => {
        options.splice(idx, 1);
        renderOptions();
      });

      badge.appendChild(span);
      badge.appendChild(removeBtn);
      optionsList.appendChild(badge);
    });
  }

  addOptionBtn.addEventListener('click', () => {
    const val = optionInput.value.trim();
    if (!val) return;
    if (val.length > 256) {
      alert('Option too long (max 256 characters).');
      return;
    }
    options.push(val);
    optionInput.value = '';
    optionInput.focus();
    renderOptions();
  });

  optionInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      addOptionBtn.click();
    }
  });

  resetBtn.addEventListener('click', () => {
    form.reset();
    options = [];
    renderOptions();
    statusMessage.textContent = 'Idle';
    statusBadge.innerHTML = '';
  });

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    statusBadge.innerHTML = '';

    if (!form.checkValidity()) {
      form.classList.add('was-validated');
      statusMessage.textContent = 'Fix validation errors above.';
      return;
    }

    const selectedPrinter = printerSelect.value;
    if (!selectedPrinter) {
      showStatus('Please select a printer.', 'text-warning');
      return;
    }

    const fileInput = document.getElementById('fileInput');
    const file = fileInput.files && fileInput.files[0];
    if (!file) {
      statusMessage.textContent = 'Please choose a file to upload.';
      return;
    }
    const allowed = ['application/pdf', 'image/jpeg'];
    if (!allowed.includes(file.type)) {
      const name = file.name.toLowerCase();
      if (!(name.endsWith('.pdf') || name.endsWith('.jpg') || name.endsWith('.jpeg'))) {
        statusMessage.textContent = 'Invalid file type. Use PDF or JPG.';
        return;
      }
    }

    const formData = new FormData();
    formData.append('printer_name', selectedPrinter);
    formData.append('file', file);
    formData.append('copies', document.getElementById('copies').value || '1');
    options.forEach(opt => formData.append('options', opt));

    submitSpinner.classList.remove('visually-hidden');
    submitText.textContent = ' Sending...';
    submitBtn.disabled = true;
    statusMessage.textContent = 'Sending print job...';

    try {
      const resp = await fetch('/api/v1/print-job', {
        method: 'POST',
        body: formData
      });

      const text = await resp.text().catch(() => '');
      if (resp.ok) {
        statusBadge.innerHTML = '<span class="badge bg-success">Success</span>';
        statusMessage.textContent = text || 'Print job sent successfully.';
      } else if (resp.status === 400) {
        statusBadge.innerHTML = '<span class="badge bg-warning text-dark">Bad Request</span>';
        statusMessage.textContent = text || 'Invalid input (400).';
      } else {
        statusBadge.innerHTML = '<span class="badge bg-danger">Error</span>';
        statusMessage.textContent = text || `Server error (${resp.status}).`;
      }
    } catch (err) {
      statusBadge.innerHTML = '<span class="badge bg-danger">Network</span>';
      statusMessage.textContent = 'Network or connection error: ' + (err?.message || String(err));
    } finally {
      submitSpinner.classList.add('visually-hidden');
      submitText.textContent = 'Send to Printer';
      submitBtn.disabled = false;
    }
  });

  // Disable submit if no printers
  document.addEventListener('DOMContentLoaded', () => {
    if (printerSelect && printerSelect.options.length === 0) {
      submitBtn.disabled = true;
      showStatus('No printers available.', 'text-warning');
    }
  });

  // Drag-and-drop support
  (function addDragDrop() {
    const fileEl = document.getElementById('fileInput');
    const parent = fileEl.closest('.mb-3');
    if (!parent) return;

    ['dragenter', 'dragover'].forEach(evt => {
      parent.addEventListener(evt, (e) => {
        e.preventDefault();
        e.stopPropagation();
        parent.classList.add('border-primary');
      });
    });
    ['dragleave', 'drop'].forEach(evt => {
      parent.addEventListener(evt, (e) => {
        e.preventDefault();
        e.stopPropagation();
        parent.classList.remove('border-primary');
      });
    });
    parent.addEventListener('drop', (e) => {
      const dt = e.dataTransfer;
      if (dt?.files?.length) {
        fileEl.files = dt.files;
      }
    });
  })();
})();
