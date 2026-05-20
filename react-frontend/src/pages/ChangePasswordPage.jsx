import { useState } from 'react';
import { Link } from 'react-router-dom';
import { patientService } from '../services/patientService';
import toast from 'react-hot-toast';

export default function ChangePasswordPage() {
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [saving, setSaving] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);

    try {
      const data = await patientService.changePassword(currentPassword, newPassword, confirmPassword);
      if (data?.success) {
        toast.success('Password changed successfully.');
        setCurrentPassword('');
        setNewPassword('');
        setConfirmPassword('');
      } else {
        toast.error(data?.error || 'Password change failed.');
      }
    } catch {
      toast.error('Unable to connect to server.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <div className="page-header">
        <h2>Change Password</h2>
        <Link to="/profile" className="btn btn-outline">&larr; Back to Profile</Link>
      </div>

      <div className="card card--narrow">
        <div className="card-header">Update Your Password</div>
        <div className="card-body">
          <form onSubmit={handleSubmit} autoComplete="off">
            <div className="form-group">
              <label htmlFor="currentPassword">Current Password <span className="req">*</span></label>
              <input type="password" id="currentPassword" required autoComplete="current-password"
                value={currentPassword} onChange={e => setCurrentPassword(e.target.value)} />
            </div>

            <div className="form-divider"></div>

            <div className="form-group">
              <label htmlFor="newPassword">New Password <span className="req">*</span></label>
              <input type="password" id="newPassword" required minLength={8} autoComplete="new-password"
                placeholder="min. 8 characters"
                value={newPassword} onChange={e => setNewPassword(e.target.value)} />
            </div>
            <div className="form-group">
              <label htmlFor="confirmPassword">Confirm New Password <span className="req">*</span></label>
              <input type="password" id="confirmPassword" required minLength={8} autoComplete="new-password"
                placeholder="repeat new password"
                value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} />
            </div>

            <div className="form-actions form-actions--right">
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? 'Updating...' : 'Update Password'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </>
  );
}
