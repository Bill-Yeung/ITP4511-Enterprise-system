import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider } from './context/AuthProvider';
import { NotificationProvider } from './hooks/useNotifications';
import { ProtectedRoute } from './components/ProtectedRoute';
import AppHeader from './components/AppHeader';
import Sidebar from './components/Sidebar';
import AppFooter from './components/AppFooter';

import DashboardPage from './pages/DashboardPage';
import BookAppointmentPage from './pages/BookAppointmentPage';
import MyAppointmentsPage from './pages/MyAppointmentsPage';
import ChangePasswordPage from './pages/ChangePasswordPage';
import ClinicsPage from './pages/ClinicsPage';
import { QueuePage } from './pages/QueuePage';
import { QueueStatusPage } from './pages/QueueStatusPage';
import { NotificationsPage } from './pages/NotificationsPage';
import { ProfilePage } from './pages/ProfilePage';
import { AuthCallbackPage } from './pages/AuthCallbackPage';

import './App.css';

function App() {
  return (
    <BrowserRouter basename="/react-frontend">
      <AuthProvider>
        <NotificationProvider>
          <div className="app-layout">
            <AppHeader />
            <div className="app-body">
              <Sidebar />
              <main className="app-content">
                <Routes>
                  <Route path="/auth-callback" element={
                    <AuthCallbackPage />
                  } />
                  <Route path="/dashboard" element={
                    <ProtectedRoute><DashboardPage /></ProtectedRoute>
                  } />
                  <Route path="/book" element={
                    <ProtectedRoute><BookAppointmentPage /></ProtectedRoute>
                  } />
                  <Route path="/appointments" element={
                    <ProtectedRoute><MyAppointmentsPage /></ProtectedRoute>
                  } />
                  <Route path="/change-password" element={
                    <ProtectedRoute><ChangePasswordPage /></ProtectedRoute>
                  } />
                  <Route path="/clinics" element={
                    <ProtectedRoute><ClinicsPage /></ProtectedRoute>
                  } />
                  <Route path="/queue" element={
                    <ProtectedRoute><QueuePage /></ProtectedRoute>
                  } />
                  <Route path="/queue-status" element={
                    <ProtectedRoute><QueueStatusPage /></ProtectedRoute>
                  } />
                  <Route path="/notifications" element={
                    <ProtectedRoute><NotificationsPage /></ProtectedRoute>
                  } />
                  <Route path="/profile" element={
                    <ProtectedRoute><ProfilePage /></ProtectedRoute>
                  } />
                  <Route path="/" element={<Navigate to="/dashboard" replace />} />
                  <Route path="*" element={<Navigate to="/dashboard" replace />} />
                </Routes>
              </main>
            </div>
            <AppFooter />
          </div>
          <Toaster position="bottom-center" />
        </NotificationProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
