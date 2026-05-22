package com.iviet.ivshs.util;

import org.slf4j.MDC;
import java.util.Map;
import java.util.function.Supplier;

public class MdcTaskWrapper {

    public static Runnable wrap(Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            if (contextMap != null && !contextMap.isEmpty()) {
                MDC.setContextMap(contextMap);
            }
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }

    public static <T> Supplier<T> wrap(Supplier<T> supplier) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            if (contextMap != null && !contextMap.isEmpty()) {
                MDC.setContextMap(contextMap);
            }
            try {
                return supplier.get();
            } finally {
                MDC.clear();
            }
        };
    }
}
