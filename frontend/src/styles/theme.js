export const CATEGORIES = [
  { value: 'CAR', label: 'Car' },
  { value: 'BIKE', label: 'Bike' },
  { value: 'SCOOTER', label: 'Scooter' },
  { value: 'BICYCLE', label: 'Bicycle' },
  { value: 'OTHER', label: 'Other' },
];

export const SORT_OPTIONS = [
  { value: 'newest',      label: 'Newest first' },
  { value: 'price_asc',   label: 'Price: Low to High' },
  { value: 'price_desc',  label: 'Price: High to Low' },
  { value: 'year_desc',   label: 'Year: Newest' },
  { value: 'mileage_asc', label: 'Mileage: Low to High' },
];

export const inr = (n) =>
  new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(Number(n || 0));

export const formatDate = (d) =>
  d ? new Date(d).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' }) : '—';
