import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api, extractError } from '../api/client';
import { toast } from 'react-toastify';

export const vehicleKeys = {
  all: ['vehicles'],
  search: (params) => ['vehicles', 'search', params],
  detail: (id) => ['vehicles', 'detail', id],
  mine: (page = 0) => ['vehicles', 'mine', page],
};

export function useVehicleSearch(params) {
  return useQuery({
    queryKey: vehicleKeys.search(params),
    queryFn: async () => (await api.get('/vehicles', { params })).data,
    keepPreviousData: true,
    staleTime: 30_000,
  });
}

export function useVehicle(id) {
  return useQuery({
    queryKey: vehicleKeys.detail(id),
    queryFn: async () => (await api.get(`/vehicles/${id}`)).data,
    enabled: !!id,
  });
}

export function useMyVehicles(page = 0, size = 12) {
  return useQuery({
    queryKey: vehicleKeys.mine(page),
    queryFn: async () => (await api.get('/vehicles/me', { params: { page, size } })).data,
  });
}

// ───── useCreateVehicle ─────
export function useCreateVehicle() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ data, images }) => {
      const fd = new FormData();
      fd.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }));
      images.forEach((img) => fd.append('images', img));

      // ✅ Let axios set the Content-Type with boundary automatically
      const res = await api.post('/vehicles', fd);
      return res.data;
    },
    onSuccess: () => {
      toast.success('Listing published!');
      qc.invalidateQueries({ queryKey: vehicleKeys.all });
    },
    onError: (e) => toast.error(extractError(e)),
  });
}

export function useUpdateVehicle(id) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (data) => (await api.put(`/vehicles/${id}`, data)).data,
    onSuccess: () => {
      toast.success('Listing updated');
      qc.invalidateQueries({ queryKey: vehicleKeys.detail(id) });
      qc.invalidateQueries({ queryKey: vehicleKeys.all });
    },
    onError: (e) => toast.error(extractError(e)),
  });
}

// ───── useReplaceImages ─────
export function useReplaceImages(id) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (images) => {
      const fd = new FormData();
      images.forEach((img) => fd.append('images', img));
      // ✅ No Content-Type header
      const res = await api.put(`/vehicles/${id}/images`, fd);
      return res.data;
    },
    onSuccess: () => {
      toast.success('Images updated');
      qc.invalidateQueries({ queryKey: vehicleKeys.detail(id) });
    },
    onError: (e) => toast.error(extractError(e)),
  });
}

export function useMarkSold() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, note }) => (await api.patch(`/vehicles/${id}/sold`, { note })).data,
    onSuccess: () => { toast.success('Marked as sold'); qc.invalidateQueries({ queryKey: vehicleKeys.all }); },
    onError: (e) => toast.error(extractError(e)),
  });
}

export function useReactivateVehicle() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (id) => (await api.patch(`/vehicles/${id}/reactivate`)).data,
    onSuccess: () => { toast.success('Listing reactivated'); qc.invalidateQueries({ queryKey: vehicleKeys.all }); },
    onError: (e) => toast.error(extractError(e)),
  });
}

export function useDeleteVehicle() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (id) => { await api.delete(`/vehicles/${id}`); },
    onSuccess: () => { toast.success('Listing removed'); qc.invalidateQueries({ queryKey: vehicleKeys.all }); },
    onError: (e) => toast.error(extractError(e)),
  });
}
