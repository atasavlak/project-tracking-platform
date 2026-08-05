# Project Tracking Backend

Haftalık Proje Durum Raporlama ve CTO Takip Sistemi için geliştirilen Spring Boot tabanlı backend uygulamasıdır.

Sistem; projelerin, haftalık durum raporlarının, iş kalemlerinin, risk ve engel kayıtlarının ve kullanıcıların merkezi olarak yönetilmesini sağlar. Proje yöneticileri kendi projelerinde işlem yapabilir, kendilerine sorumlu olarak atanan riskleri takip edebilir; CTO ve ADMIN rolleri ise genel proje durumunu görüntüleyebilir.

Backend uygulaması, React tabanlı frontend uygulaması tarafından REST API üzerinden kullanılmaktadır.

## Proje Durumu

Tamamlanan temel özellikler:

- Spring Boot backend iskeleti
- Katmanlı paket yapısı
- PostgreSQL veritabanı bağlantısı ve Docker Compose kurulumu
- Project CRUD, PATCH, filtreleme ve pasife alma
- WeeklyReport CRUD, PATCH, filtreleme ve pasife alma
- WorkItem CRUD, PATCH, filtreleme ve pasife alma
- RiskIssue CRUD, PATCH, filtreleme ve pasife alma
- Risk ve engel kayıtlarına sorumlu kullanıcı atama
- Atanan risklerin ilgili proje yöneticisi tarafından görüntülenmesi
- CTO ve ADMIN dashboard endpointleri
- Kullanıcı ve rol yönetimi
- Rol ve proje sahipliği bazlı yetkilendirme
- Kullanıcı aktivasyon akışı
- Aktivasyon e-postasını yeniden gönderme
- Şifremi unuttum ve parola sıfırlama akışı
- DTO ve validasyon yapısı
- İş kuralı kontrolleri
- Global hata yönetimi
- Health-check endpointi
- Swagger/OpenAPI dokümantasyonu
- Mailpit ile yerel e-posta testi
- Soft delete desteği
- Frontend entegrasyonu

## Kullanılan Teknolojiler

- Java 21
- Spring Boot 4.1.0
- Spring Web
- Spring Data JPA
- Spring Security
- Jakarta Validation
- PostgreSQL
- H2 Database (yalnızca testler)
- Spring Mail
- Springdoc OpenAPI / Swagger UI
- Lombok
- Maven Wrapper
- Git
- IntelliJ IDEA
- Mailpit

## Proje Mimarisi

Uygulama katmanlı mimari kullanılarak geliştirilmiştir.

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Katmanların görevleri:

- `controller`: HTTP isteklerini karşılar ve API cevaplarını döndürür.
- `service`: İş kurallarını, yetki kontrollerini ve uygulama mantığını içerir.
- `repository`: Veritabanı işlemlerini gerçekleştirir.
- `entity`: Veritabanı tablolarını temsil eder.
- `dto`: API istek ve cevap modellerini taşır.
- `exception`: Uygulama hatalarını ve ortak hata cevaplarını yönetir.
- `security`: Kimlik doğrulama, yetkilendirme ve güvenlik hata cevaplarını yönetir.
- `config`: Swagger, Security ve diğer uygulama konfigürasyonlarını içerir.
- `user`: Kullanıcı, rol, aktivasyon ve parola sıfırlama yapılarını içerir.

## Paket Yapısı

```text
src/main/java/com/kolaysoft/projecttracking
├── config
├── controller
├── dto
├── entity
├── exception
├── repository
├── security
├── service
├── user
└── ProjecttrackingApplication.java
```

## Gereksinimler

Projeyi çalıştırabilmek için aşağıdaki araçların kurulu olması gerekir:

- Java 21
- Git
- Mailpit

Maven'ın ayrıca kurulması zorunlu değildir. Projede Maven Wrapper bulunmaktadır.

Sürümleri kontrol etmek için:

```bash
java -version
git --version
```

## Projenin Çalıştırılması

