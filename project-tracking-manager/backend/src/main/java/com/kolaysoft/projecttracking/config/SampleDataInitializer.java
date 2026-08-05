package com.kolaysoft.projecttracking.config;

import com.kolaysoft.projecttracking.entity.ActionItem;
import com.kolaysoft.projecttracking.entity.ActionItemPriority;
import com.kolaysoft.projecttracking.entity.ActionItemStatus;
import com.kolaysoft.projecttracking.entity.DecisionLog;
import com.kolaysoft.projecttracking.entity.DecisionStatus;
import com.kolaysoft.projecttracking.entity.Project;
import com.kolaysoft.projecttracking.entity.ProjectStatus;
import com.kolaysoft.projecttracking.entity.RiskIssue;
import com.kolaysoft.projecttracking.entity.RiskIssueSeverity;
import com.kolaysoft.projecttracking.entity.RiskIssueStatus;
import com.kolaysoft.projecttracking.entity.RiskIssueType;
import com.kolaysoft.projecttracking.entity.WeeklyReport;
import com.kolaysoft.projecttracking.entity.WeeklyReportStatus;
import com.kolaysoft.projecttracking.entity.WorkItem;
import com.kolaysoft.projecttracking.entity.WorkItemStatus;
import com.kolaysoft.projecttracking.repository.ActionItemRepository;
import com.kolaysoft.projecttracking.repository.AppUserRepository;
import com.kolaysoft.projecttracking.repository.DecisionLogRepository;
import com.kolaysoft.projecttracking.repository.ProjectRepository;
import com.kolaysoft.projecttracking.repository.RiskIssueRepository;
import com.kolaysoft.projecttracking.repository.WeeklyReportRepository;
import com.kolaysoft.projecttracking.repository.WorkItemRepository;
import com.kolaysoft.projecttracking.user.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Order(2)
@Component
@ConditionalOnProperty(
        name = "app.sample-data.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class SampleDataInitializer implements CommandLineRunner {

    private static final Logger log =
            LoggerFactory.getLogger(SampleDataInitializer.class);

    private final AppUserRepository appUserRepository;
    private final ProjectRepository projectRepository;
    private final WeeklyReportRepository weeklyReportRepository;
    private final WorkItemRepository workItemRepository;
    private final RiskIssueRepository riskIssueRepository;
    private final DecisionLogRepository decisionLogRepository;
    private final ActionItemRepository actionItemRepository;

    public SampleDataInitializer(
            AppUserRepository appUserRepository,
            ProjectRepository projectRepository,
            WeeklyReportRepository weeklyReportRepository,
            WorkItemRepository workItemRepository,
            RiskIssueRepository riskIssueRepository,
            DecisionLogRepository decisionLogRepository,
            ActionItemRepository actionItemRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.projectRepository = projectRepository;
        this.weeklyReportRepository = weeklyReportRepository;
        this.workItemRepository = workItemRepository;
        this.riskIssueRepository = riskIssueRepository;
        this.decisionLogRepository = decisionLogRepository;
        this.actionItemRepository = actionItemRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (projectRepository.count() > 0
                || weeklyReportRepository.count() > 0
                || workItemRepository.count() > 0
                || riskIssueRepository.count() > 0
                || decisionLogRepository.count() > 0
                || actionItemRepository.count() > 0) {

            log.info(
                    "Veritabanında mevcut proje verisi bulunduğu için örnek veriler oluşturulmadı."
            );

            return;
        }

        LocalDate currentWeekStart =
                LocalDate.now().with(
                        TemporalAdjusters.previousOrSame(
                                DayOfWeek.MONDAY
                        )
                );

        LocalDate currentWeekEnd =
                currentWeekStart.plusDays(6);

        LocalDate previousWeekStart =
                currentWeekStart.minusWeeks(1);

        LocalDate previousWeekEnd =
                previousWeekStart.plusDays(6);

        AppUser manager = getActiveUser("manager");
        AppUser manager2 = getActiveUser("manager2");
        AppUser teamLead = getActiveUser("teamlead");
        AppUser cto = getActiveUser("cto");

        Project trackingProject = createProject(
                "Proje Takip Sistemi",
                "Projelerin, haftalık raporların, iş kalemlerinin, risklerin ve kararların takip edildiği uygulama.",
                manager,
                currentWeekStart.minusMonths(2),
                currentWeekStart.plusMonths(3),
                ProjectStatus.ON_TRACK
        );

        Project integrationProject = createProject(
                "Entegrasyon Yenileme Projesi",
                "Eski dış servis entegrasyonlarının yeni API altyapısına taşınması.",
                manager2,
                currentWeekStart.minusMonths(3),
                currentWeekStart.plusMonths(1),
                ProjectStatus.AT_RISK
        );

        Project mobileProject = createProject(
                "Mobil Bankacılık Modernizasyonu",
                "Mobil uygulamanın kullanıcı deneyimi ve servis katmanının yenilenmesi.",
                manager,
                currentWeekStart.minusMonths(4),
                currentWeekStart.plusWeeks(2),
                ProjectStatus.DELAYED
        );

        createProject(
                "Veri Ambarı Raporlama Projesi",
                "Yönetim raporlarının merkezi veri ambarı üzerinden hazırlanması.",
                manager2,
                currentWeekStart.plusWeeks(2),
                currentWeekStart.plusMonths(6),
                ProjectStatus.PLANNED
        );

        Project identityProject = createProject(
                "Kimlik ve Yetki Dönüşümü",
                "Rol ve yetki yönetiminin merkezi kimlik altyapısına taşınması.",
                manager,
                currentWeekStart.minusWeeks(3),
                currentWeekStart.plusMonths(2),
                ProjectStatus.IN_PROGRESS
        );

        Project completedProject = createProject(
                "Eski Uygulama Kapatma Projesi",
                "Kullanılmayan uygulamaların kapatılması ve arşiv süreçlerinin tamamlanması.",
                manager2,
                currentWeekStart.minusMonths(5),
                previousWeekEnd,
                ProjectStatus.COMPLETED
        );

        Project onHoldProject = createProject(
                "Tedarikçi Geçiş Projesi",
                "Mevcut tedarikçi servislerinden yeni sağlayıcıya geçiş çalışması.",
                manager,
                currentWeekStart.minusMonths(1),
                currentWeekStart.plusMonths(4),
                ProjectStatus.ON_HOLD
        );

        Project inactiveProject = createProject(
                "Arşivlenmiş Demo Projesi",
                "Soft delete ve aktif kayıt filtrelerinin test edilmesi için pasif proje.",
                manager2,
                currentWeekStart.minusYears(1),
                currentWeekStart.minusMonths(8),
                ProjectStatus.COMPLETED
        );

        inactiveProject.setActive(false);
        projectRepository.save(inactiveProject);

        WeeklyReport trackingPreviousReport = createWeeklyReport(
                trackingProject,
                previousWeekStart,
                previousWeekEnd,
                WeeklyReportStatus.ON_TRACK,
                "Temel proje ve haftalık rapor modülleri planlanan takvime göre tamamlandı.",
                "Project ve WeeklyReport servisleri geliştirildi.",
                "İş kalemi, risk ve karar ekranlarına devam edilecek.",
                "Kritik bir risk bulunmamaktadır."
        );

        WeeklyReport trackingCurrentReport = createWeeklyReport(
                trackingProject,
                currentWeekStart,
                currentWeekEnd,
                WeeklyReportStatus.ON_TRACK,
                "Frontend ve backend modülleri birlikte çalışmakta, geliştirme planına uygun ilerlemektedir.",
                "Risk, karar ve dashboard entegrasyonları tamamlandı.",
                "Aksiyon kayıtları, çıktı alma ve regresyon testleri hazırlanacak.",
                "Paket bağımlılıklarının lokal ortamda doğrulanması gerekmektedir."
        );

        WeeklyReport integrationPreviousReport = createWeeklyReport(
                integrationProject,
                previousWeekStart,
                previousWeekEnd,
                WeeklyReportStatus.AT_RISK,
                "Servis envanteri çıkarıldı ancak erişim bilgilerinde gecikme yaşandı.",
                "Mevcut servisler ve bağımlılıklar dokümante edildi.",
                "Test ortamı bağlantıları kurulacak.",
                "Dış servis erişimlerinin gecikmesi takvimi etkileyebilir."
        );

        WeeklyReport integrationCurrentReport = createWeeklyReport(
                integrationProject,
                currentWeekStart,
                currentWeekEnd,
                WeeklyReportStatus.AT_RISK,
                "Kritik entegrasyonlarda bağlantı ve veri sözleşmesi problemleri bulunmaktadır.",
                "İki servis yeni altyapıya taşındı.",
                "Kalan servisler için geçiş sırası netleştirilecek.",
                "Test ortamı erişimi, sertifika ve veri formatı uyuşmazlığı devam etmektedir."
        );

        WeeklyReport mobileCurrentReport = createWeeklyReport(
                mobileProject,
                currentWeekStart,
                currentWeekEnd,
                WeeklyReportStatus.DELAYED,
                "Mobil uygulama testleri planın gerisinde kalmıştır.",
                "Yeni giriş ekranı ve servis istemcisi tamamlandı.",
                "Kritik cihaz testleri ve mağaza hazırlıkları tamamlanacak.",
                "Test cihazı eksikliği ve servis performansı gecikmeye neden olmaktadır."
        );

        WeeklyReport identityCurrentReport = createWeeklyReport(
                identityProject,
                currentWeekStart,
                currentWeekEnd,
                WeeklyReportStatus.ON_TRACK,
                "Merkezi rol ve yetki dönüşümü geliştirme aşamasındadır.",
                "Rol matrisi ve örnek kullanıcı senaryoları tamamlandı.",
                "Yetki geçişleri ve negatif testler uygulanacak.",
                "Eski rol kodlarının yeni yapıya eşlenmesinde düşük seviyeli risk bulunmaktadır."
        );

        WeeklyReport completedCurrentReport = createWeeklyReport(
                completedProject,
                currentWeekStart,
                currentWeekEnd,
                WeeklyReportStatus.ON_TRACK,
                "Kapatma ve arşivleme işlemleri tamamlanmıştır.",
                "Veri arşivi, yönlendirme ve kullanıcı bilgilendirmeleri tamamlandı.",
                "Kapanış dokümanı arşivlenecek.",
                "Açık risk bulunmamaktadır."
        );

        WeeklyReport onHoldCurrentReport = createWeeklyReport(
                onHoldProject,
                currentWeekStart,
                currentWeekEnd,
                WeeklyReportStatus.AT_RISK,
                "Sözleşme görüşmeleri nedeniyle proje geçici olarak beklemeye alınmıştır.",
                "Teknik uygunluk analizi tamamlandı.",
                "Tedarikçi kararı sonrasında geçiş planı güncellenecek.",
                "Sözleşme ve lisans koşulları netleşmemiştir."
        );

        WeeklyReport inactiveReport = createWeeklyReport(
                identityProject,
                previousWeekStart.minusWeeks(1),
                previousWeekEnd.minusWeeks(1),
                WeeklyReportStatus.DELAYED,
                "Pasif haftalık rapor filtre test kaydı.",
                "Geçmiş test verisi oluşturuldu.",
                "Aktif listelerde görünmemelidir.",
                "Bu kayıt soft delete testi içindir."
        );

        inactiveReport.setActive(false);
        weeklyReportRepository.save(inactiveReport);

        createWorkItem(
                trackingPreviousReport,
                "Project CRUD geliştirmesi",
                WorkItemStatus.COMPLETED,
                "Ata Burak Savlak",
                "Project CRUD endpointlerinin hazırlanması.",
                "CRUD, PATCH, filtreleme ve yetki kontrolleri tamamlandı.",
                "Önceki hafta tamamlanan iş kalemi."
        );

        createWorkItem(
                trackingPreviousReport,
                "WeeklyReport CRUD geliştirmesi",
                WorkItemStatus.COMPLETED,
                "Ata Burak Savlak",
                "Haftalık rapor API işlemlerinin hazırlanması.",
                "CRUD, PATCH ve filtreleme işlemleri tamamlandı.",
                "Önceki hafta tamamlanan iş kalemi."
        );

        createWorkItem(
                trackingCurrentReport,
                "Risk ve engel ekranı",
                WorkItemStatus.COMPLETED,
                "Ata Burak Savlak",
                "Risk ve engel kayıtlarının uçtan uca yönetilmesi.",
                "Listeleme, filtreleme, atama ve güncelleme tamamlandı.",
                "Güncel rapordaki tamamlanmış iş kalemi."
        );

        createWorkItem(
                trackingCurrentReport,
                "Karar kayıtları ekranı",
                WorkItemStatus.COMPLETED,
                "Ata Burak Savlak",
                "Decision Log backend ve frontend modüllerinin hazırlanması.",
                "CRUD, filtreleme ve rol kontrolleri tamamlandı.",
                "Güncel rapordaki tamamlanmış iş kalemi."
        );

        createWorkItem(
                trackingCurrentReport,
                "Regresyon testlerinin hazırlanması",
                WorkItemStatus.IN_PROGRESS,
                "Test Ekibi",
                "Tüm modüller için rol ve validasyon testlerinin hazırlanması.",
                "Pozitif senaryoların bir kısmı tamamlandı.",
                "Devam eden iş kalemi."
        );

        createWorkItem(
                trackingCurrentReport,
                "Aksiyon takip modülü",
                WorkItemStatus.PLANNED,
                "Ata Burak Savlak",
                "Action Item modülünün analiz ve geliştirmesinin yapılması.",
                null,
                "Planlanan iş kalemi."
        );

        WorkItem inactiveWorkItem = createWorkItem(
                trackingCurrentReport,
                "Pasif test iş kalemi",
                WorkItemStatus.BLOCKED,
                "Test Kullanıcısı",
                "Soft delete görünürlük kontrolünün yapılması.",
                null,
                "Aktif listelerde ve dashboard sayılarında görünmemelidir."
        );

        inactiveWorkItem.setActive(false);
        workItemRepository.save(inactiveWorkItem);

        createWorkItem(
                integrationPreviousReport,
                "Servis envanterinin çıkarılması",
                WorkItemStatus.COMPLETED,
                "Entegrasyon Ekibi",
                "Mevcut servislerin ve bağımlılıkların listelenmesi.",
                "Servis envanteri tamamlandı.",
                "Önceki hafta tamamlandı."
        );

        createWorkItem(
                integrationPreviousReport,
                "Test ortamı hazırlığı",
                WorkItemStatus.IN_PROGRESS,
                "Altyapı Ekibi",
                "Test ortamı ağ ve erişim tanımlarının hazırlanması.",
                "Ağ tanımlarının bir kısmı tamamlandı.",
                "Önceki haftadan devam etmektedir."
        );

        createWorkItem(
                integrationCurrentReport,
                "Dış servis erişimlerinin alınması",
                WorkItemStatus.BLOCKED,
                "Entegrasyon Ekibi",
                "Dış servis kullanıcı ve sertifika bilgilerinin alınması.",
                null,
                "Erişim bilgileri beklenmektedir."
        );

        createWorkItem(
                integrationCurrentReport,
                "Veri sözleşmelerinin uyarlanması",
                WorkItemStatus.AT_RISK,
                "Backend Ekibi",
                "Eski ve yeni veri modelleri arasındaki dönüşümlerin hazırlanması.",
                "Ana alan eşlemeleri tamamlandı.",
                "Bazı alanların karşılığı henüz net değildir."
        );

        createWorkItem(
                integrationCurrentReport,
                "Sertifika yenileme işlemi",
                WorkItemStatus.DELAYED,
                "Altyapı Ekibi",
                "Süresi dolan entegrasyon sertifikalarının yenilenmesi.",
                "Talep açıldı ancak onay bekleniyor.",
                "Planlanan tarihin gerisindedir."
        );

        createWorkItem(
                integrationCurrentReport,
                "İlk servis geçişi",
                WorkItemStatus.COMPLETED,
                "Entegrasyon Ekibi",
                "Düşük riskli servisin yeni altyapıya taşınması.",
                "Servis geçişi ve smoke test tamamlandı.",
                "Tamamlanmış örnek entegrasyon işi."
        );

        createWorkItem(
                mobileCurrentReport,
                "Android cihaz testleri",
                WorkItemStatus.DELAYED,
                "Mobil Test Ekibi",
                "Desteklenen Android sürümlerinde regresyon testi yapılması.",
                "Testlerin yarısı tamamlandı.",
                "Cihaz eksikliği nedeniyle gecikmiştir."
        );

        createWorkItem(
                mobileCurrentReport,
                "iOS performans testi",
                WorkItemStatus.DELAYED,
                "Mobil Test Ekibi",
                "Düşük donanımlı cihazlarda performans ölçümü yapılması.",
                "Profiling başlatıldı.",
                "Hedef tarihin gerisindedir."
        );

        createWorkItem(
                mobileCurrentReport,
                "Mağaza sertifika işlemleri",
                WorkItemStatus.BLOCKED,
                "Yayın Ekibi",
                "Uygulama mağazası sertifika ve hesap tanımlarının tamamlanması.",
                null,
                "Kurumsal hesap onayı beklenmektedir."
        );

        createWorkItem(
                mobileCurrentReport,
                "Yeni giriş ekranı",
                WorkItemStatus.COMPLETED,
                "Mobil Geliştirme Ekibi",
                "Yeni kimlik doğrulama ekranının geliştirilmesi.",
                "Geliştirme ve temel testler tamamlandı.",
                "Tamamlanmış mobil iş kalemi."
        );

        createWorkItem(
                identityCurrentReport,
                "Yeni rol matrisi",
                WorkItemStatus.PLANNED,
                "Analiz Ekibi",
                "Yeni sistem için rol ve yetki matrisinin onaylanması.",
                null,
                "Planlanan senaryo."
        );

        createWorkItem(
                identityCurrentReport,
                "Yetki servisinin geliştirilmesi",
                WorkItemStatus.IN_PROGRESS,
                "Backend Ekibi",
                "Merkezi yetki sorgulama servisinin hazırlanması.",
                "Temel servis ve cache yapısı tamamlandı.",
                "Devam eden senaryo."
        );

        createWorkItem(
                identityCurrentReport,
                "Eski rol kodlarının analizi",
                WorkItemStatus.COMPLETED,
                "Analiz Ekibi",
                "Eski sistemdeki rol kodlarının kullanım alanlarının çıkarılması.",
                "Analiz dokümanı tamamlandı.",
                "Tamamlanan senaryo."
        );

        createWorkItem(
                identityCurrentReport,
                "Geçiş tarihi planlaması",
                WorkItemStatus.DELAYED,
                "Proje Yönetimi",
                "Yetki geçişi için canlı tarihinin netleştirilmesi.",
                "Taslak tarih hazırlandı.",
                "Bağımlı ekiplerden onay beklenmektedir."
        );

        createWorkItem(
                identityCurrentReport,
                "Yönetici yetki testi",
                WorkItemStatus.AT_RISK,
                "Test Ekibi",
                "Yönetici rollerinin negatif ve pozitif testlerinin yapılması.",
                "Pozitif testler tamamlandı.",
                "Negatif senaryolarda eksik kapsam bulunmaktadır."
        );

        createWorkItem(
                identityCurrentReport,
                "Dizin servisi bağlantısı",
                WorkItemStatus.BLOCKED,
                "Altyapı Ekibi",
                "Kurumsal dizin servisi bağlantısının açılması.",
                null,
                "Firewall talebi beklenmektedir."
        );

        createWorkItem(
                completedCurrentReport,
                "Veri arşivleme",
                WorkItemStatus.COMPLETED,
                "Veri Ekibi",
                "Kapanan uygulama verilerinin arşivlenmesi.",
                "Arşivleme tamamlandı ve doğrulandı.",
                "Tamamlanmış proje iş kalemi."
        );

        createWorkItem(
                completedCurrentReport,
                "Kullanıcı yönlendirmesi",
                WorkItemStatus.COMPLETED,
                "Uygulama Ekibi",
                "Eski adreslerin yeni uygulamaya yönlendirilmesi.",
                "Yönlendirmeler aktif edildi.",
                "Tamamlanmış proje iş kalemi."
        );

        createWorkItem(
                completedCurrentReport,
                "Kapanış dokümanı",
                WorkItemStatus.COMPLETED,
                "Proje Yönetimi",
                "Proje kapanış dokümanının hazırlanması.",
                "Doküman onaylandı.",
                "Tamamlanmış proje iş kalemi."
        );

        createWorkItem(
                onHoldCurrentReport,
                "Tedarikçi kararının beklenmesi",
                WorkItemStatus.PLANNED,
                "Satın Alma Ekibi",
                "Teknik ve ticari değerlendirmenin sonuçlandırılması.",
                null,
                "Karar sonrasında başlanacaktır."
        );

        createWorkItem(
                onHoldCurrentReport,
                "Lisans koşullarının netleştirilmesi",
                WorkItemStatus.BLOCKED,
                "Hukuk Ekibi",
                "Yeni lisans ve sözleşme koşullarının onaylanması.",
                null,
                "Sözleşme görüşmesi beklenmektedir."
        );

        createRiskIssue(
                trackingCurrentReport,
                manager,
                RiskIssueType.RISK,
                "Paket bağımlılığı kurulumu",
                "Frontend paketlerinden birinin registry üzerinden indirilememesi lokal build sürecini etkileyebilir.",
                RiskIssueSeverity.LOW,
                RiskIssueStatus.RESOLVED,
                currentWeekStart.plusDays(1),
                "Alternatif registry ve lokal cache kullanılarak sorun giderildi."
        );

        createRiskIssue(
                trackingCurrentReport,
                teamLead,
                RiskIssueType.ISSUE,
                "Regresyon senaryolarının eksik olması",
                "Yeni modüller için bütün rol ve negatif test senaryoları henüz tamamlanmamıştır.",
                RiskIssueSeverity.MEDIUM,
                RiskIssueStatus.OPEN,
                currentWeekStart.plusDays(4),
                null
        );

        RiskIssue inactiveRiskIssue = createRiskIssue(
                trackingCurrentReport,
                manager,
                RiskIssueType.RISK,
                "Pasif risk kaydı",
                "Soft delete görünürlük kontrolü için oluşturulmuştur.",
                RiskIssueSeverity.CRITICAL,
                RiskIssueStatus.OPEN,
                currentWeekEnd,
                null
        );

        inactiveRiskIssue.setActive(false);
        riskIssueRepository.save(inactiveRiskIssue);

        createRiskIssue(
                integrationCurrentReport,
                manager,
                RiskIssueType.RISK,
                "Dış servis erişim gecikmesi",
                "Erişim bilgilerinin teslim edilmemesi entegrasyon takvimini riske atmaktadır.",
                RiskIssueSeverity.HIGH,
                RiskIssueStatus.OPEN,
                currentWeekStart.plusDays(2),
                null
        );

        createRiskIssue(
                integrationCurrentReport,
                manager2,
                RiskIssueType.ISSUE,
                "Sertifika doğrulama hatası",
                "Test ortamında istemci sertifikası doğrulanamadığı için servis çağrısı yapılamamaktadır.",
                RiskIssueSeverity.CRITICAL,
                RiskIssueStatus.IN_PROGRESS,
                currentWeekStart.plusDays(1),
                "Altyapı ve entegrasyon ekipleri birlikte incelemektedir."
        );

        createRiskIssue(
                integrationCurrentReport,
                teamLead,
                RiskIssueType.RISK,
                "Alan eşleme belirsizliği",
                "Bazı eski alanların yeni veri sözleşmesinde doğrudan karşılığı bulunmamaktadır.",
                RiskIssueSeverity.MEDIUM,
                RiskIssueStatus.CLOSED,
                currentWeekStart.plusDays(3),
                "Alanlar için dönüşüm tablosu hazırlanarak karar kapatıldı."
        );

        createRiskIssue(
                mobileCurrentReport,
                manager,
                RiskIssueType.ISSUE,
                "Test cihazı yetersizliği",
                "Desteklenen cihaz çeşitliliği sağlanamadığı için regresyon kapsamı tamamlanamamaktadır.",
                RiskIssueSeverity.CRITICAL,
                RiskIssueStatus.OPEN,
                currentWeekStart.plusDays(1),
                null
        );

        createRiskIssue(
                mobileCurrentReport,
                manager2,
                RiskIssueType.RISK,
                "Servis yanıt süresi",
                "Yoğun saatlerde bazı servislerin yanıt süresi hedef değerin üzerine çıkmaktadır.",
                RiskIssueSeverity.HIGH,
                RiskIssueStatus.IN_PROGRESS,
                currentWeekStart.plusDays(5),
                "Performans ölçümleri ve sorgu analizi devam etmektedir."
        );

        createRiskIssue(
                identityCurrentReport,
                teamLead,
                RiskIssueType.RISK,
                "Eski rol kodu uyumsuzluğu",
                "Bazı eski roller yeni sistemde birebir karşılık bulamayabilir.",
                RiskIssueSeverity.LOW,
                RiskIssueStatus.OPEN,
                currentWeekEnd,
                null
        );

        createRiskIssue(
                identityCurrentReport,
                manager,
                RiskIssueType.ISSUE,
                "Firewall erişiminin kapalı olması",
                "Kurumsal dizin servisine test ortamından bağlantı kurulamamaktadır.",
                RiskIssueSeverity.MEDIUM,
                RiskIssueStatus.RESOLVED,
                currentWeekStart.plusDays(2),
                "Firewall kuralı açıldı ve bağlantı doğrulandı."
        );

        createRiskIssue(
                completedCurrentReport,
                manager2,
                RiskIssueType.RISK,
                "Arşiv doğrulama riski",
                "Kapanış öncesinde arşiv kayıtlarının bütünlüğü kontrol edilmelidir.",
                RiskIssueSeverity.LOW,
                RiskIssueStatus.CLOSED,
                previousWeekEnd,
                "Arşiv bütünlüğü kontrol edildi ve kayıt kapatıldı."
        );

        createRiskIssue(
                onHoldCurrentReport,
                teamLead,
                RiskIssueType.ISSUE,
                "Sözleşme koşullarının netleşmemesi",
                "Teknik çalışmanın başlaması için gerekli lisans ve sözleşme koşulları onaylanmamıştır.",
                RiskIssueSeverity.HIGH,
                RiskIssueStatus.OPEN,
                currentWeekStart.plusWeeks(1),
                null
        );

        createDecisionLog(
                trackingProject,
                trackingCurrentReport,
                manager,
                "Dashboard ilerleme oranının otomatik hesaplanması",
                "Proje ilerleme oranının son aktif haftalık rapordaki tamamlanan iş kalemleri üzerinden hesaplanmasına karar verildi.",
                currentWeekStart,
                DecisionStatus.APPROVED,
                "Tamamlanan iş sayısı toplam aktif iş sayısına bölünecektir."
        );

        createDecisionLog(
                trackingProject,
                null,
                teamLead,
                "Soft delete yaklaşımının kullanılması",
                "Proje verilerinin fiziksel olarak silinmesi yerine active alanı ile pasife alınmasına karar verildi.",
                previousWeekStart.plusDays(2),
                DecisionStatus.IMPLEMENTED,
                "Liste ve dashboard sorguları yalnızca aktif kayıtları getirmektedir."
        );

        DecisionLog inactiveDecisionLog = createDecisionLog(
                trackingProject,
                trackingCurrentReport,
                manager,
                "Pasif karar kaydı",
                "Soft delete görünürlük kontrolü için oluşturulmuştur.",
                currentWeekStart.plusDays(1),
                DecisionStatus.DRAFT,
                null
        );

        inactiveDecisionLog.setActive(false);
        decisionLogRepository.save(inactiveDecisionLog);

        createDecisionLog(
                integrationProject,
                integrationCurrentReport,
                manager,
                "Servis geçiş sırasının değiştirilmesi",
                "Manager kullanıcısına başka proje üzerinden atanmış karar görünürlüğünü test etmek için düşük riskli servisin önce taşınmasına karar taslağı oluşturuldu.",
                currentWeekStart.plusDays(1),
                DecisionStatus.DRAFT,
                "Manager kendi projesi dışında sahibi olduğu bu kararı görebilmelidir."
        );

        createDecisionLog(
                integrationProject,
                integrationCurrentReport,
                manager2,
                "Eski sertifikanın kullanımının iptali",
                "Güvenlik nedeniyle süresi dolan sertifika ile geçici bağlantı kurulması önerisi reddedildi.",
                currentWeekStart.plusDays(2),
                DecisionStatus.CANCELLED,
                "Yeni sertifika beklenerek güvenlik standardı korunacaktır."
        );

        createDecisionLog(
                mobileProject,
                mobileCurrentReport,
                cto,
                "Canlıya geçiş tarihinin ertelenmesi",
                "Kritik cihaz testleri tamamlanmadığı için canlıya geçiş tarihinin bir hafta ertelenmesine karar verildi.",
                currentWeekStart.plusDays(1),
                DecisionStatus.APPROVED,
                "Yeni tarih ilgili ekiplerle paylaşılacaktır."
        );

        createDecisionLog(
                identityProject,
                identityCurrentReport,
                manager,
                "Rol eşleme tablosunun zorunlu olması",
                "Canlı geçiş öncesinde bütün eski roller için onaylı eşleme tablosu hazırlanmasına karar verildi.",
                currentWeekStart.minusDays(1),
                DecisionStatus.IMPLEMENTED,
                "Eşleme tablosu analiz ekibi tarafından tamamlandı."
        );

        createDecisionLog(
                completedProject,
                completedCurrentReport,
                manager2,
                "Eski uygulamanın erişime kapatılması",
                "Arşiv ve yönlendirme işlemlerinin tamamlanmasının ardından eski uygulamanın erişime kapatılmasına karar verildi.",
                previousWeekEnd,
                DecisionStatus.IMPLEMENTED,
                "Kapanış işlemi başarıyla uygulandı."
        );

        createDecisionLog(
                onHoldProject,
                onHoldCurrentReport,
                teamLead,
                "Teknik geliştirmenin bekletilmesi",
                "Tedarikçi ve lisans kararı netleşene kadar teknik geliştirmenin başlatılmamasına karar taslağı oluşturuldu.",
                currentWeekStart.plusDays(3),
                DecisionStatus.DRAFT,
                "Karar satın alma ve hukuk değerlendirmesi sonrasında güncellenecektir."
        );

        createActionItem(
                trackingProject,
                trackingCurrentReport,
                manager,
                "ActionItem modülünün backend geliştirmesini tamamla",
                "Aksiyon CRUD, filtreleme, rol kontrolleri ve otomatik gecikme yönetimi tamamlanacaktır.",
                ActionItemPriority.HIGH,
                ActionItemStatus.IN_PROGRESS,
                currentWeekStart.plusDays(4),
                null,
                "Swagger üzerinden pozitif ve negatif senaryolar test edilecektir."
        );

        createActionItem(
                trackingProject,
                trackingCurrentReport,
                teamLead,
                "Frontend aksiyon takip ekranını hazırla",
                "Aksiyon listesi, filtreler, detay, oluşturma, güncelleme ve silme akışları hazırlanacaktır.",
                ActionItemPriority.HIGH,
                ActionItemStatus.OPEN,
                currentWeekStart.plusDays(6),
                null,
                "Rol bazlı buton görünürlüğü backend yetkileriyle uyumlu olmalıdır."
        );

        createActionItem(
                trackingProject,
                trackingPreviousReport,
                manager,
                "Risk görünürlük testlerini tamamla",
                "Proje sahibi ve sorumlu kullanıcı senaryoları test edilmiştir.",
                ActionItemPriority.MEDIUM,
                ActionItemStatus.COMPLETED,
                previousWeekEnd,
                previousWeekEnd.minusDays(1),
                "Manager2 hesabıyla çapraz sorumluluk testi başarılıdır."
        );

        createActionItem(
                trackingProject,
                trackingCurrentReport,
                manager2,
                "Geciken paket bağımlılığı problemini incele",
                "Frontend build sırasında registry üzerinden alınamayan paketin alternatifini ve sürümünü kontrol et.",
                ActionItemPriority.CRITICAL,
                ActionItemStatus.OVERDUE,
                currentWeekStart.minusDays(2),
                null,
                "Bu kayıt otomatik gecikme durumunu test eder."
        );

        createActionItem(
                integrationProject,
                integrationCurrentReport,
                manager,
                "Yeni servis sertifikasını temin et",
                "Test ortamında bağlantı kurulabilmesi için geçerli istemci sertifikası hazırlanacaktır.",
                ActionItemPriority.CRITICAL,
                ActionItemStatus.OVERDUE,
                currentWeekStart.minusDays(1),
                null,
                "Manager kullanıcısına başka proje üzerinden atanmış aksiyon görünürlüğünü test eder."
        );

        createActionItem(
                integrationProject,
                integrationCurrentReport,
                manager2,
                "Veri sözleşmesi uyuşmazlıklarını kapat",
                "Eski ve yeni servis cevap modelleri karşılaştırılarak dönüşüm kuralları netleştirilecektir.",
                ActionItemPriority.HIGH,
                ActionItemStatus.IN_PROGRESS,
                currentWeekStart.plusDays(3),
                null,
                null
        );

        createActionItem(
                mobileProject,
                mobileCurrentReport,
                teamLead,
                "Kritik cihaz testlerini tamamla",
                "Desteklenen cihaz ve işletim sistemi matrisindeki kritik senaryolar çalıştırılacaktır.",
                ActionItemPriority.CRITICAL,
                ActionItemStatus.OPEN,
                currentWeekStart.plusDays(2),
                null,
                "Canlıya geçiş kararı bu aksiyonun sonucuna bağlıdır."
        );

        createActionItem(
                identityProject,
                identityCurrentReport,
                manager,
                "Rol eşleme tablosunu doğrula",
                "Eski ve yeni rol kodlarının birebir eşleştiği negatif senaryolarla doğrulanacaktır.",
                ActionItemPriority.HIGH,
                ActionItemStatus.COMPLETED,
                currentWeekStart.plusDays(1),
                currentWeekStart,
                "Kontrol listesi ekip lideri tarafından onaylandı."
        );

        createActionItem(
                completedProject,
                completedCurrentReport,
                manager2,
                "Kapanış dokümanını arşivle",
                "Proje kapanış dokümanı merkezi doküman alanına yüklenmiştir.",
                ActionItemPriority.LOW,
                ActionItemStatus.COMPLETED,
                currentWeekStart,
                currentWeekStart.minusDays(1),
                null
        );

        createActionItem(
                onHoldProject,
                onHoldCurrentReport,
                teamLead,
                "Tedarikçi teknik toplantısını planla",
                "Sözleşme koşulları netleşmediği için teknik toplantı iptal edilmiştir.",
                ActionItemPriority.MEDIUM,
                ActionItemStatus.CANCELLED,
                currentWeekStart.plusWeeks(1),
                null,
                "Yeni tedarikçi kararı sonrasında tekrar açılacaktır."
        );

        ActionItem inactiveActionItem = createActionItem(
                trackingProject,
                trackingCurrentReport,
                manager,
                "Pasif aksiyon kaydı",
                "Soft delete görünürlük kontrolü için oluşturulmuştur.",
                ActionItemPriority.LOW,
                ActionItemStatus.OPEN,
                currentWeekStart.plusDays(5),
                null,
                null
        );

        inactiveActionItem.setActive(false);
        actionItemRepository.save(inactiveActionItem);

        log.info(
                "Örnek veriler oluşturuldu: 8 proje, 9 haftalık rapor, 28 iş kalemi, 12 risk/engel, 9 karar ve 11 aksiyon kaydı."
        );
    }

    private AppUser getActiveUser(
            String username
    ) {
        return appUserRepository
                .findByUsernameIgnoreCaseAndActiveTrue(
                        username
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Örnek veri için aktif kullanıcı bulunamadı: "
                                        + username
                        )
                );
    }

    private Project createProject(
            String name,
            String description,
            AppUser projectManager,
            LocalDate startDate,
            LocalDate endDate,
            ProjectStatus status
    ) {
        Project project = new Project();

        project.setName(name);
        project.setDescription(description);
        project.setProjectManager(projectManager);
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        project.setStatus(status);

        return projectRepository.save(project);
    }

    private WeeklyReport createWeeklyReport(
            Project project,
            LocalDate weekStartDate,
            LocalDate weekEndDate,
            WeeklyReportStatus status,
            String summary,
            String completedWork,
            String nextWeekPlan,
            String risks
    ) {
        WeeklyReport weeklyReport = new WeeklyReport();

        weeklyReport.setProject(project);
        weeklyReport.setWeekStartDate(weekStartDate);
        weeklyReport.setWeekEndDate(weekEndDate);
        weeklyReport.setStatus(status);
        weeklyReport.setSummary(summary);
        weeklyReport.setCompletedWork(completedWork);
        weeklyReport.setNextWeekPlan(nextWeekPlan);
        weeklyReport.setRisks(risks);

        return weeklyReportRepository.save(weeklyReport);
    }

    private WorkItem createWorkItem(
            WeeklyReport weeklyReport,
            String title,
            WorkItemStatus status,
            String responsiblePerson,
            String plannedWork,
            String completedWork,
            String description
    ) {
        WorkItem workItem = new WorkItem();

        workItem.setWeeklyReport(weeklyReport);
        workItem.setTitle(title);
        workItem.setStatus(status);
        workItem.setResponsiblePerson(responsiblePerson);
        workItem.setPlannedWork(plannedWork);
        workItem.setCompletedWork(completedWork);
        workItem.setDescription(description);

        return workItemRepository.save(workItem);
    }

    private RiskIssue createRiskIssue(
            WeeklyReport weeklyReport,
            AppUser responsibleUser,
            RiskIssueType type,
            String title,
            String description,
            RiskIssueSeverity severity,
            RiskIssueStatus status,
            LocalDate followUpDate,
            String resolutionNote
    ) {
        RiskIssue riskIssue = new RiskIssue();

        riskIssue.setWeeklyReport(weeklyReport);
        riskIssue.setResponsibleUser(responsibleUser);
        riskIssue.setType(type);
        riskIssue.setTitle(title);
        riskIssue.setDescription(description);
        riskIssue.setSeverity(severity);
        riskIssue.setStatus(status);
        riskIssue.setFollowUpDate(followUpDate);
        riskIssue.setResolutionNote(resolutionNote);

        return riskIssueRepository.save(riskIssue);
    }

    private DecisionLog createDecisionLog(
            Project project,
            WeeklyReport weeklyReport,
            AppUser decisionOwner,
            String title,
            String description,
            LocalDate decisionDate,
            DecisionStatus status,
            String note
    ) {
        DecisionLog decisionLog = new DecisionLog();

        decisionLog.setProject(project);
        decisionLog.setWeeklyReport(weeklyReport);
        decisionLog.setDecisionOwner(decisionOwner);
        decisionLog.setTitle(title);
        decisionLog.setDescription(description);
        decisionLog.setDecisionDate(decisionDate);
        decisionLog.setStatus(status);
        decisionLog.setNote(note);

        return decisionLogRepository.save(decisionLog);
    }

    private ActionItem createActionItem(
            Project project,
            WeeklyReport weeklyReport,
            AppUser responsibleUser,
            String title,
            String description,
            ActionItemPriority priority,
            ActionItemStatus status,
            LocalDate targetDate,
            LocalDate completionDate,
            String note
    ) {
        ActionItem actionItem = new ActionItem();

        actionItem.setProject(project);
        actionItem.setWeeklyReport(weeklyReport);
        actionItem.setResponsibleUser(responsibleUser);
        actionItem.setTitle(title);
        actionItem.setDescription(description);
        actionItem.setPriority(priority);
        actionItem.setStatus(status);
        actionItem.setTargetDate(targetDate);
        actionItem.setCompletionDate(completionDate);
        actionItem.setNote(note);

        return actionItemRepository.save(actionItem);
    }
}
