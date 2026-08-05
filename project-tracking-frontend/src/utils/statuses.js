export const PROJECT_STATUSES = [
  'PLANNED',
  'IN_PROGRESS',
  'ON_TRACK',
  'AT_RISK',
  'DELAYED',
  'COMPLETED',
  'ON_HOLD',
]

export const PROJECT_STATUS_LABELS = Object.freeze({
  PLANNED: 'Planlandı',
  IN_PROGRESS: 'Devam Ediyor',
  ON_TRACK: 'Planında',
  AT_RISK: 'Riskli',
  DELAYED: 'Gecikmiş',
  COMPLETED: 'Tamamlandı',
  ON_HOLD: 'Beklemede',
})

export const WEEKLY_REPORT_STATUSES = [
  'ON_TRACK',
  'AT_RISK',
  'DELAYED',
]

export const WEEKLY_REPORT_STATUS_LABELS =
  Object.freeze({
    ON_TRACK: 'Planında',
    AT_RISK: 'Riskli',
    DELAYED: 'Gecikmiş',
  })

export const WORK_ITEM_STATUSES = [
  'PLANNED',
  'IN_PROGRESS',
  'COMPLETED',
  'AT_RISK',
  'BLOCKED',
  'DELAYED',
]

export const WORK_ITEM_STATUS_LABELS = Object.freeze({
  PLANNED: 'Planlandı',
  IN_PROGRESS: 'Devam Ediyor',
  COMPLETED: 'Tamamlandı',
  AT_RISK: 'Riskli',
  BLOCKED: 'Blokeli',
  DELAYED: 'Gecikmiş',
})

export const RISK_ISSUE_TYPES = [
  'RISK',
  'ISSUE',
]

export const RISK_ISSUE_TYPE_LABELS =
  Object.freeze({
    RISK: 'Risk',
    ISSUE: 'Engel',
  })

export const RISK_ISSUE_SEVERITIES = [
  'LOW',
  'MEDIUM',
  'HIGH',
  'CRITICAL',
]

export const RISK_ISSUE_SEVERITY_LABELS =
  Object.freeze({
    LOW: 'Düşük',
    MEDIUM: 'Orta',
    HIGH: 'Yüksek',
    CRITICAL: 'Kritik',
  })

export const RISK_ISSUE_STATUSES = [
  'OPEN',
  'IN_PROGRESS',
  'RESOLVED',
  'CLOSED',
]

export const RISK_ISSUE_STATUS_LABELS =
  Object.freeze({
    OPEN: 'Açık',
    IN_PROGRESS: 'Devam Ediyor',
    RESOLVED: 'Çözüldü',
    CLOSED: 'Kapatıldı',
  })
export const DECISION_STATUSES = [
  'DRAFT',
  'APPROVED',
  'IMPLEMENTED',
  'CANCELLED',
]

export const DECISION_STATUS_LABELS =
  Object.freeze({
    DRAFT: 'Taslak',
    APPROVED: 'Onaylandı',
    IMPLEMENTED: 'Uygulandı',
    CANCELLED: 'İptal Edildi',
  })

export const ACTION_ITEM_PRIORITIES = [
  'LOW',
  'MEDIUM',
  'HIGH',
  'CRITICAL',
]

export const ACTION_ITEM_PRIORITY_LABELS =
  Object.freeze({
    LOW: 'Düşük',
    MEDIUM: 'Orta',
    HIGH: 'Yüksek',
    CRITICAL: 'Kritik',
  })

export const ACTION_ITEM_STATUSES = [
  'OPEN',
  'IN_PROGRESS',
  'COMPLETED',
  'CANCELLED',
  'OVERDUE',
]

export const ACTION_ITEM_STATUS_LABELS =
  Object.freeze({
    OPEN: 'Açık',
    IN_PROGRESS: 'Devam Ediyor',
    COMPLETED: 'Tamamlandı',
    CANCELLED: 'İptal Edildi',
    OVERDUE: 'Gecikmiş',
  })


export const PROJECT_HEALTH_STATUSES = [
  'HEALTHY',
  'NEEDS_ATTENTION',
  'CRITICAL',
  'NO_REPORT',
]

export const PROJECT_HEALTH_STATUS_LABELS =
  Object.freeze({
    HEALTHY: 'Sağlıklı',
    NEEDS_ATTENTION: 'Dikkat Gerektiriyor',
    CRITICAL: 'Kritik',
    NO_REPORT: 'Rapor Bulunmuyor',
  })
