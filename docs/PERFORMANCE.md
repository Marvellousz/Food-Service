# Performance Optimization Guide

## 📊 Overview

This guide provides comprehensive performance optimization strategies for the Food Service Spring Boot application, covering JVM tuning, application optimization, monitoring, and troubleshooting.

## Table of Contents

- [📊 Overview](#-overview)
- [🎯 Performance Targets](#-performance-targets)
- [⚡ JVM Optimization](#-jvm-optimization)
- [🔧 Application Optimization](#-application-optimization)
- [🐳 Container Optimization](#-container-optimization)
- [☸️ Kubernetes Optimization](#️-kubernetes-optimization)
- [📈 Monitoring and Metrics](#-monitoring-and-metrics)
- [🔍 Performance Testing](#-performance-testing)
- [🐛 Troubleshooting](#-troubleshooting)
- [📋 Optimization Checklist](#-optimization-checklist)

## 🎯 Performance Targets

### Application Metrics

| Metric | Target | Current | Notes |
|--------|---------|---------|-------|
| Startup Time | < 30 seconds | ~25 seconds | Cold start in container |
| Response Time (P95) | < 200ms | ~150ms | API endpoints |
| Response Time (P99) | < 500ms | ~300ms | Complex operations |
| Throughput | > 1000 RPS | ~800 RPS | Concurrent requests |
| Memory Usage | < 256MB | ~200MB | Per pod |
| CPU Usage | < 200m | ~150m | Per pod |

### Resource Utilization

```yaml
# Production Resource Targets
resources:
  requests:
    memory: "128Mi"
    cpu: "100m"
  limits:
    memory: "256Mi"
    cpu: "200m"
```

## ⚡ JVM Optimization

### Container-Optimized JVM Settings

```bash
# Production JVM Configuration
JAVA_OPTS="
-Xmx256m
-Xms128m
-XX:+UseContainerSupport
-XX:MaxRAMPercentage=75
-XX:InitialRAMPercentage=50
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m
-XX:+UnlockExperimentalVMOptions
-XX:+UseCGroupMemoryLimitForHeap
-XX:+PrintGC
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-Djava.security.egd=file:/dev/./urandom
"
```

### Memory Management

```yaml
# Kubernetes Deployment with JVM Settings
apiVersion: apps/v1
kind: Deployment
metadata:
  name: food-service
spec:
  template:
    spec:
      containers:
      - name: food-service
        image: food-service:latest
        env:
        - name: JAVA_OPTS
          value: "-Xmx256m -Xms128m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "200m"
```

### Garbage Collection Tuning

```bash
# G1GC Configuration (Recommended for containers)
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m
-XX:G1NewSizePercent=30
-XX:G1MaxNewSizePercent=40
-XX:G1MixedGCLiveThresholdPercent=85

# Parallel GC Configuration (Alternative)
-XX:+UseParallelGC
-XX:ParallelGCThreads=2
-XX:MaxGCPauseMillis=200

# ZGC Configuration (Java 17+, Low Latency)
-XX:+UseZGC
-XX:+UnlockExperimentalVMOptions
```

## 🔧 Application Optimization

### Spring Boot Configuration

```yaml
# application-production.yml
spring:
  main:
    lazy-initialization: true
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false
  jackson:
    serialization:
      write-dates-as-timestamps: false
  
server:
  tomcat:
    threads:
      max: 200
      min-spare: 10
    connection-timeout: 20000
    accept-count: 100
    max-connections: 8192
  compression:
    enabled: true
    mime-types: text/html,text/css,application/javascript,application/json
    min-response-size: 1024

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### Caching Strategy

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }
    
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(Duration.ofMinutes(30))
                .recordStats();
    }
}

@Service
public class FoodServiceImpl implements FoodService {
    
    @Cacheable(value = "foods", key = "#id")
    public Food getFoodById(Long id) {
        // Implementation
    }
    
    @CacheEvict(value = "foods", allEntries = true)
    public Food updateFood(Food food) {
        // Implementation
    }
}
```

### Connection Pool Optimization

```yaml
# Database Connection Pool (if using database)
spring:
  datasource:
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000
      connection-timeout: 20000
      max-lifetime: 1200000
      leak-detection-threshold: 60000
```

### Async Processing

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}

@Service
public class AsyncFoodService {
    
    @Async("taskExecutor")
    public CompletableFuture<Void> processLargeBatch(List<Food> foods) {
        // Heavy processing
        return CompletableFuture.completedFuture(null);
    }
}
```

## 🐳 Container Optimization

### Multi-Stage Dockerfile

```dockerfile
# Optimized Dockerfile for production
FROM openjdk:17-jdk-slim as builder

# Install build dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .

# Build with optimizations
RUN ./mvnw clean package -DskipTests -Dspring.profiles.active=production

# Production stage
FROM openjdk:17-jdk-slim

# Install runtime dependencies and security updates
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

# Copy application
COPY --from=builder --chown=appuser:appgroup /app/target/food-service-0.0.1-SNAPSHOT.jar app.jar

# Set security context
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-Xmx256m -Xms128m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

EXPOSE 8080 8443

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Image Optimization

```bash
# Build optimized image
docker build --target production -t food-service:optimized .

# Analyze image layers
docker history food-service:optimized

# Security scanning
docker scout cves food-service:optimized

# Image size comparison
docker images | grep food-service
```

## ☸️ Kubernetes Optimization

### Resource Management

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: food-service
  labels:
    app: food-service
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: food-service
  template:
    metadata:
      labels:
        app: food-service
    spec:
      containers:
      - name: food-service
        image: food-service:optimized
        ports:
        - containerPort: 8080
        - containerPort: 8443
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: JAVA_OPTS
          value: "-Xmx256m -Xms128m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "200m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 90
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        startupProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 12
```

### Horizontal Pod Autoscaler

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: food-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: food-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 100
        periodSeconds: 60
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
```

### Pod Disruption Budget

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: food-service-pdb
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: food-service
```

## 📈 Monitoring and Metrics

### Custom Metrics

```java
@Component
public class PerformanceMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Timer requestTimer;
    private final Counter errorCounter;
    private final Gauge memoryGauge;
    
    public PerformanceMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.requestTimer = Timer.builder("http.requests")
                .description("HTTP request duration")
                .register(meterRegistry);
        this.errorCounter = Counter.builder("http.errors")
                .description("HTTP error count")
                .register(meterRegistry);
        this.memoryGauge = Gauge.builder("jvm.memory.used.percentage")
                .description("JVM memory usage percentage")
                .register(meterRegistry, this, PerformanceMetrics::getMemoryUsagePercentage);
    }
    
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordError() {
        errorCounter.increment();
    }
    
    private double getMemoryUsagePercentage() {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long max = runtime.maxMemory();
        return (double) used / max * 100;
    }
}
```

### Micrometer Configuration

```java
@Configuration
public class MetricsConfig {
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
    
    @Bean
    public MeterFilter meterFilter() {
        return MeterFilter.deny(id -> {
            String name = id.getName();
            return name.startsWith("jvm.gc.pause") || 
                   name.startsWith("jvm.compilation");
        });
    }
}
```

### Prometheus Metrics

```yaml
# prometheus-config.yml
global:
  scrape_interval: 15s

scrape_configs:
- job_name: 'food-service'
  kubernetes_sd_configs:
  - role: pod
  relabel_configs:
  - source_labels: [__meta_kubernetes_pod_label_app]
    action: keep
    regex: food-service
  - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
    action: keep
    regex: true
  - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_path]
    action: replace
    target_label: __metrics_path__
    regex: (.+)
  - source_labels: [__address__, __meta_kubernetes_pod_annotation_prometheus_io_port]
    action: replace
    regex: ([^:]+)(?::\d+)?;(\d+)
    replacement: $1:$2
    target_label: __address__
```

## 🔍 Performance Testing

### Load Testing with JMeter

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="Food Service Load Test">
      <elementProp name="TestPlan.arguments" elementType="Arguments" guiclass="ArgumentsPanel">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="Load Test">
        <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <stringProp name="LoopController.loops">100</stringProp>
        </elementProp>
        <stringProp name="ThreadGroup.num_threads">50</stringProp>
        <stringProp name="ThreadGroup.ramp_time">60</stringProp>
      </ThreadGroup>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

### Artillery Load Testing

```yaml
# artillery-config.yml
config:
  target: 'http://localhost:8080'
  phases:
    - duration: 60
      arrivalRate: 10
      name: "Warm up"
    - duration: 120
      arrivalRate: 50
      name: "Load test"
    - duration: 60
      arrivalRate: 100
      name: "Stress test"
  processor: "./custom-functions.js"

scenarios:
  - name: "Get all foods"
    weight: 60
    flow:
      - get:
          url: "/food-service/api/foods"
          
  - name: "Get food by ID"
    weight: 30
    flow:
      - get:
          url: "/food-service/api/foods/{{ $randomInt(1, 10) }}"
          
  - name: "Health check"
    weight: 10
    flow:
      - get:
          url: "/actuator/health"
```

### Performance Test Script

```bash
#!/bin/bash
# performance-test.sh

set -e

echo "Starting Performance Tests..."

# Start application
docker-compose up -d food-service

# Wait for startup
echo "Waiting for application startup..."
timeout 60 bash -c 'until curl -f http://localhost:8080/actuator/health; do sleep 5; done'

# Run load tests
echo "Running load tests with Artillery..."
npx artillery run artillery-config.yml --output report.json

# Generate report
npx artillery report report.json

# Run JMeter tests
echo "Running JMeter tests..."
jmeter -n -t food-service-load-test.jmx -l results.jtl -e -o jmeter-report

# Cleanup
docker-compose down

echo "Performance tests completed!"
echo "Results available in:"
echo "- Artillery: report.json"
echo "- JMeter: jmeter-report/"
```

## 🐛 Troubleshooting

### Memory Issues

```bash
# Monitor memory usage
kubectl top pods -n food-service

# Check JVM memory details
kubectl exec -it <pod-name> -- jcmd 1 VM.info

# Generate heap dump
kubectl exec -it <pod-name> -- jcmd 1 GC.run_finalization
kubectl exec -it <pod-name> -- jcmd 1 VM.dump_heap /tmp/heap.hprof

# Copy heap dump for analysis
kubectl cp <pod-name>:/tmp/heap.hprof ./heap.hprof
```

### CPU Issues

```bash
# Check CPU usage
kubectl top pods -n food-service --sort-by=cpu

# Profile application
kubectl exec -it <pod-name> -- jcmd 1 JFR.start duration=60s filename=/tmp/profile.jfr

# Copy profile for analysis
kubectl cp <pod-name>:/tmp/profile.jfr ./profile.jfr
```

### Slow Startup

```yaml
# Optimize startup with lazy initialization
spring:
  main:
    lazy-initialization: true
  jpa:
    defer-datasource-initialization: true

# Adjust probe timings
startupProbe:
  initialDelaySeconds: 60  # Increase for slow startup
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 18     # Allow 3 minutes total
```

### High Latency

```java
// Add request timing
@RestController
public class FoodController {
    
    private final MeterRegistry meterRegistry;
    
    @GetMapping("/foods")
    @Timed(name = "food.requests", description = "Time taken to fetch foods")
    public List<Food> getAllFoods() {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            return foodService.getAllFoods();
        } finally {
            sample.stop(Timer.builder("food.request.duration")
                    .tag("endpoint", "getAllFoods")
                    .register(meterRegistry));
        }
    }
}
```

### Garbage Collection Issues

```bash
# GC Logging
-XX:+PrintGC
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+PrintGCApplicationStoppedTime
-Xloggc:/tmp/gc.log

# Analyze GC logs
kubectl logs <pod-name> | grep GC

# GC tuning for low latency
-XX:+UseG1GC
-XX:MaxGCPauseMillis=50
-XX:G1HeapRegionSize=16m
```

## 📋 Optimization Checklist

### ✅ JVM Optimization
- [ ] Container-aware JVM settings configured
- [ ] Appropriate GC algorithm selected (G1GC recommended)
- [ ] Memory settings optimized for container limits
- [ ] GC logging enabled for monitoring
- [ ] Security random number generator optimized

### ✅ Application Optimization
- [ ] Lazy initialization enabled where appropriate
- [ ] Caching implemented for frequently accessed data
- [ ] Connection pools properly configured
- [ ] Async processing for non-blocking operations
- [ ] Resource cleanup implemented

### ✅ Container Optimization
- [ ] Multi-stage Docker build implemented
- [ ] Minimal base image used
- [ ] Non-root user configured
- [ ] Health checks implemented
- [ ] Security scanning performed

### ✅ Kubernetes Optimization
- [ ] Resource requests and limits set
- [ ] Horizontal Pod Autoscaler configured
- [ ] Pod Disruption Budget defined
- [ ] Proper probe configurations
- [ ] Rolling update strategy optimized

### ✅ Monitoring
- [ ] Custom metrics implemented
- [ ] Prometheus metrics exposed
- [ ] Grafana dashboards created
- [ ] Alerting rules configured
- [ ] Log aggregation setup

### ✅ Testing
- [ ] Load testing scenarios defined
- [ ] Performance benchmarks established
- [ ] Regular performance testing automated
- [ ] Performance regression detection
- [ ] Capacity planning performed

---

## 📚 Additional Resources

- [Spring Boot Performance Tuning](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.performance)
- [JVM Performance Tuning](https://docs.oracle.com/en/java/javase/17/gctuning/)
- [Kubernetes Best Practices](https://kubernetes.io/docs/concepts/cluster-administration/manage-deployment/)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Container Performance Tuning](https://developers.redhat.com/articles/2017/03/14/java-inside-docker)

*Last Updated: December 2024*