Repository bilgisayara indirildikten sonra backend klasörüne geçilir:

```bash
cd project-tracking-manager/backend
```

Windows üzerinde uygulamayı çalıştırmak için:

```bash
mvnw.cmd spring-boot:run
```

Maven kuruluysa aşağıdaki komut da kullanılabilir:

```bash
mvn spring-boot:run
```

Testleri çalıştırmak için:

```bash
mvnw.cmd test
```

Build almak için:

```bash
mvnw.cmd clean package
```

IntelliJ IDEA üzerinden çalıştırmak için:

1. Projeyi IntelliJ IDEA ile açın.
2. Maven bağımlılıklarının yüklenmesini bekleyin.
3. `ProjecttrackingApplication` sınıfını açın.
4. `Run` butonuna basın.

Uygulama varsayılan olarak aşağıdaki adreste çalışır:

```text
http://localhost:8080
```

## Health Check

Backend uygulamasının çalışıp çalışmadığını kontrol etmek için:

```http
GET http://localhost:8080/api/health
```

Örnek cevap:

```json
{
  "status": "UP",
  "message": "Project Tracking Backend is running"
}
```

## API Dokümantasyonu

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON dokümanı:

```text
http://localhost:8080/v3/api-docs
```

Swagger UI üzerinden endpointler görüntülenebilir, istek gövdeleri incelenebilir, HTTP Basic ile giriş yapılabilir ve API testleri gerçekleştirilebilir.

## Kimlik Doğrulama

Uygulama stateless HTTP Basic Authentication kullanır.

Kimlik doğrulama kullanıcı adı veya e-posta adresi ve parola ile gerçekleştirilir.

Giriş yapan kullanıcının bilgilerini döndüren endpoint:

```http
GET /api/auth/me
```

Public erişime açık başlıca alanlar:

```text
Swagger UI
OpenAPI dokümanı
POST /api/auth/activate
POST /api/auth/forgot-password
POST /api/auth/reset-password
```

Diğer API endpointleri kimlik doğrulama gerektirir.

## Roller ve Yetkiler

### ADMIN

- Bütün aktif projeleri, raporları, iş kalemlerini, riskleri ve engelleri görüntüleyebilir.
- Proje, haftalık rapor, iş kalemi, risk ve engel kayıtlarını yönetebilir.
- Kullanıcı oluşturabilir ve kullanıcı bilgilerini yönetebilir.
- Kullanıcının rolünü ve aktiflik durumunu değiştirebilir.
- Aktivasyon e-postasını yeniden gönderebilir.
- Dashboard endpointlerini kullanabilir.

### PROJECT_MANAGER

- Yalnızca kendisine bağlı projeleri görüntüleyebilir ve yönetebilir.
- Kendi projeleri için haftalık rapor oluşturabilir ve yönetebilir.
- Kendi projelerindeki iş kalemlerini oluşturabilir, güncelleyebilir ve pasife alabilir.
- Kendi projelerindeki risk ve engel kayıtlarını görüntüleyebilir ve yönetebilir.
- Başka bir projede kendisine sorumlu olarak atanan risk veya engel kaydını görüntüleyebilir.
- Kendisine sorumlu olarak atanan risk veya engel kaydını güncelleyebilir.
- Başka bir proje yöneticisine ait risk veya engel kaydını silemez.
- Kendisine atanmamış başka proje yöneticilerine ait kayıtları görüntüleyemez.

### TEAM_LEAD

- İzin verilen aktif kayıtları görüntüleyebilir.
- Aktif iş kalemlerini güncelleyebilir.
- Aktif risk ve engel kayıtlarını güncelleyebilir.
- İş kalemi, risk veya engel kaydı oluşturamaz ya da pasife alamaz.

### CTO

- Aktif projeleri, haftalık raporları, iş kalemlerini, riskleri ve engelleri görüntüleyebilir.
- Dashboard endpointlerini kullanabilir.
- Yönetim işlemlerinde salt okunur role sahiptir.

