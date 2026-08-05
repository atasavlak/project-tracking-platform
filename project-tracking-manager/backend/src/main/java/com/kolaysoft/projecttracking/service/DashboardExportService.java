package com.kolaysoft.projecttracking.service;

import com.kolaysoft.projecttracking.dto.DashboardCriticalRiskResponse;
import com.kolaysoft.projecttracking.dto.DashboardOverdueActionResponse;
import com.kolaysoft.projecttracking.dto.DashboardProjectResponse;
import com.kolaysoft.projecttracking.dto.DashboardRiskyWorkItemResponse;
import com.kolaysoft.projecttracking.dto.DashboardSummaryResponse;
import com.kolaysoft.projecttracking.dto.ProjectHealthStatus;
import com.kolaysoft.projecttracking.entity.ProjectStatus;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardExportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "dd.MM.yyyy HH:mm",
                    Locale.forLanguageTag("tr-TR")
            );

    private final DashboardService dashboardService;

    public DashboardExportService(
            DashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    public byte[] exportPdf(
            ProjectStatus projectStatus,
            ProjectHealthStatus healthStatus
    ) {
        DashboardReportData data = getReportData(
                projectStatus,
                healthStatus
        );

        try (
                PDDocument document = new PDDocument();
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {
            FontSet fonts = loadFonts(document);
            PdfWriter writer = new PdfWriter(document, fonts);

            writer.title("CTO Proje Durum Raporu");
            writer.paragraph(
                    "Olusturulma zamani: "
                            + DATE_TIME_FORMATTER.format(
                            LocalDateTime.now()
                    )
            );
            writer.paragraph(
                    "Proje durumu filtresi: "
                            + valueOrAll(projectStatus)
                            + " | Saglik filtresi: "
                            + valueOrAll(healthStatus)
            );

            writer.heading("Genel Ozet");
            appendSummaryToPdf(writer, data.summary());

            writer.heading("Proje Saglik Durumu");
            for (DashboardProjectResponse project : data.projects()) {
                writer.subheading(project.getProjectName());
                writer.paragraph(
                        "Yonetici: "
                                + valueOrDash(
                                project.getProjectManagerFullName()
                        )
                                + " | Durum: "
                                + project.getProjectStatus()
                                + " | Saglik: "
                                + project.getHealthStatus()
                                + " | Skor: "
                                + project.getHealthScore()
                                + " | Ilerleme: %"
                                + project.getCompletionRate()
                );
                writer.paragraph(
                        "Acik risk: "
                                + project.getOpenRiskIssues()
                                + " | Kritik risk: "
                                + project.getCriticalRiskIssues()
                                + " | Karar: "
                                + project.getDecisionCount()
                                + " | Acik aksiyon: "
                                + project.getOpenActionItems()
                                + " | Gecikmis aksiyon: "
                                + project.getOverdueActionItems()
                );
                writer.paragraph(
                        "Son rapor: "
                                + valueOrDash(
                                project.getLatestReportWeekStartDate()
                        )
                                + " | Rapor durumu: "
                                + valueOrDash(
                                project.getLatestReportStatus()
                        )
                );
            }

            writer.heading("Kritik Risk ve Engeller");
            if (data.criticalRisks().isEmpty()) {
                writer.paragraph("Kayit bulunmuyor.");
            } else {
                for (DashboardCriticalRiskResponse risk :
                        data.criticalRisks()) {
                    writer.paragraph(
                            risk.getProjectName()
                                    + " | "
                                    + risk.getSeverity()
                                    + " | "
                                    + risk.getTitle()
                                    + " | Sorumlu: "
                                    + valueOrDash(
                                    risk.getResponsibleUserFullName()
                            )
                                    + " | Takip: "
                                    + valueOrDash(risk.getFollowUpDate())
                    );
                }
            }

            writer.heading("Gecikmis Aksiyonlar");
            if (data.overdueActions().isEmpty()) {
                writer.paragraph("Kayit bulunmuyor.");
            } else {
                for (DashboardOverdueActionResponse action :
                        data.overdueActions()) {
                    writer.paragraph(
                            action.getProjectName()
                                    + " | "
                                    + action.getPriority()
                                    + " | "
                                    + action.getTitle()
                                    + " | Sorumlu: "
                                    + valueOrDash(
                                    action.getResponsibleUserFullName()
                            )
                                    + " | Hedef: "
                                    + valueOrDash(action.getTargetDate())
                                    + " | Gecikme: "
                                    + action.getOverdueDays()
                                    + " gun"
                    );
                }
            }

            writer.heading("Kritik Is Kalemleri");
            if (data.riskyWorkItems().isEmpty()) {
                writer.paragraph("Kayit bulunmuyor.");
            } else {
                for (DashboardRiskyWorkItemResponse workItem :
                        data.riskyWorkItems()) {
                    writer.paragraph(
                            workItem.getProjectName()
                                    + " | "
                                    + workItem.getStatus()
                                    + " | "
                                    + workItem.getTitle()
                                    + " | Sorumlu: "
                                    + valueOrDash(
                                    workItem.getResponsiblePerson()
                            )
                    );
                }
            }

            writer.close();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "PDF raporu oluşturulamadı.",
                    exception
            );
        }
    }

    public byte[] exportExcel(
            ProjectStatus projectStatus,
            ProjectHealthStatus healthStatus
    ) {
        DashboardReportData data = getReportData(
                projectStatus,
                healthStatus
        );

        try (
                XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {
            ExcelStyles styles = createExcelStyles(workbook);

            createSummarySheet(
                    workbook,
                    styles,
                    data.summary(),
                    projectStatus,
                    healthStatus
            );
            createProjectsSheet(
                    workbook,
                    styles,
                    data.projects()
            );
            createCriticalRisksSheet(
                    workbook,
                    styles,
                    data.criticalRisks()
            );
            createOverdueActionsSheet(
                    workbook,
                    styles,
                    data.overdueActions()
            );
            createRiskyWorkItemsSheet(
                    workbook,
                    styles,
                    data.riskyWorkItems()
            );

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Excel raporu oluşturulamadı.",
                    exception
            );
        }
    }

    private DashboardReportData getReportData(
            ProjectStatus projectStatus,
            ProjectHealthStatus healthStatus
    ) {
        List<DashboardProjectResponse> projects =
                dashboardService.getProjects(
                        projectStatus,
                        healthStatus
                );

        Set<Long> projectIds = projects.stream()
                .map(DashboardProjectResponse::getProjectId)
                .collect(Collectors.toUnmodifiableSet());

        return new DashboardReportData(
                dashboardService.getSummaryForProjects(projects),
                projects,
                dashboardService.getCriticalRisksForProjects(
                        projectIds
                ),
                dashboardService.getOverdueActionsForProjects(
                        projectIds
                ),
                dashboardService.getRiskyWorkItemsForProjects(
                        projectIds
                )
        );
    }

    private void appendSummaryToPdf(
            PdfWriter writer,
            DashboardSummaryResponse summary
    ) throws IOException {
        writer.paragraph(
                "Aktif proje: "
                        + summary.getTotalActiveProjects()
                        + " | Saglikli: "
                        + summary.getHealthyProjects()
                        + " | Dikkat: "
                        + summary.getNeedsAttentionProjects()
                        + " | Kritik: "
                        + summary.getCriticalProjects()
                        + " | Raporsuz: "
                        + summary.getProjectsWithoutReport()
        );
        writer.paragraph(
                "Aktif is kalemi: "
                        + summary.getTotalActiveWorkItems()
                        + " | Tamamlanan: "
                        + summary.getCompletedWorkItems()
                        + " | Riskli: "
                        + summary.getRiskyWorkItems()
                        + " | Blokeli: "
                        + summary.getBlockedWorkItems()
                        + " | Gecikmis: "
                        + summary.getDelayedWorkItems()
        );
        writer.paragraph(
                "Acik risk/engel: "
                        + summary.getOpenRiskIssues()
                        + " | Kritik risk/engel: "
                        + summary.getCriticalRiskIssues()
                        + " | Karar: "
                        + summary.getTotalActiveDecisions()
                        + " | Acik aksiyon: "
                        + summary.getOpenActionItems()
                        + " | Gecikmis aksiyon: "
                        + summary.getOverdueActionItems()
        );
    }

    private ExcelStyles createExcelStyles(XSSFWorkbook workbook) {
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(
                IndexedColors.DARK_BLUE.getIndex()
        );
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        CellStyle bodyStyle = workbook.createCellStyle();
        bodyStyle.setVerticalAlignment(VerticalAlignment.TOP);
        bodyStyle.setWrapText(true);
        bodyStyle.setBorderBottom(BorderStyle.THIN);
        bodyStyle.setBorderTop(BorderStyle.THIN);
        bodyStyle.setBorderLeft(BorderStyle.THIN);
        bodyStyle.setBorderRight(BorderStyle.THIN);

        CellStyle metricLabelStyle = workbook.createCellStyle();
        Font metricLabelFont = workbook.createFont();
        metricLabelFont.setBold(true);
        metricLabelStyle.setFont(metricLabelFont);
        metricLabelStyle.setFillForegroundColor(
                IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex()
        );
        metricLabelStyle.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );
        metricLabelStyle.setBorderBottom(BorderStyle.THIN);
        metricLabelStyle.setBorderTop(BorderStyle.THIN);
        metricLabelStyle.setBorderLeft(BorderStyle.THIN);
        metricLabelStyle.setBorderRight(BorderStyle.THIN);

        CellStyle metricValueStyle = workbook.createCellStyle();
        metricValueStyle.setAlignment(HorizontalAlignment.CENTER);
        metricValueStyle.setBorderBottom(BorderStyle.THIN);
        metricValueStyle.setBorderTop(BorderStyle.THIN);
        metricValueStyle.setBorderLeft(BorderStyle.THIN);
        metricValueStyle.setBorderRight(BorderStyle.THIN);

        return new ExcelStyles(
                titleStyle,
                headerStyle,
                bodyStyle,
                metricLabelStyle,
                metricValueStyle
        );
    }

    private void createSummarySheet(
            XSSFWorkbook workbook,
            ExcelStyles styles,
            DashboardSummaryResponse summary,
            ProjectStatus projectStatus,
            ProjectHealthStatus healthStatus
    ) {
        Sheet sheet = workbook.createSheet("Genel Ozet");
        int rowIndex = 0;

        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("CTO Proje Durum Raporu");
        titleCell.setCellStyle(styles.title());

        addSummaryTextRow(
                sheet,
                rowIndex++,
                "Olusturulma Zamani",
                DATE_TIME_FORMATTER.format(LocalDateTime.now()),
                styles
        );
        addSummaryTextRow(
                sheet,
                rowIndex++,
                "Proje Durumu Filtresi",
                valueOrAll(projectStatus),
                styles
        );
        addSummaryTextRow(
                sheet,
                rowIndex++,
                "Saglik Filtresi",
                valueOrAll(healthStatus),
                styles
        );

        rowIndex++;

        List<Metric> metrics = List.of(
                new Metric("Aktif Proje", summary.getTotalActiveProjects()),
                new Metric("Saglikli Proje", summary.getHealthyProjects()),
                new Metric(
                        "Dikkat Gerektiren Proje",
                        summary.getNeedsAttentionProjects()
                ),
                new Metric("Kritik Proje", summary.getCriticalProjects()),
                new Metric(
                        "Raporsuz Proje",
                        summary.getProjectsWithoutReport()
                ),
                new Metric(
                        "Aktif Haftalik Rapor",
                        summary.getTotalActiveWeeklyReports()
                ),
                new Metric(
                        "Aktif Is Kalemi",
                        summary.getTotalActiveWorkItems()
                ),
                new Metric(
                        "Tamamlanan Is Kalemi",
                        summary.getCompletedWorkItems()
                ),
                new Metric("Acik Risk/Engel", summary.getOpenRiskIssues()),
                new Metric(
                        "Kritik Risk/Engel",
                        summary.getCriticalRiskIssues()
                ),
                new Metric(
                        "Aktif Karar",
                        summary.getTotalActiveDecisions()
                ),
                new Metric("Acik Aksiyon", summary.getOpenActionItems()),
                new Metric(
                        "Gecikmis Aksiyon",
                        summary.getOverdueActionItems()
                ),
                new Metric(
                        "Tamamlanan Aksiyon",
                        summary.getCompletedActionItems()
                )
        );

        for (Metric metric : metrics) {
            Row row = sheet.createRow(rowIndex++);
            Cell labelCell = row.createCell(0);
            labelCell.setCellValue(metric.label());
            labelCell.setCellStyle(styles.metricLabel());

            Cell valueCell = row.createCell(1);
            valueCell.setCellValue(metric.value());
            valueCell.setCellStyle(styles.metricValue());
        }

        sheet.setColumnWidth(0, 36 * 256);
        sheet.setColumnWidth(1, 24 * 256);
        sheet.createFreezePane(0, 1);
    }

    private void createProjectsSheet(
            XSSFWorkbook workbook,
            ExcelStyles styles,
            List<DashboardProjectResponse> projects
    ) {
        Sheet sheet = workbook.createSheet("Projeler");
        String[] headers = {
                "Proje ID",
                "Proje",
                "Yonetici",
                "Proje Durumu",
                "Saglik Durumu",
                "Saglik Skoru",
                "Ilerleme Yuzdesi",
                "Son Rapor Tarihi",
                "Son Rapor Durumu",
                "Toplam Is",
                "Tamamlanan Is",
                "Devam Eden Is",
                "Riskli Is",
                "Blokeli Is",
                "Gecikmis Is",
                "Acik Risk",
                "Kritik Risk",
                "Karar",
                "Onayli/Uygulanan Karar",
                "Acik Aksiyon",
                "Gecikmis Aksiyon",
                "Tamamlanan Aksiyon"
        };

        createHeaderRow(sheet, headers, styles.header());
        int rowIndex = 1;

        for (DashboardProjectResponse project : projects) {
            Row row = sheet.createRow(rowIndex++);
            List<Object> values = List.of(
                    project.getProjectId(),
                    valueOrDash(project.getProjectName()),
                    valueOrDash(project.getProjectManagerFullName()),
                    project.getProjectStatus(),
                    project.getHealthStatus(),
                    project.getHealthScore(),
                    project.getCompletionRate(),
                    valueOrDash(project.getLatestReportWeekStartDate()),
                    valueOrDash(project.getLatestReportStatus()),
                    project.getTotalWorkItems(),
                    project.getCompletedWorkItems(),
                    project.getInProgressWorkItems(),
                    project.getRiskyWorkItems(),
                    project.getBlockedWorkItems(),
                    project.getDelayedWorkItems(),
                    project.getOpenRiskIssues(),
                    project.getCriticalRiskIssues(),
                    project.getDecisionCount(),
                    project.getApprovedDecisionCount(),
                    project.getOpenActionItems(),
                    project.getOverdueActionItems(),
                    project.getCompletedActionItems()
            );
            fillRow(row, values, styles.body());
        }

        finalizeTableSheet(sheet, headers.length);
    }

    private void createCriticalRisksSheet(
            XSSFWorkbook workbook,
            ExcelStyles styles,
            List<DashboardCriticalRiskResponse> risks
    ) {
        Sheet sheet = workbook.createSheet("Kritik Riskler");
        String[] headers = {
                "Risk ID",
                "Proje",
                "Tip",
                "Baslik",
                "Onem",
                "Durum",
                "Sorumlu",
                "Takip Tarihi"
        };
        createHeaderRow(sheet, headers, styles.header());

        int rowIndex = 1;
        for (DashboardCriticalRiskResponse risk : risks) {
            Row row = sheet.createRow(rowIndex++);
            fillRow(
                    row,
                    List.of(
                            risk.getRiskIssueId(),
                            risk.getProjectName(),
                            risk.getType(),
                            risk.getTitle(),
                            risk.getSeverity(),
                            risk.getStatus(),
                            risk.getResponsibleUserFullName(),
                            valueOrDash(risk.getFollowUpDate())
                    ),
                    styles.body()
            );
        }

        finalizeTableSheet(sheet, headers.length);
    }

    private void createOverdueActionsSheet(
            XSSFWorkbook workbook,
            ExcelStyles styles,
            List<DashboardOverdueActionResponse> actions
    ) {
        Sheet sheet = workbook.createSheet("Gecikmis Aksiyonlar");
        String[] headers = {
                "Aksiyon ID",
                "Proje",
                "Baslik",
                "Oncelik",
                "Durum",
                "Sorumlu",
                "Hedef Tarih",
                "Gecikme Gunu"
        };
        createHeaderRow(sheet, headers, styles.header());

        int rowIndex = 1;
        for (DashboardOverdueActionResponse action : actions) {
            Row row = sheet.createRow(rowIndex++);
            fillRow(
                    row,
                    List.of(
                            action.getActionItemId(),
                            action.getProjectName(),
                            action.getTitle(),
                            action.getPriority(),
                            action.getStatus(),
                            action.getResponsibleUserFullName(),
                            valueOrDash(action.getTargetDate()),
                            action.getOverdueDays()
                    ),
                    styles.body()
            );
        }

        finalizeTableSheet(sheet, headers.length);
    }

    private void createRiskyWorkItemsSheet(
            XSSFWorkbook workbook,
            ExcelStyles styles,
            List<DashboardRiskyWorkItemResponse> workItems
    ) {
        Sheet sheet = workbook.createSheet("Kritik Isler");
        String[] headers = {
                "Is Kalemi ID",
                "Proje",
                "Baslik",
                "Durum",
                "Sorumlu",
                "Rapor Haftasi"
        };
        createHeaderRow(sheet, headers, styles.header());

        int rowIndex = 1;
        for (DashboardRiskyWorkItemResponse workItem : workItems) {
            Row row = sheet.createRow(rowIndex++);
            fillRow(
                    row,
                    List.of(
                            workItem.getWorkItemId(),
                            workItem.getProjectName(),
                            workItem.getTitle(),
                            workItem.getStatus(),
                            valueOrDash(workItem.getResponsiblePerson()),
                            valueOrDash(workItem.getReportWeekStartDate())
                    ),
                    styles.body()
            );
        }

        finalizeTableSheet(sheet, headers.length);
    }

    private void addSummaryTextRow(
            Sheet sheet,
            int rowIndex,
            String label,
            String value,
            ExcelStyles styles
    ) {
        Row row = sheet.createRow(rowIndex);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(styles.metricLabel());

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(styles.body());
    }

    private void createHeaderRow(
            Sheet sheet,
            String[] headers,
            CellStyle headerStyle
    ) {
        Row row = sheet.createRow(0);
        row.setHeightInPoints(28);

        for (int index = 0; index < headers.length; index++) {
            Cell cell = row.createCell(index);
            cell.setCellValue(headers[index]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void fillRow(
            Row row,
            List<Object> values,
            CellStyle style
    ) {
        for (int index = 0; index < values.size(); index++) {
            Cell cell = row.createCell(index);
            Object value = values.get(index);

            if (value instanceof Number number) {
                cell.setCellValue(number.doubleValue());
            } else {
                cell.setCellValue(valueOrDash(value));
            }

            cell.setCellStyle(style);
        }
    }

    private void finalizeTableSheet(
            Sheet sheet,
            int columnCount
    ) {
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(
                new org.apache.poi.ss.util.CellRangeAddress(
                        0,
                        Math.max(0, sheet.getLastRowNum()),
                        0,
                        columnCount - 1
                )
        );

        for (int index = 0; index < columnCount; index++) {
            sheet.autoSizeColumn(index);
            int width = Math.min(
                    sheet.getColumnWidth(index) + 768,
                    40 * 256
            );
            sheet.setColumnWidth(index, width);
        }
    }

    private FontSet loadFonts(PDDocument document)
            throws IOException {
        List<FontCandidate> candidates = List.of(
                new FontCandidate(
                        "C:/Windows/Fonts/arial.ttf",
                        "C:/Windows/Fonts/arialbd.ttf"
                ),
                new FontCandidate(
                        "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
                ),
                new FontCandidate(
                        "/usr/share/fonts/truetype/liberation2/LiberationSans-Regular.ttf",
                        "/usr/share/fonts/truetype/liberation2/LiberationSans-Bold.ttf"
                )
        );

        for (FontCandidate candidate : candidates) {
            File regular = new File(candidate.regularPath());
            File bold = new File(candidate.boldPath());

            if (regular.isFile() && bold.isFile()) {
                return new FontSet(
                        PDType0Font.load(document, regular),
                        PDType0Font.load(document, bold),
                        true
                );
            }
        }

        return new FontSet(
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA
                ),
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA_BOLD
                ),
                false
        );
    }

    private static String valueOrAll(Object value) {
        return value == null ? "TUMU" : value.toString();
    }

    private static String valueOrDash(Object value) {
        return value == null || value.toString().isBlank()
                ? "-"
                : value.toString();
    }

    private static String asciiSafe(String value) {
        String replaced = value
                .replace('ı', 'i')
                .replace('İ', 'I')
                .replace('ğ', 'g')
                .replace('Ğ', 'G')
                .replace('ş', 's')
                .replace('Ş', 'S')
                .replace('ç', 'c')
                .replace('Ç', 'C')
                .replace('ö', 'o')
                .replace('Ö', 'O')
                .replace('ü', 'u')
                .replace('Ü', 'U');

        return Normalizer.normalize(
                        replaced,
                        Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}", "")
                .replaceAll("[^\\x20-\\x7E]", "?");
    }

    private record DashboardReportData(
            DashboardSummaryResponse summary,
            List<DashboardProjectResponse> projects,
            List<DashboardCriticalRiskResponse> criticalRisks,
            List<DashboardOverdueActionResponse> overdueActions,
            List<DashboardRiskyWorkItemResponse> riskyWorkItems
    ) {
    }

    private record Metric(String label, long value) {
    }

    private record ExcelStyles(
            CellStyle title,
            CellStyle header,
            CellStyle body,
            CellStyle metricLabel,
            CellStyle metricValue
    ) {
    }

    private record FontCandidate(
            String regularPath,
            String boldPath
    ) {
    }

    private record FontSet(
            PDFont normal,
            PDFont bold,
            boolean unicode
    ) {
    }

    private static final class PdfWriter {

        private static final float MARGIN = 48;
        private static final float BOTTOM_MARGIN = 48;
        private static final float CONTENT_WIDTH =
                PDRectangle.A4.getWidth() - MARGIN * 2;

        private final PDDocument document;
        private final FontSet fonts;
        private PDPageContentStream contentStream;
        private float y;

        private PdfWriter(
                PDDocument document,
                FontSet fonts
        ) throws IOException {
            this.document = document;
            this.fonts = fonts;
            newPage();
        }

        private void title(String text) throws IOException {
            writeWrapped(text, fonts.bold(), 18, 24);
        }

        private void heading(String text) throws IOException {
            ensureSpace(34);
            y -= 8;
            writeWrapped(text, fonts.bold(), 14, 19);
        }

        private void subheading(String text) throws IOException {
            ensureSpace(28);
            writeWrapped(text, fonts.bold(), 11, 16);
        }

        private void paragraph(String text) throws IOException {
            writeWrapped(text, fonts.normal(), 9.5f, 14);
        }

        private void writeWrapped(
                String rawText,
                PDFont font,
                float fontSize,
                float lineHeight
        ) throws IOException {
            String text = fonts.unicode()
                    ? valueOrDash(rawText)
                    : asciiSafe(valueOrDash(rawText));

            List<String> lines = wrap(
                    text,
                    font,
                    fontSize,
                    CONTENT_WIDTH
            );

            for (String line : lines) {
                ensureSpace(lineHeight);
                contentStream.beginText();
                contentStream.setFont(font, fontSize);
                contentStream.newLineAtOffset(MARGIN, y);
                contentStream.showText(line);
                contentStream.endText();
                y -= lineHeight;
            }

            y -= 3;
        }

        private List<String> wrap(
                String text,
                PDFont font,
                float fontSize,
                float maxWidth
        ) throws IOException {
            List<String> lines = new ArrayList<>();
            String[] paragraphs = text.split("\\R", -1);

            for (String paragraph : paragraphs) {
                if (paragraph.isBlank()) {
                    lines.add("");
                    continue;
                }

                StringBuilder currentLine = new StringBuilder();
                for (String word : paragraph.split("\\s+")) {
                    String candidate = currentLine.isEmpty()
                            ? word
                            : currentLine + " " + word;
                    float width = font.getStringWidth(candidate)
                            / 1000
                            * fontSize;

                    if (width <= maxWidth || currentLine.isEmpty()) {
                        currentLine.setLength(0);
                        currentLine.append(candidate);
                    } else {
                        lines.add(currentLine.toString());
                        currentLine.setLength(0);
                        currentLine.append(word);
                    }
                }

                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                }
            }

            return lines;
        }

        private void ensureSpace(float requiredSpace)
                throws IOException {
            if (y - requiredSpace < BOTTOM_MARGIN) {
                newPage();
            }
        }

        private void newPage() throws IOException {
            if (contentStream != null) {
                contentStream.close();
            }

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contentStream = new PDPageContentStream(
                    document,
                    page
            );
            y = PDRectangle.A4.getHeight() - MARGIN;
        }

        private void close() throws IOException {
            if (contentStream != null) {
                contentStream.close();
                contentStream = null;
            }
        }
    }
}
