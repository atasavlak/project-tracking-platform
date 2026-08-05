# Project Tracking Frontend

Project Tracking Manager uygulamasının React tabanlı kullanıcı arayüzüdür. Projelerin, haftalık raporların, iş kalemlerinin, risklerin ve engellerin rol bazlı olarak yönetilmesini sağlar.

## Teknolojiler

- React
- Vite
- React Router
- JavaScript
- CSS
- REST API
- Basic Authentication

## Özellikler

- Kullanıcı girişi
- Rol bazlı yetkilendirme
- Proje yönetimi
- Haftalık rapor yönetimi
- İş kalemi yönetimi
- Risk ve engel yönetimi
- Kullanıcı arama ve sorumlu atama
- Proje yöneticisine göre veri izolasyonu
- CTO ve Admin dashboard erişimi
- Responsive arayüz

## Roller

- `PROJECT_MANAGER`
- `TEAM_LEAD`
- `CTO`
- `ADMIN`

Kullanıcıların görüntüleme, oluşturma, güncelleme ve silme yetkileri rollerine göre belirlenir.

## Gereksinimler

- Node.js
- npm
- Çalışır durumdaki Project Tracking backend uygulaması

Backend varsayılan olarak aşağıdaki adreste çalışır:

```text
http://localhost:8080
```

## Kurulum

Projeyi klonlayın:

```bash
git clone https://github.com/atasavlak/project-tracking-frontend.git
```

Proje klasörüne geçin:

```bash
cd project-tracking-frontend
```

Bağımlılıkları yükleyin:

```bash
npm install
```

Uygulamayı geliştirme ortamında başlatın:

```bash
npm run dev
```

## Build

Production build oluşturmak için:

```bash
npm run build
```

Build sonucunu yerel olarak görüntülemek için:

```bash
npm run preview
```

## Modüller

### Projeler

Projelerin oluşturulması, listelenmesi, detaylarının görüntülenmesi, güncellenmesi ve pasife alınması işlemlerini içerir.

### Haftalık Raporlar

Projelerin haftalık durum, tamamlanan çalışma, gelecek hafta planı ve risk bilgilerinin yönetilmesini sağlar.

### İş Kalemleri

Haftalık raporlara bağlı planlanan ve tamamlanan işlerin takip edilmesini sağlar.

### Riskler ve Engeller

Risk ve engellerin önem seviyesi, durumu, takip tarihi ve sorumlu kullanıcı bilgileriyle yönetilmesini sağlar.

### Dashboard

CTO ve Admin rollerinin proje, rapor, iş kalemi ve risk verilerini özet olarak görüntülemesini sağlar.

## Backend

Frontend uygulamasının çalışması için backend servisinin de çalışıyor olması gerekir.

```text
http://localhost:8080
```

## Geliştirici

Ata Burak Savlak
