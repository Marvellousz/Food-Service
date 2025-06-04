# Operations Runbook

This document provides operational procedures for the Food Service application, including troubleshooting, incident response, and maintenance tasks.

## Table of Contents

- [Service Health Checks](#service-health-checks)
- [Common Issues and Solutions](#common-issues-and-solutions)
- [Incident Response Procedures](#incident-response-procedures)
- [Maintenance Tasks](#maintenance-tasks)
- [Monitoring and Alerting](#monitoring-and-alerting)
- [Log Analysis](#log-analysis)
- [Scaling Operations](#scaling-operations)
- [Backup and Recovery](#backup-and-recovery)

## Service Health Checks

### Application Health Endpoints

```bash
# Basic health check
curl -k https://your-domain/actuator/health

# Detailed health information
curl -k https://your-domain/actuator/health/readiness
curl -k https://your-domain/actuator/health/liveness

# Application info
curl -k https://your-domain/actuator/info

# Metrics endpoint
curl -k https://your-domain/actuator/metrics
```

### Kubernetes Health Checks

```bash
# Check pod status
kubectl get pods -n food-service

# Check service status
kubectl get services -n food-service

# Check ingress status
kubectl get ingress -n food-service

# Describe pod for detailed info
kubectl describe pod <pod-name> -n food-service

# Check logs
kubectl logs <pod-name> -n food-service --tail=100 -f
```

## Common Issues and Solutions

### Issue: Application Won't Start

**Symptoms:**
- Pod in CrashLoopBackOff state
- Health checks failing
- Application logs show startup errors

**Diagnostic Steps:**
```bash
# Check pod events
kubectl describe pod <pod-name> -n food-service

# Check application logs
kubectl logs <pod-name> -n food-service --previous

# Check resource usage
kubectl top pod <pod-name> -n food-service
```

**Common Solutions:**
1. **Insufficient Resources:**
   ```bash
   # Check resource limits
   kubectl describe pod <pod-name> -n food-service | grep -A 5 "Limits\|Requests"
   ```
   - Increase memory/CPU limits in deployment
   - Check JVM heap size configuration

2. **Configuration Issues:**
   ```bash
   # Check ConfigMap
   kubectl get configmap -n food-service
   kubectl describe configmap <configmap-name> -n food-service
   ```
   - Verify application.yml properties
   - Check SSL certificate paths

3. **SSL Certificate Issues:**
   ```bash
   # Check secret
   kubectl get secrets -n food-service
   kubectl describe secret food-service-ssl -n food-service
   ```
   - Verify certificate validity
   - Check certificate mounting in pod

### Issue: High Response Times

**Symptoms:**
- Slow API responses
- Timeouts in client applications
- High CPU/memory usage

**Diagnostic Steps:**
```bash
# Check application metrics
curl -k https://your-domain/actuator/metrics/http.server.requests

# Monitor resource usage
kubectl top pod -n food-service --containers

# Check JVM metrics
curl -k https://your-domain/actuator/metrics/jvm.memory.used
curl -k https://your-domain/actuator/metrics/jvm.gc.pause
```

**Solutions:**
1. **JVM Tuning:**
   - Adjust heap size: `-Xmx512m -Xms512m`
   - Enable G1GC: `-XX:+UseG1GC`
   - Tune GC settings: `-XX:MaxGCPauseMillis=200`

2. **Application Performance:**
   - Enable caching where appropriate
   - Optimize database queries (if applicable)
   - Review thread pool configurations

3. **Scale Resources:**
   ```bash
   # Scale replicas
   kubectl scale deployment food-service --replicas=3 -n food-service
   
   # Update resource limits
   kubectl patch deployment food-service -n food-service -p '{"spec":{"template":{"spec":{"containers":[{"name":"food-service","resources":{"limits":{"memory":"1Gi","cpu":"1000m"}}}]}}}}'
   ```

### Issue: SSL/TLS Connection Errors

**Symptoms:**
- SSL handshake failures
- Certificate validation errors
- Browser security warnings

**Diagnostic Steps:**
```bash
# Test SSL connection
openssl s_client -connect your-domain:443 -servername your-domain

# Check certificate details
curl -vI https://your-domain 2>&1 | grep -A 10 -B 10 certificate

# Verify certificate in cluster
kubectl get secret food-service-ssl -n food-service -o yaml | base64 -d
```

**Solutions:**
1. **Certificate Renewal:**
   ```bash
   # Update certificate secret
   kubectl create secret tls food-service-ssl \
     --cert=path/to/new/cert.pem \
     --key=path/to/new/key.pem \
     -n food-service --dry-run=client -o yaml | kubectl apply -f -
   ```

2. **Configuration Fix:**
   - Verify keystore password
   - Check certificate format (PEM vs PKCS12)
   - Ensure certificate chain is complete

## Incident Response Procedures

### Severity Levels

- **P0 (Critical):** Complete service outage
- **P1 (High):** Major functionality impacted
- **P2 (Medium):** Minor functionality impacted
- **P3 (Low):** Enhancement or cosmetic issues

### Response Procedures

#### P0/P1 Incident Response

1. **Immediate Actions (0-5 minutes):**
   ```bash
   # Check service status
   kubectl get pods -n food-service
   kubectl get services -n food-service
   
   # Quick restart if needed
   kubectl rollout restart deployment/food-service -n food-service
   ```

2. **Investigation (5-15 minutes):**
   ```bash
   # Gather logs
   kubectl logs -l app=food-service -n food-service --tail=200 > incident-logs.txt
   
   # Check recent changes
   kubectl rollout history deployment/food-service -n food-service
   
   # Monitor metrics
   curl -k https://your-domain/actuator/metrics/system.cpu.usage
   ```

3. **Resolution (15+ minutes):**
   - Implement fix or rollback
   - Verify service restoration
   - Document incident and resolution

#### Rollback Procedure

```bash
# Check rollout history
kubectl rollout history deployment/food-service -n food-service

# Rollback to previous version
kubectl rollout undo deployment/food-service -n food-service

# Rollback to specific revision
kubectl rollout undo deployment/food-service --to-revision=2 -n food-service

# Verify rollback
kubectl rollout status deployment/food-service -n food-service
```

## Maintenance Tasks

### Regular Maintenance Schedule

#### Daily Tasks
- Monitor application health and performance metrics
- Review error logs for unusual patterns
- Check resource utilization

#### Weekly Tasks
- Review and rotate logs
- Update monitoring dashboards
- Perform security scans

#### Monthly Tasks
- Review and update SSL certificates
- Performance baseline review
- Capacity planning assessment

### Certificate Management

```bash
# Check certificate expiration
openssl x509 -in certificate.crt -text -noout | grep "Not After"

# Generate new certificate (example with Let's Encrypt)
certbot certonly --dns-route53 -d your-domain.com

# Update Kubernetes secret
kubectl create secret tls food-service-ssl \
  --cert=/etc/letsencrypt/live/your-domain.com/fullchain.pem \
  --key=/etc/letsencrypt/live/your-domain.com/privkey.pem \
  -n food-service --dry-run=client -o yaml | kubectl apply -f -
```

## Monitoring and Alerting

### Key Metrics to Monitor

1. **Application Metrics:**
   - Response time (95th percentile < 500ms)
   - Error rate (< 1%)
   - Throughput (requests per second)
   - Active connections

2. **Infrastructure Metrics:**
   - CPU usage (< 80%)
   - Memory usage (< 80%)
   - Disk usage (< 80%)
   - Network I/O

3. **Business Metrics:**
   - API endpoint usage
   - Food item search patterns
   - Peak usage times

### Alerting Rules

```yaml
# Example Prometheus alerting rules
groups:
  - name: food-service
    rules:
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.01
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High error rate detected"
          
      - alert: HighResponseTime
        expr: histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m])) > 0.5
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time detected"
```

## Log Analysis

### Log Locations

```bash
# Application logs in Kubernetes
kubectl logs -l app=food-service -n food-service --tail=100

# Access logs (if enabled)
kubectl logs -l app=food-service -n food-service --tail=100 | grep "HTTP"

# Error logs
kubectl logs -l app=food-service -n food-service --tail=100 | grep -i error
```

### Common Log Patterns

```bash
# Find errors in last hour
kubectl logs -l app=food-service -n food-service --since=1h | grep -i "error\|exception\|failed"

# Monitor specific endpoint
kubectl logs -l app=food-service -n food-service -f | grep "/api/foods"

# Check SSL-related issues
kubectl logs -l app=food-service -n food-service | grep -i "ssl\|tls\|certificate"
```

## Scaling Operations

### Horizontal Pod Autoscaler (HPA)

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: food-service-hpa
  namespace: food-service
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
```

### Manual Scaling

```bash
# Scale up
kubectl scale deployment food-service --replicas=5 -n food-service

# Scale down
kubectl scale deployment food-service --replicas=2 -n food-service

# Check scaling status
kubectl get hpa -n food-service
kubectl describe hpa food-service-hpa -n food-service
```

## Backup and Recovery

### Configuration Backup

```bash
# Backup Kubernetes configurations
kubectl get all -n food-service -o yaml > food-service-backup.yaml

# Backup secrets
kubectl get secrets -n food-service -o yaml > food-service-secrets-backup.yaml

# Backup configmaps
kubectl get configmaps -n food-service -o yaml > food-service-configmaps-backup.yaml
```

### Recovery Procedures

```bash
# Restore from backup
kubectl apply -f food-service-backup.yaml

# Verify restoration
kubectl get pods -n food-service
kubectl get services -n food-service
```

## Contact Information

### Escalation Contacts

- **L1 Support:** [Your on-call team]
- **L2 Support:** [Development team lead]
- **L3 Support:** [Architecture team]

### Communication Channels

- **Incident Channel:** [Slack/Teams channel]
- **Status Page:** [Internal status page URL]
- **Documentation:** [Wiki/Confluence URL]

---

**Last Updated:** [Current Date]
**Document Owner:** [Operations Team]
**Review Schedule:** Monthly
