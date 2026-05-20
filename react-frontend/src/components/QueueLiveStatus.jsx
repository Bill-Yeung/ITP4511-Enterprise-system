import { useQueue } from '../hooks/useQueue';
import { queueService } from '../services/queueService';
import { useState, useEffect, useRef } from 'react';
import toast from 'react-hot-toast';
import { formatTicket } from '../utils/clinicCode';
import '../styles/QueueLiveStatus.css';

export function QueueLiveStatus({ clinicId, serviceId, clinicName }) {
  const { status, loading, error } = useQueue(clinicId, serviceId, 10000);
  const [myTickets, setMyTickets] = useState([]);
  const lastStatusRef = useRef(null);

  const loadMyTickets = async () => {
    try {
      const data = await queueService.getMyTickets();
      setMyTickets(data);
    } catch (err) {
      console.error('Error loading tickets:', err);
    }
  };

  useEffect(() => {
    loadMyTickets();
  }, []);

  useEffect(() => {
    if (status) loadMyTickets();
  }, [status]);

  useEffect(() => {
    const lastStatus = lastStatusRef.current;
    if (status?.patientStatus === 'CALLED' && lastStatus?.patientStatus !== 'CALLED') {
      toast.success("It's your turn. Please proceed to the counter.", {
        duration: 5000,
        position: 'bottom-center',
      });
    }
    lastStatusRef.current = status;
  }, [status]);

  const [cancelTicketId, setCancelTicketId] = useState(null);

  const handleCancelTicket = async () => {
    if (!cancelTicketId) return;
    try {
      await queueService.cancelTicket(cancelTicketId);
      toast.success('Ticket cancelled');
      setCancelTicketId(null);
      await loadMyTickets();
    } catch (err) {
      toast.error('Failed to cancel ticket: ' + err.message);
    }
  };

  if (loading && !status) {
    return <div className="loading">Loading queue status...</div>;
  }

  if (error && !status) {
    return <div className="error">Error loading queue status: {error}</div>;
  }

  return (
    <div className="queue-status">
      <h2>Queue Status</h2>

      {status && (
        <div className="status-info">
          <div className="status-card current">
            <div className="status-label">Currently Serving</div>
            <div className="status-number">
              {status.currentNumber > 0 ? formatTicket(clinicName, status.currentNumber) : '--'}
            </div>
          </div>

          {status.patientPosition !== undefined && (
            <div className="status-card position">
              <div className="status-label">Your Position</div>
              <div className="status-number">#{status.patientPosition}</div>
              <div className="status-detail">
                Est. Wait: {status.estimatedWaitMinutes || 0} min
              </div>
            </div>
          )}

          <div className="status-card waiting">
            <div className="status-label">Total Waiting</div>
            <div className="status-number">{status.totalWaiting || 0}</div>
          </div>
        </div>
      )}

      {status?.patientStatus === 'CALLED' && (
        <div className="alert alert-success">
          <strong>It's your turn.</strong> Please proceed to the counter immediately.
        </div>
      )}

      {status?.patientStatus === 'SERVED' && (
        <div className="alert alert-info">
          Your visit has been completed. Thank you for using our service.
        </div>
      )}

      {status?.patientStatus === 'SKIPPED' && (
        <div className="alert alert-warning">
          <div>Your visit was skipped.</div>
          <button
            className="rejoin-btn"
            onClick={async () => {
              try {
                const t = await queueService.joinQueue(clinicId, serviceId);
                toast.success(`Re-joined queue! Your new ticket: ${formatTicket(t.clinicName, t.queueNumber)}`);
                await loadMyTickets();
              } catch (err) {
                toast.error('Failed to re-join: ' + err.message);
              }
            }}
          >
            Re-join Queue
          </button>
        </div>
      )}

      {myTickets.length > 0 && (
        <div className="my-tickets">
          <h3>My Queue Tickets (Today)</h3>
          {myTickets.map((ticket) => (
            <div key={ticket.ticket_id} className="ticket-item">
              <div className="ticket-info">
                <span className="ticket-number">Ticket {formatTicket(ticket.clinicName, ticket.queue_number)}</span>
                <span className="ticket-clinic">{ticket.clinicName}</span>
                <span className="ticket-service">{ticket.serviceName}</span>
                <span className={`ticket-status status-${ticket.status.toLowerCase()}`}>
                  {ticket.status}
                </span>
              </div>
              {ticket.status === 'WAITING' && (
                <button
                  className="cancel-btn"
                  onClick={() => setCancelTicketId(ticket.ticket_id)}
                >
                  Cancel
                </button>
              )}
            </div>
          ))}
        </div>
      )}

      <div className="last-updated">
        Last updated: {new Date().toLocaleTimeString()}
      </div>

      {cancelTicketId && (
        <div className="modal-overlay" onClick={() => setCancelTicketId(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h3>Cancel Queue Ticket</h3>
            <p>Are you sure you want to cancel your ticket?</p>
            <div className="form-actions form-actions--right">
              <button className="btn btn-outline" onClick={() => setCancelTicketId(null)}>Go Back</button>
              <button className="btn btn-danger" onClick={handleCancelTicket}>Confirm Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
