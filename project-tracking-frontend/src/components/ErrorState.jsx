export default function ErrorState({ message, onRetry }) {
  return (
    <div className="state-panel state-panel-error" role="alert">
      <div>
        <strong>İşlem tamamlanamadı</strong>
        <p>{message}</p>
      </div>
      {onRetry ? (
        <button className="button button-secondary" type="button" onClick={onRetry}>
          Yeniden Dene
        </button>
      ) : null}
    </div>
  )
}