## Örnek Kullanıcılar

| Rol | Kullanıcı adı | Parola |
|---|---|---|
| ADMIN | `admin` | `Admin123!` |
| PROJECT_MANAGER | `manager` | `Manager123!` |
| PROJECT_MANAGER | `manager2` | `Manager2123!` |
| CTO | `cto` | `Cto123!` |
| TEAM_LEAD | `teamlead` | `TeamLead123!` |

Bu kullanıcılar yalnızca yerel geliştirme ve demo ortamı içindir.

## Project API

Temel endpointler:

```text
POST   /api/projects
GET    /api/projects
GET    /api/projects/{id}
PUT    /api/projects/{id}
PATCH  /api/projects/{id}
DELETE /api/projects/{id}
```

Proje listeleme endpointi desteklenen alanlara göre filtrelenebilir. PROJECT_MANAGER yalnızca kendi projelerini, ADMIN ise bütün aktif projeleri görüntüler.

## Weekly Report API

Temel endpointler:

```text
POST   /api/weekly-reports
GET    /api/weekly-reports
GET    /api/weekly-reports/{id}
PUT    /api/weekly-reports/{id}
PATCH  /api/weekly-reports/{id}
DELETE /api/weekly-reports/{id}
```

Desteklenen filtreler:

```text
projectId
status
weekStartDate
weekEndDate
```

Örnek:

```http
GET /api/weekly-reports?projectId=1&status=AT_RISK&weekStartDate=2026-07-01&weekEndDate=2026-07-31
```

Uygulanan temel kurallar:

- Hafta başlangıç ve bitiş tarihleri zorunludur.
- Hafta bitiş tarihi başlangıç tarihinden önce olamaz.
- Aynı proje ve hafta için birden fazla aktif rapor oluşturulamaz.
- Pasif projeye bağlı raporlar üzerinde işlem yapılamaz.
- PROJECT_MANAGER yalnızca kendi projelerine ait raporları görüntüleyebilir ve yönetebilir.

## Work Item API

Temel endpointler:

```text
POST   /api/work-items
GET    /api/work-items
GET    /api/work-items/{id}
PUT    /api/work-items/{id}
PATCH  /api/work-items/{id}
DELETE /api/work-items/{id}
```

Desteklenen filtreler:

```text
projectId
weeklyReportId
status
responsiblePerson
```

Örnek:

```http
GET /api/work-items?projectId=1&status=DELAYED&responsiblePerson=ahmet
```

Riskli, gecikmiş ve bloke iş kalemleri durum alanı üzerinden filtrelenir:

```http
GET /api/work-items?status=AT_RISK
GET /api/work-items?status=DELAYED
GET /api/work-items?status=BLOCKED
```

Uygulanan temel kurallar:

- Aynı haftalık raporda aynı başlığa sahip birden fazla aktif iş kalemi oluşturulamaz.
- Pasif rapor veya projeye bağlı iş kalemleri üzerinde işlem yapılamaz.
- PROJECT_MANAGER yalnızca kendi projelerindeki iş kalemlerini görüntüleyebilir ve yönetebilir.
- TEAM_LEAD aktif iş kalemlerini güncelleyebilir ancak oluşturamaz veya pasife alamaz.

## Risk ve Engel API

Temel endpointler:

```text
POST   /api/risk-issues
GET    /api/risk-issues
GET    /api/risk-issues/{id}
PUT    /api/risk-issues/{id}
PATCH  /api/risk-issues/{id}
DELETE /api/risk-issues/{id}
```

Desteklenen filtreler:

```text
projectId
weeklyReportId
type
severity
status
responsibleUserId
followUpDateFrom
followUpDateTo
```

Örnek:

```http
GET /api/risk-issues?projectId=1&severity=HIGH&status=OPEN
```

Sorumlu kullanıcıya göre filtreleme:

```http
GET /api/risk-issues?responsibleUserId=2
```

Takip tarihi aralığına göre filtreleme:

