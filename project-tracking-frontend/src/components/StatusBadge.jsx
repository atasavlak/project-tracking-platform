import {
  ACTION_ITEM_STATUS_LABELS,
  DECISION_STATUS_LABELS,
  PROJECT_HEALTH_STATUS_LABELS,
  PROJECT_STATUS_LABELS,
  RISK_ISSUE_STATUS_LABELS,
  WEEKLY_REPORT_STATUS_LABELS,
  WORK_ITEM_STATUS_LABELS,
} from '../utils/statuses.js'

const STATUS_LABELS = {
  ...ACTION_ITEM_STATUS_LABELS,
  ...DECISION_STATUS_LABELS,
  ...PROJECT_HEALTH_STATUS_LABELS,
  ...PROJECT_STATUS_LABELS,
  ...WEEKLY_REPORT_STATUS_LABELS,
  ...WORK_ITEM_STATUS_LABELS,
  ...RISK_ISSUE_STATUS_LABELS,
}

export default function StatusBadge({ status }) {
  return (
    <span
      className={`status-badge status-${
        status?.toLowerCase() ?? 'unknown'
      }`}
    >
      {STATUS_LABELS[status] ?? status ?? '-'}
    </span>
  )
}
