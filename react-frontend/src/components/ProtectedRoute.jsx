import { useEffect } from 'react';
import { useAuth } from '../context/AuthContext';

const JSP_LOGIN = '/login';

export function ProtectedRoute({ children }) {

  const { isAuthenticated, loading, user } = useAuth();
  const storedUser = !isAuthenticated || !user ? localStorage.getItem('user') : null;
  const shouldRedirect = !loading && (!isAuthenticated || !user) && !storedUser;

  useEffect(() => {

    if (shouldRedirect) {
      window.location.href = JSP_LOGIN;
    }

  }, [shouldRedirect]);

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh', color: '#6c757d' }}>
        Loading...
      </div>
    );
  }

  if (shouldRedirect) {
    return null;
  }

  return children;
  
}
