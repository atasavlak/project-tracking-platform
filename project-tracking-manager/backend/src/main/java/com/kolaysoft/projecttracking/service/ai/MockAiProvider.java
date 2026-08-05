package com.kolaysoft.projecttracking.service.ai;

import com.kolaysoft.projecttracking.dto.AiDashboardProjectInsightResponse;
import com.kolaysoft.projecttracking.dto.AiDashboardSummaryResponse;
import com.kolaysoft.projecttracking.dto.AiWeeklyReportAnalysisResponse;
import com.kolaysoft.projecttracking.dto.DashboardProjectResponse;
import com.kolaysoft.projecttracking.dto.DashboardSummaryResponse;
import com.kolaysoft.projecttracking.dto.ProjectHealthStatus;
import com.kolaysoft.projecttracking.entity.WeeklyReportStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class MockAiProvider implements AiProvider {

    private static final int MAX_SUMMARY_LENGTH = 2000;
    private static final int MAX_SUGGESTION_COUNT = 5;
    private static final int MAX_INSIGHT_LENGTH = 240;

    @Override
    public String getProviderName() {
        return "MOCK_RULE_BASED";
    }

    @Override
    public AiWeeklyReportAnalysisResponse analyze(
            AiWeeklyReportAnalysisContext context
    ) {
        AiWeeklyReportAnalysisResponse response =
                new AiWeeklyReportAnalysisResponse();

        WeeklyReportStatus suggestedStatus =
                determineSuggestedStatus(context);

        response.setSuggestedStatus(suggestedStatus);
        response.setExecutiveSummary(
                buildExecutiveSummary(
                        context,
                        suggestedStatus
                )
        );
        response.setDetectedRisks(
                buildDetectedRisks(context)
        );
        response.setSuggestedActions(
                buildSuggestedActions(context)
        );
        response.setIndicators(
                buildIndicators(context)
        );
        response.setTotalWorkItems(
                context.totalWorkItems()
        );
        response.setCompletedWorkItems(
                context.completedWorkItems()
        );
        response.setAtRiskWorkItems(
                context.atRiskWorkItems()
        );
        response.setBlockedWorkItems(
                context.blockedWorkItems()
        );
        response.setDelayedWorkItems(
                context.delayedWorkItems()
        );
        response.setOpenHighRiskIssues(
                context.openHighRiskIssues()
        );
        response.setOpenCriticalRiskIssues(
                context.openCriticalRiskIssues()
        );
        response.setOverdueActionItems(
                context.overdueActionItems()
        );

        return response;
    }

    @Override
    public AiDashboardSummaryResponse summarizeDashboard(
            AiDashboardSummaryContext context
    ) {
        DashboardSummaryResponse summary = context.summary();
        AiDashboardSummaryResponse response =
                new AiDashboardSummaryResponse();

        response.setOverallStatus(
                determinePortfolioStatus(summary)
        );
        response.setExecutiveSummary(
                buildPortfolioExecutiveSummary(context)
        );
        response.setHighlights(
                buildPortfolioHighlights(summary)
        );
        response.setAttentionProjects(
                buildAttentionProjects(context.projects())
        );
        response.setWeeklyReportInsights(
                buildWeeklyReportInsights(context.projects())
        );
        response.setRecommendations(
                buildPortfolioRecommendations(summary)
        );
        response.setAnalyzedProjectCount(
                summary.getTotalActiveProjects()
        );
        response.setAnalyzedWeeklyReportCount(
                summary.getTotalActiveWeeklyReports()
        );
        response.setHealthyProjectCount(
                summary.getHealthyProjects()
        );
        response.setNeedsAttentionProjectCount(
                summary.getNeedsAttentionProjects()
        );
        response.setCriticalProjectCount(
                summary.getCriticalProjects()
        );
        response.setProjectsWithoutReportCount(
                summary.getProjectsWithoutReport()
        );
        response.setOpenRiskIssueCount(
                summary.getOpenRiskIssues()
        );
        response.setCriticalRiskIssueCount(
                summary.getCriticalRiskIssues()
        );
        response.setOverdueActionItemCount(
                summary.getOverdueActionItems()
        );
        response.setCriticalWorkItemCount(
                summary.getRiskyWorkItems()
                        + summary.getBlockedWorkItems()
                        + summary.getDelayedWorkItems()
        );
        response.setAppliedProjectStatus(
                context.appliedProjectStatus()
        );
        response.setAppliedHealthStatus(
                context.appliedHealthStatus()
        );

        return response;
    }

    private WeeklyReportStatus determineSuggestedStatus(
            AiWeeklyReportAnalysisContext context
    ) {
        String combinedText = normalizeForSearch(
                String.join(
                        " ",
                        valueOrEmpty(context.summary()),
                        valueOrEmpty(context.completedWork()),
                        valueOrEmpty(context.nextWeekPlan()),
                        valueOrEmpty(context.risks())
                )
        );

        boolean hasDelaySignal = containsAny(
                combinedText,
                "gecik",
                "takvim sarkt",
                "süre aşı",
                "termin aşı"
        );

        boolean hasRiskSignal = containsAny(
                combinedText,
                "risk",
                "engel",
                "blok",
                "hata",
                "eksik",
                "beklen",
                "performans",
                "erişim"
        );

        if (context.currentStatus()
                == WeeklyReportStatus.DELAYED
                || context.delayedWorkItems() > 0
                || context.overdueActionItems() >= 2
                || hasDelaySignal) {

            return WeeklyReportStatus.DELAYED;
        }

        if (context.currentStatus()
                == WeeklyReportStatus.AT_RISK
                || context.openCriticalRiskIssues() > 0
                || context.openHighRiskIssues() > 0
                || context.blockedWorkItems() > 0
                || context.atRiskWorkItems() > 0
                || context.overdueActionItems() > 0
                || hasText(context.risks())
                || hasRiskSignal) {

            return WeeklyReportStatus.AT_RISK;
        }

        return WeeklyReportStatus.ON_TRACK;
    }

    private String buildExecutiveSummary(
            AiWeeklyReportAnalysisContext context,
            WeeklyReportStatus suggestedStatus
    ) {
        List<String> parts = new ArrayList<>();

        parts.add(
                context.projectName()
                        + " projesi için haftalık değerlendirme "
                        + statusDescription(suggestedStatus)
                        + "."
        );

        if (hasText(context.summary())) {
            parts.add(ensureSentence(context.summary()));
        }

        if (hasText(context.completedWork())) {
            parts.add(
                    "Tamamlanan çalışmalar: "
                            + ensureSentence(
                            context.completedWork()
                    )
            );
        } else if (context.totalWorkItems() > 0) {
            parts.add(
                    "Aktif iş kalemlerinin "
                            + context.completedWorkItems()
                            + " tanesi tamamlandı."
            );
        }

        if (hasText(context.nextWeekPlan())) {
            parts.add(
                    "Gelecek hafta odağı: "
                            + ensureSentence(
                            context.nextWeekPlan()
                    )
            );
        }

        if (hasText(context.risks())) {
            parts.add(
                    "Dikkat edilmesi gereken konular: "
                            + ensureSentence(context.risks())
            );
        } else if (hasOperationalRisk(context)) {
            parts.add(
                    "Açık riskler, gecikmiş aksiyonlar ve kritik iş kalemleri yakından takip edilmelidir."
            );
        }

        return limitText(
                String.join(" ", parts),
                MAX_SUMMARY_LENGTH
        );
    }

    private List<String> buildDetectedRisks(
            AiWeeklyReportAnalysisContext context
    ) {
        Set<String> risks = new LinkedHashSet<>();

        if (hasText(context.risks())) {
            splitSuggestions(context.risks())
                    .forEach(risks::add);
        }

        context.openRiskTitles()
                .stream()
                .filter(this::hasText)
                .forEach(risks::add);

        if (context.blockedWorkItems() > 0) {
            risks.add(
                    context.blockedWorkItems()
                            + " iş kalemi blokeli durumda."
            );
        }

        if (context.delayedWorkItems() > 0) {
            risks.add(
                    context.delayedWorkItems()
                            + " iş kaleminde gecikme bulunuyor."
            );
        }

        if (context.overdueActionItems() > 0) {
            risks.add(
                    context.overdueActionItems()
                            + " aksiyon hedef tarihini geçti."
            );
        }

        if (risks.isEmpty()) {
            risks.add(
                    "Mevcut verilerde belirgin bir kritik risk sinyali tespit edilmedi."
            );
        }

        return risks.stream()
                .limit(MAX_SUGGESTION_COUNT)
                .toList();
    }

    private List<String> buildSuggestedActions(
            AiWeeklyReportAnalysisContext context
    ) {
        Set<String> actions = new LinkedHashSet<>();

        if (context.openCriticalRiskIssues() > 0) {
            actions.add(
                    "Kritik riskler için sorumlu, çözüm planı ve takip tarihi netleştirilmelidir."
            );
        }

        if (context.openHighRiskIssues() > 0) {
            actions.add(
                    "Yüksek önem seviyesindeki riskler haftalık takip toplantısında ayrıca ele alınmalıdır."
            );
        }

        if (context.blockedWorkItems() > 0) {
            actions.add(
                    "Blokeli iş kalemlerinin bağımlılıkları kaldırılmalı ve çözüm sahibi atanmalıdır."
            );
        }

        if (context.delayedWorkItems() > 0) {
            actions.add(
                    "Geciken iş kalemleri için yeni hedef tarih ve telafi planı belirlenmelidir."
            );
        }

        if (context.overdueActionItems() > 0) {
            actions.add(
                    "Gecikmiş aksiyonlar sorumlularıyla gözden geçirilmeli ve kapanış tarihleri güncellenmelidir."
            );
        }

        if (!hasText(context.nextWeekPlan())) {
            actions.add(
                    "Gelecek hafta planı ölçülebilir teslimler ve sorumlularla netleştirilmelidir."
            );
        }

        if (actions.isEmpty()) {
            actions.add(
                    "Mevcut plan sürdürülmeli ve iş kalemleri haftalık olarak izlenmelidir."
            );
        }

        return actions.stream()
                .limit(MAX_SUGGESTION_COUNT)
                .toList();
    }

    private List<String> buildIndicators(
            AiWeeklyReportAnalysisContext context
    ) {
        List<String> indicators = new ArrayList<>();

        indicators.add(
                context.completedWorkItems()
                        + "/"
                        + context.totalWorkItems()
                        + " iş kalemi tamamlandı"
        );

        addIndicator(
                indicators,
                context.atRiskWorkItems(),
                "riskli iş kalemi"
        );
        addIndicator(
                indicators,
                context.blockedWorkItems(),
                "blokeli iş kalemi"
        );
        addIndicator(
                indicators,
                context.delayedWorkItems(),
                "gecikmiş iş kalemi"
        );
        addIndicator(
                indicators,
                context.openHighRiskIssues(),
                "açık yüksek risk"
        );
        addIndicator(
                indicators,
                context.openCriticalRiskIssues(),
                "açık kritik risk"
        );
        addIndicator(
                indicators,
                context.overdueActionItems(),
                "gecikmiş aksiyon"
        );

        return indicators;
    }

    private ProjectHealthStatus determinePortfolioStatus(
            DashboardSummaryResponse summary
    ) {
        if (summary.getTotalActiveProjects() == 0) {
            return ProjectHealthStatus.NO_REPORT;
        }

        if (summary.getCriticalProjects() > 0
                || summary.getCriticalRiskIssues() > 0
                || summary.getOverdueActionItems() > 0
                || summary.getBlockedWorkItems() > 0
                || summary.getDelayedWorkItems() > 0) {

            return ProjectHealthStatus.CRITICAL;
        }

        if (summary.getNeedsAttentionProjects() > 0
                || summary.getProjectsWithoutReport() > 0
                || summary.getOpenRiskIssues() > 0
                || summary.getRiskyWorkItems() > 0) {

            return ProjectHealthStatus.NEEDS_ATTENTION;
        }

        return ProjectHealthStatus.HEALTHY;
    }

    private String buildPortfolioExecutiveSummary(
            AiDashboardSummaryContext context
    ) {
        DashboardSummaryResponse summary = context.summary();

        if (summary.getTotalActiveProjects() == 0) {
            return "Seçilen filtrelere uyan aktif proje bulunamadığı için yönetici özeti oluşturulamadı.";
        }

        List<String> parts = new ArrayList<>();
        parts.add(
                "İncelenen "
                        + summary.getTotalActiveProjects()
                        + " aktif projenin "
                        + summary.getCriticalProjects()
                        + " tanesi kritik, "
                        + summary.getNeedsAttentionProjects()
                        + " tanesi dikkat gerektiriyor ve "
                        + summary.getHealthyProjects()
                        + " tanesi sağlıklı durumda."
        );

        if (summary.getProjectsWithoutReport() > 0) {
            parts.add(
                    summary.getProjectsWithoutReport()
                            + " proje için aktif haftalık rapor bulunmuyor."
            );
        }

        parts.add(
                summary.getCompletedWorkItems()
                        + "/"
                        + summary.getTotalActiveWorkItems()
                        + " iş kalemi tamamlandı."
        );

        if (summary.getCriticalRiskIssues() > 0
                || summary.getOverdueActionItems() > 0) {
            parts.add(
                    summary.getCriticalRiskIssues()
                            + " kritik risk ve "
                            + summary.getOverdueActionItems()
                            + " gecikmiş aksiyon yönetim müdahalesi gerektiriyor."
            );
        } else if (summary.getOpenRiskIssues() > 0) {
            parts.add(
                    summary.getOpenRiskIssues()
                            + " açık risk veya engel düzenli takip edilmelidir."
            );
        } else {
            parts.add(
                    "Mevcut verilerde kritik risk veya gecikmiş aksiyon bulunmuyor."
            );
        }

        return limitText(
                String.join(" ", parts),
                MAX_SUMMARY_LENGTH
        );
    }

    private List<String> buildPortfolioHighlights(
            DashboardSummaryResponse summary
    ) {
        List<String> highlights = new ArrayList<>();

        highlights.add(
                summary.getTotalActiveWeeklyReports()
                        + " aktif haftalık rapor analiz edildi."
        );
        highlights.add(
                summary.getCompletedWorkItems()
                        + "/"
                        + summary.getTotalActiveWorkItems()
                        + " iş kalemi tamamlandı."
        );
        highlights.add(
                summary.getOpenRiskIssues()
                        + " açık risk veya engelin "
                        + summary.getCriticalRiskIssues()
                        + " tanesi kritik seviyede."
        );
        highlights.add(
                summary.getOverdueActionItems()
                        + " aksiyon hedef tarihini geçti."
        );
        highlights.add(
                summary.getBlockedWorkItems()
                        + " blokeli, "
                        + summary.getDelayedWorkItems()
                        + " gecikmiş ve "
                        + summary.getRiskyWorkItems()
                        + " riskli iş kalemi bulunuyor."
        );

        return highlights;
    }

    private List<AiDashboardProjectInsightResponse>
    buildAttentionProjects(
            List<DashboardProjectResponse> projects
    ) {
        return projects.stream()
                .filter(project ->
                        project.getHealthStatus()
                                != ProjectHealthStatus.HEALTHY
                )
                .sorted(
                        Comparator
                                .comparingInt(
                                        (DashboardProjectResponse project) -> healthPriority(
                                                project.getHealthStatus()
                                        )
                                )
                                .thenComparingInt(
                                        DashboardProjectResponse::getHealthScore
                                )
                                .thenComparing(
                                        DashboardProjectResponse::getProjectName,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                )
                .limit(MAX_SUGGESTION_COUNT)
                .map(this::toAttentionProject)
                .toList();
    }

    private AiDashboardProjectInsightResponse toAttentionProject(
            DashboardProjectResponse project
    ) {
        AiDashboardProjectInsightResponse response =
                new AiDashboardProjectInsightResponse();

        response.setProjectId(project.getProjectId());
        response.setProjectName(project.getProjectName());
        response.setProjectStatus(project.getProjectStatus());
        response.setHealthStatus(project.getHealthStatus());
        response.setHealthScore(project.getHealthScore());
        response.setLatestReportWeekStartDate(
                project.getLatestReportWeekStartDate()
        );
        response.setReason(
                buildAttentionReason(project)
        );

        return response;
    }

    private String buildAttentionReason(
            DashboardProjectResponse project
    ) {
        List<String> reasons = new ArrayList<>();

        if (project.getHealthStatus()
                == ProjectHealthStatus.NO_REPORT) {
            reasons.add("Aktif haftalık rapor bulunmuyor");
        }

        if (project.getCriticalRiskIssues() > 0) {
            reasons.add(
                    project.getCriticalRiskIssues()
                            + " kritik risk açık"
            );
        }

        if (project.getOverdueActionItems() > 0) {
            reasons.add(
                    project.getOverdueActionItems()
                            + " aksiyon gecikmiş"
            );
        }

        if (project.getBlockedWorkItems() > 0) {
            reasons.add(
                    project.getBlockedWorkItems()
                            + " iş kalemi blokeli"
            );
        }

        if (project.getDelayedWorkItems() > 0) {
            reasons.add(
                    project.getDelayedWorkItems()
                            + " iş kalemi gecikmiş"
            );
        }

        if (project.getRiskyWorkItems() > 0) {
            reasons.add(
                    project.getRiskyWorkItems()
                            + " iş kalemi riskli"
            );
        }

        if (hasText(project.getLatestReportRisks())) {
            reasons.add(
                    "Son rapor sinyali: "
                            + limitText(
                            project.getLatestReportRisks(),
                            150
                    )
            );
        }

        if (reasons.isEmpty()) {
            reasons.add(
                    "Sağlık skoru "
                            + project.getHealthScore()
                            + "/100"
            );
        }

        return ensureSentence(
                String.join("; ", reasons)
        );
    }

    private List<String> buildWeeklyReportInsights(
            List<DashboardProjectResponse> projects
    ) {
        List<String> insights = projects.stream()
                .filter(project ->
                        project.getLatestWeeklyReportId() != null
                )
                .sorted(
                        Comparator
                                .comparingInt(
                                        (DashboardProjectResponse project) -> healthPriority(
                                                project.getHealthStatus()
                                        )
                                )
                                .thenComparingInt(
                                        DashboardProjectResponse::getHealthScore
                                )
                )
                .map(project -> {
                    String reportText = hasText(
                            project.getLatestReportRisks()
                    )
                            ? project.getLatestReportRisks()
                            : project.getLatestReportSummary();

                    if (!hasText(reportText)) {
                        return null;
                    }

                    return project.getProjectName()
                            + ": "
                            + limitText(
                            reportText,
                            MAX_INSIGHT_LENGTH
                    );
                })
                .filter(this::hasText)
                .distinct()
                .limit(MAX_SUGGESTION_COUNT)
                .toList();

        if (!insights.isEmpty()) {
            return insights;
        }

        return List.of(
                "İncelenen projelerin son haftalık raporlarında özetlenebilecek metin bulunamadı."
        );
    }

    private List<String> buildPortfolioRecommendations(
            DashboardSummaryResponse summary
    ) {
        Set<String> recommendations = new LinkedHashSet<>();

        if (summary.getCriticalProjects() > 0) {
            recommendations.add(
                    "Kritik projeler için proje yöneticileriyle yönetim değerlendirmesi yapılmalıdır."
            );
        }

        if (summary.getProjectsWithoutReport() > 0) {
            recommendations.add(
                    "Raporu bulunmayan projelerin güncel haftalık raporları tamamlanmalıdır."
            );
        }

        if (summary.getCriticalRiskIssues() > 0) {
            recommendations.add(
                    "Kritik riskler için sorumlu, çözüm planı ve takip tarihi netleştirilmelidir."
            );
        } else if (summary.getOpenRiskIssues() > 0) {
            recommendations.add(
                    "Açık risk ve engeller haftalık yönetim toplantısında gözden geçirilmelidir."
            );
        }

        if (summary.getOverdueActionItems() > 0) {
            recommendations.add(
                    "Gecikmiş aksiyonlar yeniden planlanmalı veya tamamlanarak kapatılmalıdır."
            );
        }

        if (summary.getBlockedWorkItems() > 0
                || summary.getDelayedWorkItems() > 0) {
            recommendations.add(
                    "Blokeli ve gecikmiş iş kalemlerinin bağımlılıkları kaldırılmalı, yeni hedef tarihleri belirlenmelidir."
            );
        }

        if (recommendations.isEmpty()) {
            recommendations.add(
                    "Mevcut ilerleme korunmalı ve haftalık raporlar düzenli olarak güncellenmelidir."
            );
        }

        return recommendations.stream()
                .limit(MAX_SUGGESTION_COUNT)
                .toList();
    }

    private int healthPriority(
            ProjectHealthStatus status
    ) {
        if (status == null) {
            return 4;
        }

        return switch (status) {
            case CRITICAL -> 0;
            case NEEDS_ATTENTION -> 1;
            case NO_REPORT -> 2;
            case HEALTHY -> 3;
        };
    }

    private void addIndicator(
            List<String> indicators,
            int count,
            String label
    ) {
        if (count > 0) {
            indicators.add(
                    count + " " + label
            );
        }
    }

    private boolean hasOperationalRisk(
            AiWeeklyReportAnalysisContext context
    ) {
        return context.openHighRiskIssues() > 0
                || context.openCriticalRiskIssues() > 0
                || context.atRiskWorkItems() > 0
                || context.blockedWorkItems() > 0
                || context.delayedWorkItems() > 0
                || context.overdueActionItems() > 0;
    }

    private List<String> splitSuggestions(String value) {
        return List.of(
                        value.split("[\\n;]+")
                )
                .stream()
                .map(String::trim)
                .filter(this::hasText)
                .map(this::ensureSentence)
                .toList();
    }

    private String statusDescription(
            WeeklyReportStatus status
    ) {
        return switch (status) {
            case DELAYED -> "gecikmiş durumda";
            case AT_RISK -> "riskli durumda";
            case ON_TRACK -> "plan doğrultusunda ilerliyor";
        };
    }

    private String ensureSentence(String value) {
        String normalized = value
                .replaceAll("\\s+", " ")
                .trim();

        if (normalized.isEmpty()) {
            return normalized;
        }

        char lastCharacter = normalized.charAt(
                normalized.length() - 1
        );

        if (lastCharacter == '.'
                || lastCharacter == '!'
                || lastCharacter == '?') {

            return normalized;
        }

        return normalized + ".";
    }

    private String limitText(
            String value,
            int maximumLength
    ) {
        String normalized = value
                .replaceAll("\\s+", " ")
                .trim();

        if (normalized.length() <= maximumLength) {
            return normalized;
        }

        return normalized.substring(
                0,
                maximumLength - 3
        ).trim() + "...";
    }

    private String normalizeForSearch(String value) {
        return value
                .toLowerCase(
                        Locale.forLanguageTag("tr-TR")
                )
                .replace('ı', 'i')
                .replace('ş', 's')
                .replace('ğ', 'g')
                .replace('ü', 'u')
                .replace('ö', 'o')
                .replace('ç', 'c');
    }

    private boolean containsAny(
            String value,
            String... keywords
    ) {
        for (String keyword : keywords) {
            if (value.contains(
                    normalizeForSearch(keyword)
            )) {
                return true;
            }
        }

        return false;
    }

    private boolean hasText(String value) {
        return value != null
                && !value.isBlank();
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
