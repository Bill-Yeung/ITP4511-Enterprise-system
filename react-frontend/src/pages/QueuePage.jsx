import { useState } from 'react';
import { Link } from 'react-router-dom';
import { QueueJoinForm } from '../components/QueueJoinForm';
import toast from 'react-hot-toast';
import { formatTicket } from '../utils/clinicCode';
import '../styles/QueuePage.css';

export function QueuePage() {
  const [joinedTicket, setJoinedTicket] = useState(null);

  const handleJoinSuccess = (ticket) => {
    setJoinedTicket(ticket);
    toast.success(`Successfully joined queue! Your ticket: ${formatTicket(ticket.clinicName, ticket.queueNumber)}`);
  };

  return (
    <div className="queue-page">
      <div className="queue-container">
        <h1>Walk-in Queue Management</h1>

        {joinedTicket ? (
          <div className="success-card">
            <h2>Successfully Joined Queue</h2>
            <div className="ticket-details">
              <div className="detail-row">
                <span className="label">Ticket Number:</span>
                <span className="value">{formatTicket(joinedTicket.clinicName, joinedTicket.queueNumber)}</span>
              </div>
              <div className="detail-row">
                <span className="label">Clinic:</span>
                <span className="value">{joinedTicket.clinicName}</span>
              </div>
              <div className="detail-row">
                <span className="label">Service:</span>
                <span className="value">{joinedTicket.serviceName}</span>
              </div>
              <div className="detail-row">
                <span className="label">Estimated Wait:</span>
                <span className="value">{joinedTicket.estimatedWaitMinutes} minutes</span>
              </div>
              <div className="detail-row">
                <span className="label">Joined At:</span>
                <span className="value">{new Date().toLocaleTimeString()}</span>
              </div>
            </div>

            <div className="next-steps">
              <p>
                Please wait at the clinic. Your number will be called when it is your turn.
              </p>
              <p>
                You can <Link to="/queue-status">view your queue status</Link> in real time.
              </p>
            </div>

            <button
              onClick={() => setJoinedTicket(null)}
              className="join-another-btn"
            >
              Join Another Queue
            </button>
          </div>
        ) : (
          <QueueJoinForm onJoinSuccess={handleJoinSuccess} />
        )}
      </div>
    </div>
  );
}
