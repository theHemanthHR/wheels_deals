import { useForm } from 'react-hook-form';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ImageUploader from '../components/ImageUploader';
import { useCreateVehicle } from '../hooks/useVehicles';
import { CATEGORIES } from '../styles/theme';
import { toast } from 'react-toastify';

export default function SellPage() {
  const { register, handleSubmit, formState: { errors } } = useForm();
  const [images, setImages] = useState([]);
  const [imgError, setImgError] = useState('');
  const createVehicle = useCreateVehicle();
  const navigate = useNavigate();

  const onSubmit = async (values) => {
    setImgError('');
    if (images.length < 3) { setImgError('At least 3 images required'); return; }
    if (images.length > 5) { setImgError('Maximum 5 images allowed'); return; }

    const payload = {
      category: values.category,
      company: values.company.trim(),
      model: values.model.trim(),
      year: Number(values.year),
      price: Number(values.price),
      mileage: Number(values.mileage),
      location: values.location.trim(),
      description: values.description.trim(),
    };

    try {
      const created = await createVehicle.mutateAsync({ data: payload, images });
      // Guard against a malformed response — never navigate to /vehicles/undefined
      if (!created?.id) {
        toast.error('Listing created but server did not return an ID. Check My Listings.');
        navigate('/my-listings');
        return;
      }
      navigate(`/vehicles/${created.id}`, { replace: true });
    } catch {
      // error already surfaced by the mutation's onError
    }
  };

  return (
    <div className="page">
      <div className="container" style={{ maxWidth: 780 }}>
        <h1 className="section-title">Sell your vehicle</h1>
        <p className="muted mb-4">Add photos and details. Your listing goes live immediately.</p>
        <form onSubmit={handleSubmit(onSubmit)} className="card-yc p-4" noValidate>
          <div className="mb-3">
            <label className="form-label">Photos</label>
            <ImageUploader files={images} onChange={setImages} error={imgError} />
          </div>
          <div className="row g-3">
            <div className="col-12 col-md-4">
              <label className="form-label">Category</label>
              <select className={`form-select ${errors.category ? 'is-invalid' : ''}`}
                      {...register('category', { required: 'Category required' })}>
                <option value="">Select…</option>
                {CATEGORIES.map((c) => <option key={c.value} value={c.value}>{c.label}</option>)}
              </select>
              {errors.category && <div className="invalid-feedback">{errors.category.message}</div>}
            </div>
            <div className="col-12 col-md-4">
              <label className="form-label">Company / Brand</label>
              <input className={`form-control ${errors.company ? 'is-invalid' : ''}`} {...register('company', { required: 'Required' })} />
              {errors.company && <div className="invalid-feedback">{errors.company.message}</div>}
            </div>
            <div className="col-12 col-md-4">
              <label className="form-label">Model</label>
              <input className={`form-control ${errors.model ? 'is-invalid' : ''}`} {...register('model', { required: 'Required' })} />
              {errors.model && <div className="invalid-feedback">{errors.model.message}</div>}
            </div>
            <div className="col-6 col-md-3">
              <label className="form-label">Year</label>
              <input type="number" className={`form-control ${errors.year ? 'is-invalid' : ''}`}
                     {...register('year', { required: 'Required', min: { value: 1900, message: 'Invalid' }, max: { value: 2100, message: 'Invalid' } })} />
              {errors.year && <div className="invalid-feedback">{errors.year.message}</div>}
            </div>
            <div className="col-6 col-md-3">
              <label className="form-label">Price (₹)</label>
              <input type="number" className={`form-control ${errors.price ? 'is-invalid' : ''}`}
                     {...register('price', { required: 'Required', min: { value: 1, message: 'Invalid' } })} />
              {errors.price && <div className="invalid-feedback">{errors.price.message}</div>}
            </div>
            <div className="col-6 col-md-3">
              <label className="form-label">Mileage (km)</label>
              <input type="number" className={`form-control ${errors.mileage ? 'is-invalid' : ''}`}
                     {...register('mileage', { required: 'Required', min: { value: 0, message: 'Invalid' } })} />
              {errors.mileage && <div className="invalid-feedback">{errors.mileage.message}</div>}
            </div>
            <div className="col-6 col-md-3">
              <label className="form-label">Location</label>
              <input className={`form-control ${errors.location ? 'is-invalid' : ''}`}
                     placeholder="e.g. Mumbai, Maharashtra" {...register('location', { required: 'Required' })} />
              {errors.location && <div className="invalid-feedback">{errors.location.message}</div>}
            </div>
            <div className="col-12">
              <label className="form-label">Description</label>
              <textarea rows="5" className={`form-control ${errors.description ? 'is-invalid' : ''}`}
                        placeholder="Condition, service history, extras, reason for selling…"
                        {...register('description', { required: 'Required', minLength: { value: 20, message: 'At least 20 characters' } })} />
              {errors.description && <div className="invalid-feedback">{errors.description.message}</div>}
            </div>
          </div>
          <div className="d-flex gap-2 mt-4">
            <button className="btn btn-accent" disabled={createVehicle.isPending}>
              {createVehicle.isPending ? 'Publishing…' : 'Publish listing'}
            </button>
            <button type="button" className="btn btn-ghost" onClick={() => navigate(-1)}>Cancel</button>
          </div>
        </form>
      </div>
    </div>
  );
}
