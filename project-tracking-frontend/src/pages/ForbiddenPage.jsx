import { Link } from 'react-router-dom'

export default function ForbiddenPage() {
  return (
    <div className="centered-page">
      <div className="centered-card">
        <span className="error-code">403</span>
        <h1>Bu sayfaya erişim yetkiniz yok</h1>
        <p>Kullanıcı rolünüz bu işlemi gerçekleştirmeye izin vermiyor.</p>
        <Link className="button button-primary" to="/">Ana Sayfaya Dön</Link>
      </div>
    </div>
  )
}
