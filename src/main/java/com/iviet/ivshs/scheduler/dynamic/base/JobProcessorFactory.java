package com.iviet.ivshs.scheduler.dynamic.base;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class JobProcessorFactory {
    private final Map<JobProcessorType, SchedulableJobProcessor> processors;

    public JobProcessorFactory(List<SchedulableJobProcessor> processorList) {
        this.processors = processorList.stream()
                .collect(Collectors.toMap(SchedulableJobProcessor::getProcessorType, Function.identity()));
    }

    public SchedulableJobProcessor getProcessor(JobProcessorType type) {
        SchedulableJobProcessor processor = processors.get(type);
        if (processor == null) {
            throw new IllegalArgumentException("No processor found for type: " + type);
        }
        return processor;
    }
}
