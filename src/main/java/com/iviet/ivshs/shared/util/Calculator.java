package com.iviet.ivshs.shared.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

public final class Calculator {

    private Calculator() {}

    public static Optional<Double> mean(Collection<Double> values) {
        OptionalDouble result = clean(values).stream()
                .mapToDouble(Double::doubleValue)
                .average();
        return result.isPresent() ? Optional.of(result.getAsDouble()) : Optional.empty();
    }

    public static Optional<Double> median(Collection<Double> values) {
        List<Double> sorted = clean(values).stream()
                .sorted()
                .toList();
        if (sorted.isEmpty()) return Optional.empty();
        int n = sorted.size();
        if (n % 2 == 1) return Optional.of(sorted.get(n / 2));
        return Optional.of((sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0);
    }

    public static Optional<Double> max(Collection<Double> values) {
        return clean(values).stream().max(Comparator.naturalOrder());
    }

    public static Optional<Double> min(Collection<Double> values) {
        return clean(values).stream().min(Comparator.naturalOrder());
    }

    public static Optional<Double> sum(Collection<Double> values) {
        List<Double> cleaned = clean(values);
        if (cleaned.isEmpty()) return Optional.empty();
        return Optional.of(cleaned.stream().mapToDouble(Double::doubleValue).sum());
    }

    public static Optional<Double> range(Collection<Double> values) {
        List<Double> cleaned = clean(values);
        if (cleaned.isEmpty()) return Optional.empty();
        double lo = cleaned.stream().mapToDouble(Double::doubleValue).min().getAsDouble();
        double hi = cleaned.stream().mapToDouble(Double::doubleValue).max().getAsDouble();
        return Optional.of(hi - lo);
    }

    public static double meanOrDefault(Collection<Double> values, double fallback) {
        return mean(values).orElse(fallback);
    }

    public static double medianOrDefault(Collection<Double> values, double fallback) {
        return median(values).orElse(fallback);
    }

    public static double maxOrDefault(Collection<Double> values, double fallback) {
        return max(values).orElse(fallback);
    }

    public static double minOrDefault(Collection<Double> values, double fallback) {
        return min(values).orElse(fallback);
    }

    private static List<Double> clean(Collection<Double> values) {
        if (values == null) return List.of();
        return values.stream().filter(Objects::nonNull).toList();
    }
}
