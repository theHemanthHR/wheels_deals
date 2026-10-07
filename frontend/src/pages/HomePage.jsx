import { useState } from 'react';
import { useVehicleSearch } from '../hooks/useVehicles';
import VehicleCard from '../components/VehicleCard';
import Pagination from '../components/Pagination';
import Empty from '../components/Empty';
import Loader from '../components/Loader';
import { CATEGORIES, SORT_OPTIONS } from '../styles/theme';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function HomePage() {
  const { isAuthenticated } = useAuth();
  const [keyword, setKeyword] = useState('');
  const [category, setCategory] = useState('');
  const [company, setCompany] = useState('');
  const [location, setLocation] = useState('');
  const [sort, setSort] = useState('newest');
  const [page, setPage] = useState(0);

  const params = { keyword: keyword || undefined, category: category || undefined,
                   company: company || undefined, location: location || undefined,
                   sort, page, size: 12 };
  const { data, isLoading, isFetching } = useVehicleSearch(params);

  const reset = () => { setKeyword(''); setCategory(''); setCompany(''); setLocation(''); setSort('newest'); setPage(0); };
  const submit = (e) => { e.preventDefault(); setPage(0); };

  return (
    <div className="page">
      <div className="container">
        <div className="mb-4">
          <h1 className="section-title">Find your next ride.</h1>
          <p className="muted">
            Buy and sell cars, bikes, scooters and bicycles across India.
            {!isAuthenticated && <> <Link to="/register">Create an account</Link> to list your vehicle or contact sellers.</>}
          </p>
        </div>

        <form onSubmit={submit} className="card-yc p-3 mb-4">
          <div className="row g-2">
            <div className="col-12 col-md-4">
              <input className="form-control" placeholder="Search by keyword (e.g. Swift, Activa)"
                     value={keyword} onChange={(e) => setKeyword(e.target.value)} />
            </div>
            <div className="col-6 col-md-2">
              <select className="form-select" value={category} onChange={(e) => setCategory(e.target.value)}>
                <option value="">All categories</option>
                {CATEGORIES.map((c) => <option key={c.value} value={c.value}>{c.label}</option>)}
              </select>
            </div>
            <div className="col-6 col-md-2">
              <input className="form-control" placeholder="Brand" value={company}
                     onChange={(e) => setCompany(e.target.value)} />
            </div>
            <div className="col-6 col-md-2">
              <input className="form-control" placeholder="Location" value={location}
                     onChange={(e) => setLocation(e.target.value)} />
            </div>
            <div className="col-6 col-md-2">
              <select className="form-select" value={sort} onChange={(e) => setSort(e.target.value)}>
                {SORT_OPTIONS.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
              </select>
            </div>
          </div>
          <div className="d-flex gap-2 mt-3">
            <button type="submit" className="btn btn-accent">Search</button>
            <button type="button" className="btn btn-ghost" onClick={reset}>Reset</button>
            {isAuthenticated && (
              <Link to="/sell" className="btn btn-outline-dark ms-auto">
                <i className="bi bi-plus-lg me-1" /> Sell a vehicle
              </Link>
            )}
          </div>
        </form>

        {isLoading ? (
          <Loader label="Finding vehicles…" />
        ) : !data?.content?.length ? (
          <Empty icon="bi-search" title="No vehicles found" hint="Try changing filters or keywords."
                 action={<button className="btn btn-ghost" onClick={reset}>Clear filters</button>} />
        ) : (
          <>
            <div className="d-flex justify-content-between align-items-center mb-3">
              <div className="muted small">
                {data.totalElements.toLocaleString('en-IN')} results
                {isFetching && <span className="ms-2 spinner-border spinner-border-sm" />}
              </div>
            </div>
            <div className="row g-3 g-md-4">
              {data.content.map((v) => (
                <div className="col-6 col-md-4 col-lg-3" key={v.id}><VehicleCard v={v} /></div>
              ))}
            </div>
            <Pagination page={data.number} totalPages={data.totalPages} onChange={setPage} />
          </>
        )}
      </div>
    </div>
  );
}
