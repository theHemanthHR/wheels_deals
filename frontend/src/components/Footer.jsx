export default function Footer() {
  return (
    <footer className="footer-yc">
      <div className="container d-flex flex-wrap justify-content-between gap-3">
        <div>
          <div className="brand mb-1">Vahan<span className="brand-dot">.</span></div>
          <div>Buy &amp; sell cars, bikes, scooters &amp; bicycles across India.</div>
        </div>
        <div className="text-md-end">
          <div>© {new Date().getFullYear()} Vahan Marketplace</div>
          <div className="mt-1">Made for India · Prices in INR</div>
        </div>
      </div>
    </footer>
  );
}
