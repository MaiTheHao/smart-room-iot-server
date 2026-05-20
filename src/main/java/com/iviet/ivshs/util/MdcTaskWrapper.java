package com.iviet.ivshs.util;

import org.apache.logging.log4j.ThreadContext;
import java.util.Map;
import java.util.function.Supplier;

public class MdcTaskWrapper {

    public static Runnable wrap(Runnable runnable) {
        Map<String, String> contextMap = ThreadContext.getImmutableContext();
        return () -> {
            if (contextMap != null && !contextMap.isEmpty()) {
                ThreadContext.putAll(contextMap);
            }
            try {
                runnable.run();
            } finally {
                ThreadContext.clearAll();
            }
        };
    }

    public static <T> Supplier<T> wrap(Supplier<T> supplier) {
        Map<String, String> contextMap = ThreadContext.getImmutableContext();
        return () -> {
            if (contextMap != null && !contextMap.isEmpty()) {
                ThreadContext.putAll(contextMap);
            }
            try {
                return supplier.get();
            } finally {
                ThreadContext.clearAll();
            }
        };
    }
}
