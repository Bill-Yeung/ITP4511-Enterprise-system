(function () {

  function ensureModal() {

    var modal = document.getElementById('confirmActionModal');
    if (modal) {
      return modal;
    }

    var wrapper = document.createElement('div');
    wrapper.innerHTML =
      '<div id="confirmActionModal" class="modal-overlay is-hidden" data-confirm-modal>' +
      '  <div class="modal">' +
      '    <h3 id="confirmActionTitle"></h3>' +
      '    <p id="confirmActionMessage"></p>' +
      '    <div class="form-actions form-actions--right">' +
      '      <button type="button" class="btn btn-outline" data-confirm-close>Go Back</button>' +
      '      <button type="button" id="confirmActionButton" class="btn btn-primary">Confirm</button>' +
      '    </div>' +
      '  </div>' +
      '</div>';

    document.body.appendChild(wrapper.firstElementChild);
    return document.getElementById('confirmActionModal');

  }

  function openModal(trigger) {

    var modal = ensureModal();
    var confirmButton = document.getElementById('confirmActionButton');

    document.getElementById('confirmActionTitle').textContent = trigger.getAttribute('data-confirm-title') || 'Confirm Action';
    document.getElementById('confirmActionMessage').textContent = trigger.getAttribute('data-confirm-message') || 'Continue with this action?';
    confirmButton.className = trigger.getAttribute('data-confirm-danger') === 'true'
      ? 'btn btn-danger'
      : 'btn btn-primary';

    confirmButton.onclick = function () {
      var fieldId = trigger.getAttribute('data-confirm-field');
      if (fieldId && document.getElementById(fieldId)) {
        document.getElementById(fieldId).value = trigger.getAttribute('data-confirm-value') || '';
      }

      var formId = trigger.getAttribute('data-confirm-submit');

      if (formId && document.getElementById(formId)) {
        document.getElementById(formId).submit();
      }

    };

    modal.classList.remove('is-hidden');

  }

  function closeModal() {

    var modal = document.getElementById('confirmActionModal');
    if (modal) {
      modal.classList.add('is-hidden');
    }

  }

  document.addEventListener('click', function (event) {
    
    var trigger = event.target.closest('[data-confirm-submit]');
    if (trigger) {
      event.preventDefault();
      openModal(trigger);
      return;
    }

    if (event.target.matches('[data-confirm-close]') || event.target.matches('[data-confirm-modal]')) {
      closeModal();
    }

  });

})();
