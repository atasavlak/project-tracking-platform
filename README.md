# Project Tracking Platform

Proje ekiplerinin çalışmalarını tek merkezden takip edebilmesi için geliştirilmiş, rol bazlı yetkilendirmeye sahip full-stack proje yönetim uygulamasıdır.

Sistem; projelerin, haftalık raporların, iş kalemlerinin, risk ve engellerin, kararların ve aksiyonların yönetilmesini sağlar. Yönetici dashboard'u üzerinden proje sağlığı, kritik riskler, geciken aksiyonlar ve riskli iş kalemleri takip edilebilir.

## Özellikler

- Proje oluşturma, listeleme, güncelleme ve pasife alma
- Haftalık proje durum raporlarının yönetimi
- İş kalemi ve aksiyon takibi
- Risk ve engel kayıtlarının yönetimi
- Karar kayıtlarının tutulması
- Rol ve proje sahipliği bazlı yetkilendirme
- CTO ve yönetici dashboard ekranları
- Haftalık rapor ve dashboard için AI destekli analiz alanları
- Kullanıcı oluşturma ve hesap aktivasyon akışı
- Şifremi unuttum ve parola sıfırlama işlemleri
- PDF ve Excel çıktı desteği
- Swagger/OpenAPI dokümantasyonu
- PostgreSQL üzerinde kalıcı veri saklama
- Docker Compose ile tek komutla çalışma
- Mailpit ile yerel e-posta testi

> AI özellikleri mevcut geliştirme yapısında mock/kural tabanlı sağlayıcı üzerinden çalışmaktadır. Gerçek bir yapay zekâ servisi bağlı değildir.

## Kullanılan Teknolojiler

### Frontend

- React 19
- Vite
- React Router
- JavaScript
- CSS
- Nginx

### Backend

- Java 21
- Spring Boot 4.1
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Jakarta Validation
- Spring Mail
- Springdoc OpenAPI
- Apache POI
- Apache PDFBox
- Maven

### Altyapı

- PostgreSQL 17
- Docker
- Docker Compose
- Mailpit

## Sistem Mimarisi

```text
Kullanıcı
   │
   ▼
React + Nginx
   │  /api
   ▼
Spring Boot REST API
   │
   ├── PostgreSQL
   └── Mailpit
```

Docker Compose aşağıdaki servisleri birlikte çalıştırır:

| Servis | Görevi |
|---|---|
| `frontend` | React uygulamasını Nginx üzerinden yayınlar. |
| `backend` | Spring Boot REST API uygulamasını çalıştırır. |
| `postgres` | Uygulama verilerini kalıcı olarak saklar. |
| `mailpit` | Aktivasyon ve şifre sıfırlama maillerini yerel ortamda yakalar. |

## Proje Yapısı

```text
.
├── compose.yaml
├── .env.example
├── .gitignore
├── project-tracking-frontend
│   ├── src
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
└── project-tracking-manager
    └── backend
        ├── src
        ├── Dockerfile
        ├── pom.xml
        ├── mvnw
        └── mvnw.cmd
```

Backend katmanlı mimari kullanır:

```text
Controller → Service → Repository → PostgreSQL
```

## Roller

### `ADMIN`

Tüm aktif kayıtları ve kullanıcıları yönetebilir. Dashboard ve AI analiz özelliklerine erişebilir.

### `PROJECT_MANAGER`

Sorumlu olduğu projeleri yönetebilir. Kendi projelerine haftalık rapor, iş kalemi, risk, karar ve aksiyon ekleyebilir. Kendi projesinin haftalık raporu için AI analizini kullanabilir.

### `TEAM_LEAD`

Yetkili olduğu aktif kayıtları görüntüleyebilir ve izin verilen iş kalemi, risk ve engel kayıtlarını güncelleyebilir.

### `CTO`

Aktif projelerin genel durumunu ve yönetici dashboard'unu görüntüleyebilir. AI yönetici özeti özelliğine erişebilir.

## Docker ile Kurulum

### Gereksinimler

- Git
- Docker Desktop
- Docker Compose

Java, Maven, Node.js ve PostgreSQL'in bilgisayara ayrıca kurulması zorunlu değildir.

### Repository'yi klonlayın

```bash
git clone https://github.com/atasavlak/project-tracking-platform.git
cd project-tracking-platform
```

### Ortam dosyasını oluşturun

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Linux/macOS:

```bash
cp .env.example .env
```

Geliştirme ortamı için örnek `.env` içeriği:

