import { useForm } from 'react-hook-form';
import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useVehicle, useUpdateVehicle, useReplaceImages } from '../hooks/useVehicles';
import ImageUploader from '../components/ImageUploader';
import Loader from '../components/Loader';
import { CATEGORIES } from '../styles/theme';

export default function EditVehiclePage() {
  const { id } = useParams();
  const { data: v, isLoading } = useVehicle(id);
  const update = useUpdateVehicle(id);
  const replaceImages = useReplaceImages(id);
  const navigate = useNavigate();

  const { register, handleSubmit, reset } = useForm();
  const [newImages, setNewImages] = useState([]);
  const [imgError, setImgError] = useState('');

  useEffect(() => {
    if (v) reset({
      category: v.category, company: v.company, model: v.model,
      year: v.year, price: v.price, mileage: v.mileage,
      location: v.location, description: v.description,
    });
  }, [v, reset]);

  if (isLoading) return <div className="page container"><Loader /></div>;
  if (!v) return <div className="page container"><div className="empty">Not found</div></div>;

  const onSubmit = async (values) => {
    await update.mutateAsync({
      category: values.category, company: values.company.trim(), model: values.model.trim(),
      year: Number(values.year), price: Number(values.price), mileage: Number(values.mileage),
      location: values.location.trim(), description: values.description.trim(),
    });
    navigate(`/vehicles/${id}`);
  };

  const doReplaceImages = async () => {
    setImgError('');
    if (newImages.length < 3 || newImages.length > 5) { setImgError('Please pick 3–5 images'); return; }
    await replaceImages.mutateAsync(newImages);
    setNewImages([]);
  };

  return (
    <div className="page">
      <div className="container" style={{ maxWidth: 780 }}>
        <h1 className="section-title">Edit listing</h1>
        <form onSubmit={handleSubmit(onSubmit)} className="card-yc p-4 mb-3" noValidate>
          <div className="row g-3">
            <div className="col-12 col-md-4">
              <label className="form-label">Category</label>
              <select className="form-select" {...register('category')}>
                {CATEGORIES.map((c) => <option key={c.value} value={c.value}>{c.label}</option>)}
              </select>
            </div>
            <div className="col-12 col-md-4">
              <label className="form-label">Company</label>
              <input className="form-control" {...register('company', { required: true })} />
            </div>
            <div className="col-12 col-md-4">
              <label className="form-label">Model</label>
              <input className="form-control" {...register('model', { required: true })} />
            </div>
            <div className="col-6 col-md-3">
              <label className="form-label">Year</label>
              <input type="number" className="form-control" {...register('year', { required: true })} />
            </div>
            <div className="col-6 col-md-3">
              <label className="form-label">Price (₹)</label>
              <input type="number" className="form-control" {...register('price', { required: true })} />
            </div>
            <div className="col-6 col-md-3">
              <label className="form-label">Mileage</label>
              <input type="number" className="form-control" {...register('mileage', { required: true })} />
            </div>
            <div className="col-6 col-md-3">
              <label className="form-label">Location</label>
              <input className="form-control" {...register('location', { required: true })} />
            </div>
            <div className="col-12">
              <label className="form-label">Description</label>
              <textarea rows="5" className="form-control" {...register('description', { required: true })} />
            </div>
          </div>
          <div className="d-flex gap-2 mt-4">
            <button className="btn btn-accent" disabled={update.isPending}>Save changes</button>
            <button type="button" className="btn btn-ghost" onClick={() => navigate(-1)}>Cancel</button>
          </div>
        </form>

        <div className="card-yc p-4">
          <h5 className="serif mb-3">Replace images</h5>
          <div className="d-flex flex-wrap gap-2 mb-3">
            {v.images.map((img) => (
              <img key={img.id} src={img.url} alt="" className="image-thumb" style={{ width: 84, height: 84 }} />
            ))}
          </div>
          <ImageUploader files={newImages} onChange={setNewImages} error={imgError} />
          <button className="btn btn-outline-dark mt-3" onClick={doReplaceImages}
                  disabled={replaceImages.isPending || newImages.length === 0}>
            {replaceImages.isPending ? 'Uploading…' : 'Upload new set'}
          </button>
        </div>
      </div>
    </div>
  );
}