```http
GET /api/risk-issues?followUpDateFrom=2026-07-01&followUpDateTo=2026-07-31
```

Uygulanan temel kurallar:

- Aynı haftalık raporda aynı başlığa sahip birden fazla aktif risk veya engel kaydı oluşturulamaz.
- Pasif rapor veya projeye bağlı risk ve engel kayıtları üzerinde işlem yapılamaz.
- Sorumlu kullanıcı aktif ve aktivasyonunu tamamlamış olmalıdır.
- PROJECT_MANAGER kendi projesindeki riskleri görüntüleyebilir.
- PROJECT_MANAGER kendisine sorumlu olarak atanan riskleri de görüntüleyebilir ve güncelleyebilir.
- Bir PROJECT_MANAGER başka bir proje yöneticisine ait kaydı silemez.
- TEAM_LEAD aktif risk ve engel kayıtlarını güncelleyebilir.
- CTO kayıtları salt okunur olarak görüntüleyebilir.
- ADMIN tüm işlemleri gerçekleştirebilir.

## Atanabilir Kullanıcılar API

Risk ve engel kayıtlarında sorumlu kullanıcı seçimi için kullanılabilecek endpoint:

```http
GET /api/users/assignable
```

Bu endpoint aktif ve aktivasyonunu tamamlamış kullanıcıların seçim listesinde gösterilmesini sağlar.

## Dashboard API

CTO ve ADMIN rollerinin kullanabildiği temel endpointler:

```text
GET /api/dashboard/summary
GET /api/dashboard/projects
GET /api/dashboard/risky-work-items
```

Dashboard aktif proje, haftalık rapor, iş kalemi, risk ve engel verilerinden özet bilgiler üretir.

## Kullanıcı Yönetimi API

ADMIN rolünün kullanabildiği endpointler:

```text
POST  /api/admin/users
GET   /api/admin/users
GET   /api/admin/users/{id}
PATCH /api/admin/users/{id}/role
PATCH /api/admin/users/{id}/status
POST  /api/admin/users/{id}/resend-activation
```

Yeni kullanıcı oluşturulurken ADMIN tarafından parola belirlenmez. Kullanıcı pasif durumda oluşturulur ve e-posta ile gönderilen aktivasyon tokenı üzerinden ilk parolasını belirler.

## Hesap Aktivasyonu ve Parola Sıfırlama

Public endpointler:

```text
POST /api/auth/activate
POST /api/auth/forgot-password
POST /api/auth/reset-password
```

Özellikler:

- Aktivasyon ve parola sıfırlama tokenlarının açık değeri veritabanında saklanmaz.
- Tokenlar tek kullanımlıktır ve süre kontrolüne tabidir.
- Aktivasyon tamamlandığında kullanıcı aktif hale gelir ve ilk parolasını belirler.
- Şifremi unuttum endpointi hesap varlığını açık etmeyen genel bir cevap döndürür.
- Parolalar BCrypt ile saklanır.

## Mailpit

Aktivasyon ve parola sıfırlama e-postaları geliştirme ortamında Mailpit üzerinden görüntülenir.

SMTP:

```text
localhost:1025
```

Mailpit arayüzü:

```text
http://localhost:8025
```

## Durum Değerleri

### ProjectStatus

```text
PLANNED
IN_PROGRESS
ON_TRACK
AT_RISK
DELAYED
COMPLETED
ON_HOLD
```

### WeeklyReportStatus

```text
ON_TRACK
AT_RISK
DELAYED
```

### WorkItemStatus

```text
PLANNED
IN_PROGRESS
COMPLETED
DELAYED
AT_RISK
BLOCKED
```

### RiskIssueType

```text
RISK
ISSUE
```

### RiskIssueSeverity

```text
LOW
MEDIUM
HIGH
CRITICAL
```

### RiskIssueStatus

```text
OPEN
IN_PROGRESS
RESOLVED
CLOSED
```

## Hata Yönetimi

Uygulamadaki hatalar ortak bir JSON formatında döndürülür.

