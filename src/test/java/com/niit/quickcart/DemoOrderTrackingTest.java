package com.niit.quickcart;

import com.niit.quickcart.service.DemoOrderTracking;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DemoOrderTrackingTest {

    private final LocalDateTime now = LocalDateTime.of(2026, 10, 1, 12, 0);

    @Test
    void startsWithConfirmedStage() {
        assertStage(0, "confirmed", 0, 3);
    }

    @Test
    void advancesToPreparingStage() {
        assertStage(30, "preparing", 1, 3);
    }

    @Test
    void advancesToOnTheWayStage() {
        assertStage(90, "on_the_way", 2, 2);
    }

    @Test
    void completesAfterThreeMinutes() {
        assertStage(180, "delivered", 3, 0);
    }

    private void assertStage(long ageSeconds, String stage, int step, int etaMinutes) {
        Map<String, Object> tracking = DemoOrderTracking.forOrder(
                now.minusSeconds(ageSeconds), now);

        assertEquals(true, tracking.get("demo"));
        assertEquals(stage, tracking.get("stage"));
        assertEquals(step, tracking.get("step"));
        assertEquals(etaMinutes, tracking.get("etaMinutes"));
    }
}
