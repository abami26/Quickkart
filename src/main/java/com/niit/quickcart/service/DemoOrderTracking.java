package com.niit.quickcart.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DemoOrderTracking {

    private DemoOrderTracking() {
    }

    public static Map<String, Object> forOrder(LocalDateTime createdAt, LocalDateTime now) {
        long elapsedSeconds = Math.max(0, Duration.between(createdAt, now).getSeconds());
        String stage;
        String label;
        String message;
        int step;
        int etaMinutes;

        if (elapsedSeconds < 15) {
            stage = "confirmed";
            label = "Order confirmed";
            message = "The shop has received your order.";
            step = 0;
            etaMinutes = 3;
        } else if (elapsedSeconds < 60) {
            stage = "preparing";
            label = "Being prepared";
            message = "The shop is getting your items ready.";
            step = 1;
            etaMinutes = 3;
        } else if (elapsedSeconds < 180) {
            stage = "on_the_way";
            label = "On the way";
            message = "The demo route is moving toward your delivery area.";
            step = 2;
            etaMinutes = (int) Math.ceil((180 - elapsedSeconds) / 60.0);
        } else {
            stage = "delivered";
            label = "Delivered";
            message = "The simulated delivery is complete.";
            step = 3;
            etaMinutes = 0;
        }

        Map<String, Object> tracking = new LinkedHashMap<>();
        tracking.put("demo", true);
        tracking.put("stage", stage);
        tracking.put("label", label);
        tracking.put("message", message);
        tracking.put("step", step);
        tracking.put("etaMinutes", etaMinutes);
        tracking.put("refreshAfterSeconds", 15);
        return tracking;
    }
}