```json
{
  "timestamp": "2026-07-30T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Doğrulama hatası",
  "path": "/api/example",
  "fieldErrors": {
    "fieldName": "Alan zorunludur."
  }
}
```

Temel HTTP durum kodları:

| Durum kodu | Açıklama |
|---|---|
| `200 OK` | İstek başarıyla tamamlandı |
| `201 Created` | Yeni kayıt oluşturuldu |
| `204 No Content` | Pasife alma işlemi başarıyla tamamlandı |
| `400 Bad Request` | Validasyon veya iş kuralı hatası oluştu |
| `401 Unauthorized` | Kimlik doğrulama başarısız oldu |
| `403 Forbidden` | Kullanıcının işlem için yetkisi bulunmuyor |
| `404 Not Found` | İstenen kayıt bulunamadı |
| `409 Conflict` | Çakışan kayıt veya iş kuralı hatası oluştu |
| `500 Internal Server Error` | Beklenmeyen sistem hatası oluştu |

## PostgreSQL Veritabanı

Uygulamanın normal çalışma veritabanı PostgreSQL olarak yapılandırılmıştır. Bağlantı bilgileri ortam değişkenlerinden alınır:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
```

Docker Compose kurulumu veritabanını `postgres_data` isimli volume üzerinde kalıcı olarak saklar. H2 yalnızca otomatik testlerde PostgreSQL uyumluluk modunda kullanılır.

## Frontend

React tabanlı frontend projesi ayrı bir repository içinde bulunmaktadır:

```text
https://github.com/atasavlak/project-tracking-frontend
```

Frontend geliştirme ortamında varsayılan olarak aşağıdaki adreste çalışır:

```text
http://localhost:5173
```

Backend servisi:

```text
http://localhost:8080
```

## Git Çalışma Düzeni

Projede özellik bazlı branch yapısı kullanılmaktadır.

```text
main
└── develop
    ├── feature/backend-setup
    ├── feature/project-module
    ├── feature/weekly-report-module
    ├── feature/work-item-module
    └── feature/risk-issue-module
```

Branch kuralları:

- `main`: Kararlı ve teslim edilebilir sürüm.
- `develop`: Tamamlanan feature branchlerinin birleştirildiği geliştirme branchi.
- `feature/*`: Yeni özellik veya geliştirme çalışmaları.
- `fix/*`: Hata düzeltmeleri.
- `docs/*`: Yalnızca dokümantasyon değişiklikleri.

Commit mesajlarında aşağıdaki ön ekler kullanılabilir:

```text
feat: Yeni özellik
fix: Hata düzeltmesi
docs: Dokümantasyon değişikliği
test: Test geliştirmesi
refactor: Kod düzenlemesi
chore: Kurulum veya yapılandırma işlemi
```

## Bilinen Sınırlamalar

- JWT kullanılmamaktadır; kimlik doğrulama HTTP Basic ile yapılmaktadır.
- Testlerde H2 PostgreSQL uyumluluk modu kullanılmaktadır; uygulama verisi PostgreSQL üzerinde tutulur.
- E-posta gönderimi geliştirme ortamında Mailpit üzerinden yapılmaktadır.
- Şema yönetimi geliştirme ortamında Hibernate `ddl-auto=update` ile yapılmaktadır; canlı ortam öncesinde Flyway/Liquibase gereklidir.
- Rate limiting, audit log ve merkezi loglama altyapısı henüz bulunmamaktadır.
- Docker Compose yerel kurulum için hazırlanmıştır; canlı ortam secret, TLS ve merkezi loglama ayarları ayrıca yapılmalıdır.

## Planlanan Geliştirmeler

- DecisionLog modülü
- ActionItem modülü
- Dashboard risk, karar ve aksiyon özetlerinin genişletilmesi
- PDF ve Excel çıktı endpointleri
- Audit log
- Flyway/Liquibase migration yapısı
- CI/CD ve production deployment ayarları

## Geliştirici

Ata Burak Savlak
