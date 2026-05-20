import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const JSP_LOGOUT = '/logout';

export function AuthCallbackPage() {

  const navigate = useNavigate();
  const { login } = useAuth();
  const [error, setError] = useState(null);

  useEffect(() => {

    const processCallback = async () => {
      try {

        const params = new URLSearchParams(window.location.search);
        const encoded = params.get('u');

        if (!encoded) {
          console.warn('No user data in callback');
          window.location.href = JSP_LOGOUT;
          return;
        }

        // Convert base64 string back to user data
        const base64 = encoded.replace(/-/g, '+').replace(/_/g, '/');
        const paddedBase64 = base64 + '='.repeat((4 - base64.length % 4) % 4);
        const userJson = atob(paddedBase64);

        const userData = JSON.parse(userJson);

        if (!userData.userId || !userData.username || !userData.role) {
          console.error('Invalid user data from callback:', userData);
          setError('Invalid user data received');
          setTimeout(() => {
            window.location.href = JSP_LOGOUT;
          }, 2000);
          return;
        }

        login(userData);
        await new Promise(resolve => setTimeout(resolve, 100));
        navigate('/dashboard', { replace: true });

      } catch (e) {
        console.error('Auth callback error:', e);
        setError('Failed to process login. Redirecting...');
        setTimeout(() => {
          window.location.href = JSP_LOGOUT;
        }, 2000);
      }
    };

    processCallback();
    
  }, [navigate, login]);

  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      height: '60vh',
      color: '#6c757d',
      fontSize: '15px',
      flexDirection: 'column',
      gap: '10px'
    }}>
      {error ? (
        <>
          <div style={{ color: '#dc3545' }}>{error}</div>
          <div style={{ fontSize: '13px' }}>Redirecting...</div>
        </>
      ) : (
        'Signing in, please wait...'
      )}
    </div>
  );

}
