package com.kolaysoft.projecttracking.service.ai;

import com.kolaysoft.projecttracking.dto.AiWeeklyReportAnalysisResponse;
import com.kolaysoft.projecttracking.entity.WeeklyReportStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockAiProviderTest {

    private final MockAiProvider provider =
            new MockAiProvider();

    @Test
    void delayedSignalsProduceDelayedRecommendation() {
        AiWeeklyReportAnalysisContext context =
                new AiWeeklyReportAnalysisContext(
                        1L,
                        "Test Projesi",
                        WeeklyReportStatus.ON_TRACK,
                        "Test ortamı bekleniyor.",
                        "Servis geliştirmesi tamamlandı.",
                        "Entegrasyon testleri yapılacak.",
                        "Test cihazı eksikliği gecikmeye neden oluyor.",
                        6,
                        2,
                        2,
                        0,
                        0,
                        1,
                        1,
                        0,
                        0,
                        List.of("Test ortamı erişim riski"),
                        List.of()
                );

        AiWeeklyReportAnalysisResponse response =
                provider.analyze(context);

        assertEquals(
                WeeklyReportStatus.DELAYED,
                response.getSuggestedStatus()
        );
        assertFalse(response.getDetectedRisks().isEmpty());
        assertFalse(response.getSuggestedActions().isEmpty());
        assertTrue(
                response.getExecutiveSummary()
                        .contains("Test Projesi")
        );
    }

    @Test
    void cleanSignalsProduceOnTrackRecommendation() {
        AiWeeklyReportAnalysisContext context =
                new AiWeeklyReportAnalysisContext(
                        2L,
                        "Planlı Proje",
                        WeeklyReportStatus.ON_TRACK,
                        "Çalışmalar plana uygun ilerliyor.",
                        "Planlanan geliştirmeler tamamlandı.",
                        "Kullanıcı testleri gerçekleştirilecek.",
                        null,
                        4,
                        3,
                        1,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        List.of(),
                        List.of()
                );

        AiWeeklyReportAnalysisResponse response =
                provider.analyze(context);

        assertEquals(
                WeeklyReportStatus.ON_TRACK,
                response.getSuggestedStatus()
        );
        assertEquals(4, response.getTotalWorkItems());
        assertEquals(3, response.getCompletedWorkItems());
    }
}