```env
POSTGRES_DB=projecttracking
POSTGRES_USER=projecttracking
POSTGRES_PASSWORD=projecttracking

FRONTEND_PUBLIC_URL=http://localhost:3000
APP_MAIL_FROM=no-reply@projecttracking.local
APP_SAMPLE_DATA_ENABLED=true

SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
```

### Uygulamayı başlatın

```bash
docker compose up --build -d
```

### Servisleri kontrol edin

```bash
docker compose ps
```

## Servis Adresleri

| Servis | Adres |
|---|---|
| Frontend | http://localhost:3000 |
| Backend | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Mailpit | http://localhost:8025 |
| PostgreSQL | `localhost:5432` |

## Örnek Kullanıcılar

Örnek veri oluşturma açıkken aşağıdaki demo kullanıcıları kullanılabilir:

| Rol | Kullanıcı adı | Parola |
|---|---|---|
| ADMIN | `admin` | `Admin123!` |
| PROJECT_MANAGER | `manager` | `Manager123!` |
| PROJECT_MANAGER | `manager2` | `Manager2123!` |
| CTO | `cto` | `Cto123!` |
| TEAM_LEAD | `teamlead` | `TeamLead123!` |

Bu hesaplar yalnızca geliştirme ve demo ortamı içindir.

## Temel Docker Komutları

Servisleri başlatmak:

```bash
docker compose up -d
```

Kod değişikliklerinden sonra image'ları yeniden oluşturmak:

```bash
docker compose up --build -d
```

Logları takip etmek:

```bash
docker compose logs -f
```

Servisleri durdurmak:

```bash
docker compose down
```

Servislerle birlikte PostgreSQL verilerini de silmek:

```bash
docker compose down -v
```

> `docker compose down -v` veritabanındaki bütün kayıtları kalıcı olarak siler.

## Yerel Geliştirme

### Frontend

```bash
cd project-tracking-frontend
npm install
npm run dev
```

Frontend geliştirme adresi:

```text
http://localhost:5173
```

### Backend

Önce PostgreSQL ve Mailpit'i başlatın:

```bash
docker compose up -d postgres mailpit
```

Ardından:

```powershell
cd project-tracking-manager/backend
.\mvnw.cmd spring-boot:run
```

Backend adresi:

```text
http://localhost:8080
```

## Veritabanı

Uygulama verileri PostgreSQL üzerinde saklanır. Docker Compose, verileri `postgres_data` volume'ünde kalıcı tutar.

Varsayılan geliştirme bağlantı bilgileri:

```text
Host: localhost
Port: 5432
Database: projecttracking
Username: projecttracking
Password: projecttracking
```

Tabloları listelemek:

```powershell
docker compose exec postgres psql -U projecttracking -d projecttracking -c "\dt"
```

Verileri görsel olarak incelemek için DBeaver veya pgAdmin kullanılabilir.

## E-posta Testi

Geliştirme ortamında kullanıcı aktivasyonu ve şifre sıfırlama mailleri Mailpit tarafından yakalanır:

```text
http://localhost:8025
```

Mailpit yalnızca geliştirme ve test amacıyla kullanılmalıdır. Production ortamında gerçek bir SMTP hizmeti yapılandırılmalıdır.

## API Dokümantasyonu

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Backend sağlık kontrolü:

```http
GET /api/health
```

## Test ve Build

Backend testleri:

```powershell
cd project-tracking-manager/backend
.\mvnw.cmd test
```

Frontend production build:

```bash
cd project-tracking-frontend
npm run build
```

## Production Notları

Production ortamına çıkmadan önce:

- Varsayılan PostgreSQL parolası değiştirilmelidir.
- `.env` dosyası Git repository'sine eklenmemelidir.
- `APP_SAMPLE_DATA_ENABLED=false` yapılmalıdır.
- Demo kullanıcıları kaldırılmalıdır.
- Mailpit yerine gerçek SMTP hizmeti kullanılmalıdır.
- `ddl-auto=update` yerine Flyway veya Liquibase tercih edilmelidir.
- Swagger kapatılmalı veya sınırlandırılmalıdır.
- PostgreSQL'in `5432` portu doğrudan internete açılmamalıdır.
- Uygulama HTTPS üzerinden yayınlanmalıdır.
- Frontend bağımlılıklarındaki güvenlik uyarıları kontrollü sürüm yükseltmesiyle giderilmelidir.

## Geliştirici

Ata Burak Savlak
