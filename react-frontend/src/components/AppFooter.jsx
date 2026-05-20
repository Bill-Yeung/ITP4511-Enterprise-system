import { Link } from 'react-router-dom';

export default function AppFooter() {

  return (
    <footer className="site-footer">
      <div className="footer-inner">
        <div className="footer-brand">
          <div className="footer-logo">CCHC</div>
          <p className="footer-brand-name">Community Care Health Consortium</p>
          <p className="footer-brand-desc">Quality healthcare services across 5 community clinics in Hong Kong.</p>
        </div>

        <div className="footer-col">
          <h4>Services</h4>
          <ul>
            <li><a href="/home#services">General Consultation</a></li>
            <li><a href="/home#services">Vaccination</a></li>
            <li><a href="/home#services">Health Screening</a></li>
            <li><a href="/home#services">Blood Test</a></li>
          </ul>
        </div>

        <div className="footer-col">
          <h4>Our Clinics</h4>
          <ul>
            <li><a href="/home#clinics">Chai Wan</a></li>
            <li><a href="/home#clinics">Tseung Kwan O</a></li>
            <li><a href="/home#clinics">Sha Tin</a></li>
            <li><a href="/home#clinics">Tuen Mun</a></li>
            <li><a href="/home#clinics">Tsing Yi</a></li>
          </ul>
        </div>

        <div className="footer-col">
          <h4>Quick Links</h4>
          <ul>
            <li><Link to="/dashboard">Dashboard</Link></li>
            <li><Link to="/book">Book Appointment</Link></li>
            <li><Link to="/appointments">My Appointments</Link></li>
            <li><Link to="/profile">Profile</Link></li>
          </ul>
        </div>
      </div>

      <div className="footer-bottom">
        <p>&copy; 2026 Community Care Health Consortium (CCHC). All rights reserved.</p>
        <p>ITP4511 Enterprise System Development &mdash; HKIIT</p>
      </div>
    </footer>
  );
  
}
