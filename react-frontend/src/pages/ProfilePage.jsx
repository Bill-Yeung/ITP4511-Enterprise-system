import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { patientService } from '../services/patientService';
import toast from 'react-hot-toast';

export function ProfilePage() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;

    patientService.getProfile()
      .then((data) => {
        if (!cancelled) setProfile(data);
      })
      .catch(() => {
        if (!cancelled) setError('Failed to load profile.');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);

    const form = new URLSearchParams();
    form.set('fullName', profile.fullName || '');
    form.set('email', profile.email || '');
    form.set('phone', profile.phone || '');
    form.set('idNumber', profile.idNumber || '');
    form.set('dateOfBirth', profile.dateOfBirth || '');
    form.set('gender', profile.gender || '');
    form.set('address', profile.address || '');
    form.set('emergencyContactName', profile.emergencyContactName || '');
    form.set('emergencyContactPhone', profile.emergencyContactPhone || '');

    try {
      const data = await patientService.updateProfile(form);
      if (data?.success) {
        toast.success('Profile updated successfully.');
      } else {
        toast.error(data?.error || 'Update failed.');
      }
    } catch {
      toast.error('Unable to connect to server.');
    } finally {
      setSaving(false);
    }
  };

  const update = (field, value) => setProfile({ ...profile, [field]: value });

  if (loading) return <p>Loading...</p>;

  return (
    <>
      <div className="page-header">
          <h2>My Profile</h2>
          <Link to="/change-password" className="btn btn-outline">Change Password</Link>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="card">
            <div className="card-header">Account Information</div>
            <div className="card-body">
              <div className="form-row">
                <div className="form-group">
                  <label>Username</label>
                  <input type="text" value={profile?.username || ''} disabled className="input-readonly" />
                </div>
                <div className="form-group">
                  <label>Role</label>
                  <input type="text" value="Patient" disabled className="input-readonly" />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Member Since</label>
                  <input type="text" value={profile?.createdAt || ''} disabled className="input-readonly" />
                </div>
              </div>
            </div>
          </div>

          <div className="card">
            <div className="card-header">Personal Details</div>
            <div className="card-body">
              <div className="form-group">
                <label htmlFor="fullName">Full Name <span className="req">*</span></label>
                <input type="text" id="fullName" required maxLength={100}
                  value={profile?.fullName || ''} onChange={e => update('fullName', e.target.value)} />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="email">Email Address <span className="req">*</span></label>
                  <input type="email" id="email" required maxLength={100}
                    value={profile?.email || ''} onChange={e => update('email', e.target.value)} />
                </div>
                <div className="form-group">
                  <label htmlFor="phone">Phone Number</label>
                  <input type="tel" id="phone" maxLength={20}
                    value={profile?.phone || ''} onChange={e => update('phone', e.target.value)} />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="idNumber">HKID / Passport No.</label>
                  <input type="text" id="idNumber" maxLength={20}
                    value={profile?.idNumber || ''} onChange={e => update('idNumber', e.target.value)} />
                </div>
                <div className="form-group">
                  <label htmlFor="dateOfBirth">Date of Birth</label>
                  <input type="date" id="dateOfBirth"
                    value={profile?.dateOfBirth || ''} onChange={e => update('dateOfBirth', e.target.value)} />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="gender">Gender</label>
                  <select id="gender" value={profile?.gender || ''} onChange={e => update('gender', e.target.value)}>
                    <option value="">-- Select --</option>
                    <option value="Male">Male</option>
                    <option value="Female">Female</option>
                    <option value="Other">Other</option>
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="address">Address</label>
                <input type="text" id="address" maxLength={200}
                  value={profile?.address || ''} onChange={e => update('address', e.target.value)} />
              </div>
            </div>
          </div>

          <div className="card">
            <div className="card-header">Emergency Contact</div>
            <div className="card-body">
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="emergencyContactName">Contact Name</label>
                  <input type="text" id="emergencyContactName" maxLength={100}
                    value={profile?.emergencyContactName || ''} onChange={e => update('emergencyContactName', e.target.value)} />
                </div>
                <div className="form-group">
                  <label htmlFor="emergencyContactPhone">Contact Phone</label>
                  <input type="tel" id="emergencyContactPhone" maxLength={20}
                    value={profile?.emergencyContactPhone || ''} onChange={e => update('emergencyContactPhone', e.target.value)} />
                </div>
              </div>
            </div>
          </div>

          <div className="form-actions form-actions--right">
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
    </>
  );
}
