// Javascript for pop up modals

(function () {

  var showReason = false;
  var activeModalId = '';

  function openModal(button) {

    var modalId = button.getAttribute('data-modal-id') || 'appointmentActionModal';

    var modal = document.getElementById(modalId);
    if (!modal) {
      return;
    }

    activeModalId = modalId;
    showReason = button.getAttribute('data-show-reason') === 'true';

    // Set the action link
    document.getElementById(modalId + 'Form').action = button.getAttribute('data-url') || '';
    // Set the action type
    document.getElementById(modalId + 'Action').value = button.getAttribute('data-action') || '';

    document.getElementById(modalId + 'AppointmentId').value = button.getAttribute('data-id') || '';
    document.getElementById(modalId + 'Title').textContent = button.getAttribute('data-title') || '';
    document.getElementById(modalId + 'Message').textContent = button.getAttribute('data-message') || '';

    // For rehect and cancel appointments
    var reasonGroup = document.getElementById(modalId + 'ReasonGroup');
    var reasonInput = document.getElementById(modalId + 'Reason');
    reasonGroup.classList.toggle('is-hidden', !showReason);
    if (showReason) {
      reasonInput.value = '';
    }

    var confirmBtn = document.getElementById(modalId + 'ConfirmBtn');
    var title = button.getAttribute('data-title') || '';
    confirmBtn.className = title === 'Cancel Appointment' || title === 'Reject Booking'
      ? 'btn btn-danger'
      : 'btn btn-primary';

    modal.classList.remove('is-hidden');

  }

  function openEditModal(button) {

    var modalId = button.getAttribute('data-edit-modal-id') || 'appointmentActionModalEdit';

    var modal = document.getElementById(modalId);
    if (!modal) {
      return;
    }

    // Set the action link
    document.getElementById(modalId + 'Form').action = button.getAttribute('data-url') || '';

    document.getElementById(modalId + 'AppointmentId').value = button.getAttribute('data-id') || '';
    document.getElementById(modalId + 'ClinicServiceId').value = button.getAttribute('data-clinic-service-id') || '';
    document.getElementById(modalId + 'Date').value = button.getAttribute('data-appointment-date') || '';
    var time = button.getAttribute('data-time-slot') || '';
    document.getElementById(modalId + 'Time').value = time.length >= 5 ? time.substring(0, 5) : time;
    document.getElementById(modalId + 'DoctorId').value = button.getAttribute('data-doctor-id') || '';
    document.getElementById(modalId + 'Status').value = button.getAttribute('data-status') || 'Booked';
    document.getElementById(modalId + 'Remarks').value = button.getAttribute('data-remarks') || '';

    modal.classList.remove('is-hidden');

  }

  // Close both modals
  function closeModal(modal) {
    if (!modal && activeModalId) {
      modal = document.getElementById(activeModalId);
    }
    if (modal) {
      modal.classList.add('is-hidden');
    }
  }

  document.addEventListener('click', function (event) {

    var actionButton = event.target.closest('.js-appointment-action');
    if (actionButton) {
      openModal(actionButton);
      return;
    }

    var editButton = event.target.closest('.js-appointment-edit');
    if (editButton) {
      openEditModal(editButton);
      return;
    }

    // Close buttons

    if (event.target.matches('[data-appointment-action-close]')) {
      closeModal(event.target.closest('[data-appointment-action-modal]'));
      return;
    }

    if (event.target.matches('[data-appointment-edit-close]')) {
      closeModal(event.target.closest('[data-appointment-edit-modal]'));
      return;
    }

    // Click outside

    if (event.target.matches('[data-appointment-action-modal]')) {
      closeModal(event.target);
    }

    if (event.target.matches('[data-appointment-edit-modal]')) {
      closeModal(event.target);
    }

  });

})();
