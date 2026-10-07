import { useRef } from 'react';

export default function ImageUploader({ files, onChange, error, min = 3, max = 5 }) {
  const inputRef = useRef();

  const handleAdd = (e) => {
    const picked = Array.from(e.target.files || []);
    const merged = [...files, ...picked].slice(0, max);
    onChange(merged);
    e.target.value = '';
  };

  const remove = (idx) => onChange(files.filter((_, i) => i !== idx));

  return (
    <div>
      <div className="d-flex flex-wrap gap-2 mb-2">
        {files.map((f, i) => (
          <div key={i} style={{ position: 'relative', width: 96 }}>
            <img src={URL.createObjectURL(f)} alt="preview" className="image-thumb" />
            <button type="button" className="btn btn-sm btn-danger"
                    style={{ position: 'absolute', top: 2, right: 2, padding: '0 6px', lineHeight: 1 }}
                    onClick={() => remove(i)}>×</button>
          </div>
        ))}
        {files.length < max && (
          <button type="button" className="btn btn-ghost" style={{ width: 96, height: 96 }}
                  onClick={() => inputRef.current?.click()}>
            <i className="bi bi-plus-lg" />
          </button>
        )}
      </div>
      <input ref={inputRef} type="file" accept="image/jpeg,image/png,image/webp,image/gif" multiple hidden onChange={handleAdd} />
      <div className="form-text">
        {min}–{max} images required · JPG/PNG/WEBP · max 5MB each · {files.length} selected
      </div>
      {error && <div className="text-danger small mt-1">{error}</div>}
    </div>
  );
}
