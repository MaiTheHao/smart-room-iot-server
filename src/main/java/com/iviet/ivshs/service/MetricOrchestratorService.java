package com.iviet.ivshs.service;


import com.iviet.ivshs.enumeration.MetricDomain;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.service.strategy.MetricServiceStrategy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MetricOrchestratorService {

    private final Map<MetricDomain, MetricServiceStrategy> strategies;

    public MetricOrchestratorService(List<MetricServiceStrategy> strategyList) {
        this.strategies = strategyList.stream().collect(Collectors.toMap(MetricServiceStrategy::getSupportedDomain, strategy -> strategy));
    }

    private MetricServiceStrategy getStrategy(MetricDomain domain) {
        MetricServiceStrategy strategy = strategies.get(domain);
        if (strategy == null) throw new BadRequestException("Metric domain " + domain.name() + " is not supported");   
        return strategy;
    }

    public Object getLatest(MetricDomain domain, String category, Long targetId) {
        return getStrategy(domain).getLatest(category, targetId);
    }

    public List<?> getHistory(MetricDomain domain, String category, Long targetId, Instant from, Instant to) {
        if (from == null || to == null) {
            throw new BadRequestException("Parameters 'from' and 'to' are required for history queries");
        }
        return getStrategy(domain).getHistory(category, targetId, from, to);
    }
}
