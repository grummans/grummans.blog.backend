package com.grummans.noyblog.configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final MeterRegistry meterRegistry;
    private static final long BOTTLENECK_THRESHOLD_MS = 500;

    @Around("execution(* com.grummans.noyblog.controller..*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        return logWithLayer(joinPoint, "CONTROLLER");
    }

    @Around("execution(* com.grummans.noyblog.services..*(..))")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        return logWithLayer(joinPoint, "SERVICE");
    }

    @Around("execution(* com.grummans.noyblog.repository..*(..))")
    public Object logRepository(ProceedingJoinPoint joinPoint) throws Throwable {
        return logWithLayer(joinPoint, "REPOSITORY");
    }

    private Object logWithLayer(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        MDC.put("layer", layer);
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "."
                + joinPoint.getSignature().getName();
        long start = System.currentTimeMillis();
        String previousLayer = MDC.get("layer");
        try {
            Object result = joinPoint.proceed();
            recordExecution(methodName, layer, start);
            return result;
        } catch (Exception e) {
            log.error("{} threw exception: {}", methodName, e.getMessage());
            throw e;
        } finally {
            if (previousLayer != null) {
                MDC.put("layer", previousLayer); // restore
            } else {
                MDC.remove("layer");
            }
        }
    }

    private void recordExecution(String methodName, String layer, long startTime) {
        long duration = System.currentTimeMillis() - startTime;

        // Record Micrometer metric for Prometheus/Grafana
        Timer.builder("api.execution.time")
                .tag("layer", layer)
                .tag("method", methodName)
                .register(meterRegistry)
                .record(duration, TimeUnit.MILLISECONDS);

        // Bottleneck detection
        if (duration > BOTTLENECK_THRESHOLD_MS) {
            log.warn("[BOTTLENECK] {} executed in {} ms", methodName, duration);
        } else {
            log.info("{} executed in {} ms", methodName, duration);
        }
    }

}
