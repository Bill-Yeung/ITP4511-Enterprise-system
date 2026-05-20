import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import { queueService } from '../services/queueService';
import { QueueLiveStatus } from '../components/QueueLiveStatus';
import { formatTicket } from '../utils/clinicCode';
import '../styles/QueueStatusPage.css';

export function QueueStatusPage() {
  const [myTickets, setMyTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedTicket, setSelectedTicket] = useState(null);

  useEffect(() => {
    let cancelled = false;

    const loadMyTickets = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await queueService.getMyTickets();
        if (cancelled) return;
        setMyTickets(data);
        if (data.length > 0) {
          setSelectedTicket((current) => current || data[0]);
        }
      } catch {
        if (!cancelled) setError('Failed to load your tickets');
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    loadMyTickets();

    return () => {
      cancelled = true;
    };
  }, []);

  const activeTickets = myTickets.filter((t) =>
    ['WAITING', 'CALLED'].includes(t.status)
  );

  const completedTickets = myTickets.filter((t) =>
    ['SERVED', 'SKIPPED', 'CANCELLED'].includes(t.status)
  );

  const reloadTickets = async () => {
    try {
      const data = await queueService.getMyTickets();
      setMyTickets(data);
    } catch {
      // ignore
    }
  };

  const handleRejoin = async (ticket) => {
    try {
      const t = await queueService.joinQueue(ticket.clinic_id, ticket.service_id);
      toast.success(`Re-joined queue! Your new ticket: ${formatTicket(t.clinicName, t.queueNumber)}`);
      await reloadTickets();
    } catch (err) {
      const status = err?.response?.status;
      if (status === 409) {
        toast.error('You already have an active ticket for this service.');
        await reloadTickets();
      } else {
        toast.error('Failed to re-join: ' + err.message);
      }
    }
  };

  return (
    <div className="queue-status-page">
      <div className="status-container">
        <h1>My Queue Status</h1>

        {loading && <div className="loading">Loading your queue information...</div>}

        {error && <div className="error-message">{error}</div>}

        {!loading && myTickets.length === 0 && (
          <div className="no-tickets">
            <p>You don't have any queue tickets today.</p>
            <p>
              <Link to="/queue">Join a queue</Link>
            </p>
          </div>
        )}

        {!loading && myTickets.length > 0 && (
          <div className="status-layout">
            <div className="tickets-sidebar">
              <h3>Your Tickets ({myTickets.length})</h3>

              {activeTickets.length > 0 && (
                <div className="ticket-section">
                  <h4>Active</h4>
                  {activeTickets.map((ticket) => (
                    <div
                      key={ticket.ticket_id}
                      className={`ticket-item ${
                        selectedTicket?.ticket_id === ticket.ticket_id
                          ? 'selected'
                          : ''
                      }`}
                      onClick={() => setSelectedTicket(ticket)}
                    >
                      <div className="ticket-number">{formatTicket(ticket.clinicName, ticket.queue_number)}</div>
                      <div className="ticket-info">
                        <div>{ticket.clinicName}</div>
                        <div className="small">{ticket.serviceName}</div>
                      </div>
                      <div className={`ticket-status status-${ticket.status.toLowerCase()}`}>
                        {ticket.status}
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {completedTickets.length > 0 && (
                <div className="ticket-section">
                  <h4>History</h4>
                  {completedTickets.map((ticket) => (
                    <div
                      key={ticket.ticket_id}
                      className="ticket-item completed"
                    >
                      <div className="ticket-number">{formatTicket(ticket.clinicName, ticket.queue_number)}</div>
                      <div className="ticket-info">
                        <div>{ticket.clinicName}</div>
                        <div className={`ticket-status status-${ticket.status.toLowerCase()}`}>
                          {ticket.status}
                        </div>
                      </div>
                      {ticket.status === 'SKIPPED' && !activeTickets.some(
                        (a) => a.clinic_id === ticket.clinic_id && a.service_id === ticket.service_id
                      ) && (
                        <button
                          className="rejoin-btn"
                          onClick={(e) => { e.stopPropagation(); handleRejoin(ticket); }}
                        >
                          Re-join Queue
                        </button>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="status-main">
              {selectedTicket && (
                <>
                  <h2>
                    {selectedTicket.clinicName} - {selectedTicket.serviceName}
                  </h2>
                  <QueueLiveStatus
                    key={selectedTicket.ticket_id}
                    clinicId={selectedTicket.clinic_id}
                    serviceId={selectedTicket.service_id}
                    clinicName={selectedTicket.clinicName}
                  />
                </>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
