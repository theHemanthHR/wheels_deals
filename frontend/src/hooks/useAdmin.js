import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api, extractError } from '../api/client';
import { toast } from 'react-toastify';

export function useAdminUsers(q = '', page = 0, size = 20) {
  return useQuery({
    queryKey: ['admin', 'users', q, page],
    queryFn: async () => (await api.get('/admin/users', { params: { q: q || undefined, page, size } })).data,
  });
}

export function useSetUserEnabled() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, enabled }) => (await api.patch(`/admin/users/${id}/enabled`, { enabled })).data,
    onSuccess: () => { toast.success('User updated'); qc.invalidateQueries({ queryKey: ['admin', 'users'] }); },
    onError: (e) => toast.error(extractError(e)),
  });
}

export function useAdminVehicles(page = 0, size = 20) {
  return useQuery({
    queryKey: ['admin', 'vehicles', page],
    queryFn: async () => (await api.get('/admin/vehicles', { params: { page, size } })).data,
  });
}

export function useAdminRemoveVehicle() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, reason }) => { await api.delete(`/admin/vehicles/${id}`, { data: { reason } }); },
    onSuccess: () => {
      toast.success('Vehicle removed');
      qc.invalidateQueries({ queryKey: ['admin', 'vehicles'] });
      qc.invalidateQueries({ queryKey: ['vehicles'] });
    },
    onError: (e) => toast.error(extractError(e)),
  });
}

export function useAdminReports(status, page = 0, size = 20) {
  return useQuery({
    queryKey: ['admin', 'reports', status, page],
    queryFn: async () => (await api.get('/admin/reports', { params: { status: status || undefined, page, size } })).data,
  });
}

export function useResolveReport() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, status, adminNote }) => (await api.patch(`/admin/reports/${id}`, { status, adminNote })).data,
    onSuccess: () => { toast.success('Report updated'); qc.invalidateQueries({ queryKey: ['admin', 'reports'] }); },
    onError: (e) => toast.error(extractError(e)),
  });
}

export function useAuditLogs(page = 0, size = 50) {
  return useQuery({
    queryKey: ['admin', 'audit', page],
    queryFn: async () => (await api.get('/admin/audit-logs', { params: { page, size } })).data,
  });
}
