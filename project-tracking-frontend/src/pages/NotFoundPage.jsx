import { Link } from 'react-router-dom'

export default function NotFoundPage() {
  return (
    <div className="centered-page">
      <div className="centered-card">
        <span className="error-code">404</span>
        <h1>Sayfa bulunamadı</h1>
        <p>Aradığınız adres mevcut değil veya taşınmış olabilir.</p>
        <Link className="button button-primary" to="/">Ana Sayfaya Dön</Link>
      </div>
    </div>
  )
}
