import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api, extractError } from '../api/client';
import { toast } from 'react-toastify';

export function useBuyerThreads(vehicleId) {
  return useQuery({
    queryKey: ['inquiries', 'buyer', vehicleId],
    queryFn: async () => (await api.get(`/vehicles/${vehicleId}/inquiries/mine`)).data,
    enabled: !!vehicleId,
  });
}

export function useSellerThreads(vehicleId) {
  return useQuery({
    queryKey: ['inquiries', 'seller', vehicleId],
    queryFn: async () => (await api.get(`/vehicles/${vehicleId}/inquiries/as-seller`)).data,
    enabled: !!vehicleId,
  });
}

export function usePostInquiry(vehicleId) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ message, parentId }) => (await api.post(`/vehicles/${vehicleId}/inquiries`, { message, parentId })).data,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['inquiries', 'buyer', vehicleId] });
      qc.invalidateQueries({ queryKey: ['inquiries', 'seller', vehicleId] });
      qc.invalidateQueries({ queryKey: ['vehicles', 'detail', vehicleId] });
    },
    onError: (e) => toast.error(extractError(e)),
  });
}

export function useCreateOffer(vehicleId) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (payload) => (await api.post(`/vehicles/${vehicleId}/offers`, payload)).data,
    onSuccess: () => { toast.success('Offer sent to seller'); qc.invalidateQueries({ queryKey: ['offers'] }); },
    onError: (e) => toast.error(extractError(e)),
  });
}

export function useReceivedOffers(page = 0, size = 10) {
  return useQuery({
    queryKey: ['offers', 'received', page],
    queryFn: async () => (await api.get('/offers/received', { params: { page, size } })).data,
  });
}

export function useSentOffers(page = 0, size = 10) {
  return useQuery({
    queryKey: ['offers', 'sent', page],
    queryFn: async () => (await api.get('/offers/sent', { params: { page, size } })).data,
  });
}

export function useOfferDecision() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, action }) => (await api.patch(`/offers/${id}/${action}`)).data,
    onSuccess: (_, vars) => {
      toast.success(`Offer ${vars.action}ed`);
      qc.invalidateQueries({ queryKey: ['offers'] });
      qc.invalidateQueries({ queryKey: ['vehicles'] });
    },
    onError: (e) => toast.error(extractError(e)),
  });
}

export function useWishlist(page = 0, size = 12) {
  return useQuery({
    queryKey: ['wishlist', page],
    queryFn: async () => (await api.get('/wishlist', { params: { page, size } })).data,
  });
}

export function useWishlistToggle() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ vehicleId, add }) => add ? api.post(`/wishlist/${vehicleId}`) : api.delete(`/wishlist/${vehicleId}`),
    onSuccess: (_, vars) => {
      toast.success(vars.add ? 'Added to wishlist' : 'Removed from wishlist');
      qc.invalidateQueries({ queryKey: ['wishlist'] });
      qc.invalidateQueries({ queryKey: ['vehicles'] });
    },
    onError: (e) => toast.error(extractError(e)),
  });
}

export function useUserReviews(userId, page = 0, size = 10) {
  return useQuery({
    queryKey: ['reviews', 'user', userId, page],
    queryFn: async () => (await api.get(`/users/${userId}/reviews`, { params: { page, size } })).data,
    enabled: !!userId,
  });
}

export function useCreateReview(vehicleId) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (payload) => (await api.post(`/vehicles/${vehicleId}/reviews`, payload)).data,
    onSuccess: () => {
      toast.success('Review submitted');
      qc.invalidateQueries({ queryKey: ['reviews'] });
      qc.invalidateQueries({ queryKey: ['vehicles', 'detail', vehicleId] });
    },
    onError: (e) => toast.error(extractError(e)),
  });
}

export function useCreateReport(vehicleId) {
  return useMutation({
    mutationFn: async (reason) => (await api.post(`/vehicles/${vehicleId}/reports`, { reason })).data,
    onSuccess: () => toast.success('Report submitted. Thank you.'),
    onError: (e) => toast.error(extractError(e)),
  });
}
