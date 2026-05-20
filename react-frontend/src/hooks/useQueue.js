import { useState, useEffect, useCallback } from 'react';
import { queueService } from '../services/queueService';

export function useQueue(clinicId, serviceId, pollInterval = 10000) {
  const [status, setStatus] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchStatus = useCallback(async () => {
    if (!clinicId || !serviceId) return;
    setLoading(true);
    setError(null);
    try {
      const data = await queueService.getStatus(clinicId, serviceId);
      setStatus(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [clinicId, serviceId]);

  // Auto-poll for queue status
  useEffect(() => {
    if (!clinicId || !serviceId) return;

    // Fetch immediately on mount
    fetchStatus();

    // Set up polling interval
    const interval = setInterval(fetchStatus, pollInterval);
    return () => clearInterval(interval);
  }, [clinicId, serviceId, pollInterval, fetchStatus]);

  return { status, loading, error, refetch: fetchStatus };
}
