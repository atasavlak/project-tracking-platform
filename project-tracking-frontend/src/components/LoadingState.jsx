export default function LoadingState({ message = 'Yükleniyor...', fullPage = false }) {
  return (
    <div className={fullPage ? 'state-page' : 'state-panel'} role="status">
      <span className="spinner" aria-hidden="true" />
      <span>{message}</span>
    </div>
  )
}
