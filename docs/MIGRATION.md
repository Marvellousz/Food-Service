# Migration and Upgrade Guide

This document provides guidance for migrating the Food Service application between versions, environments, and infrastructures, as well as upgrading dependencies and frameworks.

## Table of Contents

- [Version Upgrade Guide](#version-upgrade-guide)
- [Spring Boot Upgrades](#spring-boot-upgrades)
- [Java Version Migration](#java-version-migration)
- [Database Migration](#database-migration)
- [Container Platform Migration](#container-platform-migration)
- [Cloud Provider Migration](#cloud-provider-migration)
- [SSL Certificate Migration](#ssl-certificate-migration)
- [Configuration Migration](#configuration-migration)
- [Rollback Procedures](#rollback-procedures)
- [Testing Migration Changes](#testing-migration-changes)

## Version Upgrade Guide

### Pre-Upgrade Checklist

- [ ] Review release notes and breaking changes
- [ ] Backup current configuration and data
- [ ] Test upgrade in development environment
- [ ] Prepare rollback plan
- [ ] Schedule maintenance window
- [ ] Notify stakeholders

### Application Version Upgrade Process

1. **Preparation Phase**
   ```bash
   # Backup current deployment
   kubectl get all -n food-service -o yaml > backup-$(date +%Y%m%d).yaml
   
   # Create configuration backup
   kubectl get configmaps,secrets -n food-service -o yaml > config-backup-$(date +%Y%m%d).yaml
   ```

2. **Staging Deployment**
   ```bash
   # Deploy to staging first
   kubectl set image deployment/food-service food-service=your-registry/food-service:v2.0.0 -n food-service-staging
   
   # Monitor deployment
   kubectl rollout status deployment/food-service -n food-service-staging
   ```

3. **Validation Tests**
   ```bash
   # Run API tests
   curl -k https://staging.food-service.com/api/foods
   curl -k https://staging.food-service.com/actuator/health
   
   # Performance validation
   mvn test -Dtest=PerformanceTest -Dapi.base.url=https://staging.food-service.com
   ```

4. **Production Deployment**
   ```bash
   # Rolling update in production
   kubectl set image deployment/food-service food-service=your-registry/food-service:v2.0.0 -n food-service
   
   # Watch rollout progress
   kubectl rollout status deployment/food-service -n food-service --timeout=600s
   ```

### Version Compatibility Matrix

| Application Version | Spring Boot | Java | Kubernetes | Minimum Resources |
|---------------------|-------------|------|------------|-------------------|
| v1.0.x              | 3.1.x       | 17   | 1.25+      | 256Mi/500m        |
| v1.1.x              | 3.2.x       | 17   | 1.26+      | 256Mi/500m        |
| v2.0.x              | 3.2.x       | 21   | 1.27+      | 512Mi/1000m       |

## Spring Boot Upgrades

### Spring Boot 3.1 to 3.2 Migration

1. **Update Dependencies**
   ```xml
   <parent>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-parent</artifactId>
       <version>3.2.0</version>
       <relativePath/>
   </parent>
   ```

2. **Configuration Changes**
   ```yaml
   # application.yml updates for 3.2
   management:
     endpoints:
       web:
         exposure:
           include: "health,info,metrics,prometheus"
     endpoint:
       health:
         probes:
           enabled: true
   ```

3. **Code Changes**
   ```java
   // Update deprecated methods
   @ConfigurationProperties(prefix = "food.service")
   public class FoodServiceProperties {
       // New validation approach
       @Valid
       private Security security = new Security();
       
       // Getters and setters...
   }
   ```

### Breaking Changes Checklist

- [ ] Update configuration property names
- [ ] Replace deprecated APIs
- [ ] Update security configurations
- [ ] Verify actuator endpoint changes
- [ ] Test SSL/TLS configurations

## Java Version Migration

### Java 17 to Java 21 Migration

1. **Update Base Image**
   ```dockerfile
   FROM openjdk:21-jre-slim
   ```

2. **Maven Configuration**
   ```xml
   <properties>
       <java.version>21</java.version>
       <maven.compiler.source>21</maven.compiler.source>
       <maven.compiler.target>21</maven.compiler.target>
   </properties>
   ```

3. **JVM Arguments Update**
   ```yaml
   # Kubernetes deployment
   env:
   - name: JAVA_OPTS
     value: "-Xmx512m -Xms512m -XX:+UseZGC -XX:+UnlockExperimentalVMOptions"
   ```

### Java 21 Features to Leverage

1. **Virtual Threads**
   ```java
   @Configuration
   public class ThreadConfig {
       @Bean
       public TaskExecutor taskExecutor() {
           return new VirtualThreadTaskExecutor("virtual-");
       }
   }
   ```

2. **Pattern Matching**
   ```java
   // Updated switch expressions
   public String processFood(Object food) {
       return switch (food) {
           case String s -> "Food name: " + s;
           case Food f -> "Food: " + f.getName();
           default -> "Unknown food type";
       };
   }
   ```

## Database Migration

### Adding Database Support

1. **Dependencies**
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-jpa</artifactId>
   </dependency>
   <dependency>
       <groupId>org.postgresql</groupId>
       <artifactId>postgresql</artifactId>
       <scope>runtime</scope>
   </dependency>
   <dependency>
       <groupId>org.flywaydb</groupId>
       <artifactId>flyway-core</artifactId>
   </dependency>
   ```

2. **Configuration**
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/foodservice
       username: ${DB_USERNAME}
       password: ${DB_PASSWORD}
     jpa:
       hibernate:
         ddl-auto: validate
       show-sql: false
     flyway:
       enabled: true
       locations: classpath:db/migration
   ```

3. **Migration Scripts**
   ```sql
   -- V1__Create_food_table.sql
   CREATE TABLE foods (
       id BIGSERIAL PRIMARY KEY,
       name VARCHAR(255) NOT NULL,
       description TEXT,
       price DECIMAL(10,2),
       category VARCHAR(100),
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );
   
   CREATE INDEX idx_foods_category ON foods(category);
   CREATE INDEX idx_foods_name ON foods(name);
   ```

### Database Migration Process

1. **Pre-Migration**
   ```bash
   # Backup existing data (if any)
   kubectl exec -it deployment/postgres -- pg_dump -U postgres foodservice > backup.sql
   ```

2. **Migration Execution**
   ```bash
   # Run Flyway migration
   mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/foodservice
   
   # Verify migration
   mvn flyway:info
   ```

3. **Data Migration**
   ```java
   @Component
   public class DataMigrationRunner implements CommandLineRunner {
       @Override
       public void run(String... args) {
           // Migrate existing data if needed
           migrateFromMemoryToDatabase();
       }
   }
   ```

## Container Platform Migration

### Docker to Podman Migration

1. **Update CI/CD Scripts**
   ```bash
   # Replace docker commands with podman
   podman build -t food-service:latest .
   podman push registry.example.com/food-service:latest
   ```

2. **Rootless Configuration**
   ```dockerfile
   # Podman-optimized Dockerfile
   FROM registry.redhat.io/ubi8/openjdk-21-runtime:latest
   USER 1001
   COPY --chown=1001:1001 target/*.jar app.jar
   EXPOSE 8443
   ENTRYPOINT ["java", "-jar", "app.jar"]
   ```

### OpenShift Migration

1. **Security Context Constraints**
   ```yaml
   apiVersion: v1
   kind: SecurityContextConstraints
   metadata:
     name: food-service-scc
   allowHostDirVolumePlugin: false
   allowHostIPC: false
   allowHostNetwork: false
   allowHostPID: false
   allowPrivilegedContainer: false
   runAsUser:
     type: MustRunAsRange
     uidRangeMin: 1000
     uidRangeMax: 2000
   ```

2. **Route Configuration**
   ```yaml
   apiVersion: route.openshift.io/v1
   kind: Route
   metadata:
     name: food-service-route
   spec:
     host: food-service.apps.cluster.example.com
     to:
       kind: Service
       name: food-service
     tls:
       termination: passthrough
   ```

## Cloud Provider Migration

### AWS to Azure Migration

1. **Container Registry Migration**
   ```bash
   # Pull from AWS ECR
   docker pull 123456789012.dkr.ecr.us-west-2.amazonaws.com/food-service:latest
   
   # Tag for Azure ACR
   docker tag 123456789012.dkr.ecr.us-west-2.amazonaws.com/food-service:latest \
     myregistry.azurecr.io/food-service:latest
   
   # Push to Azure ACR
   docker push myregistry.azurecr.io/food-service:latest
   ```

2. **Kubernetes Configuration Updates**
   ```yaml
   # Update image references
   spec:
     containers:
     - name: food-service
       image: myregistry.azurecr.io/food-service:latest
       imagePullPolicy: Always
   ```

3. **Load Balancer Configuration**
   ```yaml
   # Azure Load Balancer
   apiVersion: v1
   kind: Service
   metadata:
     name: food-service
     annotations:
       service.beta.kubernetes.io/azure-load-balancer-internal: "true"
   spec:
     type: LoadBalancer
     loadBalancerSourceRanges:
     - 10.0.0.0/16
   ```

### Multi-Cloud Deployment

1. **Terraform Configuration**
   ```hcl
   # Multi-cloud deployment
   module "aws_deployment" {
     source = "./modules/aws"
     count  = var.deploy_aws ? 1 : 0
   }
   
   module "azure_deployment" {
     source = "./modules/azure" 
     count  = var.deploy_azure ? 1 : 0
   }
   ```

## SSL Certificate Migration

### Certificate Authority Migration

1. **Generate New Certificates**
   ```bash
   # Create new certificate with new CA
   openssl genrsa -out new-private-key.pem 2048
   openssl req -new -key new-private-key.pem -out certificate.csr
   # Submit CSR to new CA
   ```

2. **Update Kubernetes Secrets**
   ```bash
   # Create new secret
   kubectl create secret tls food-service-ssl-new \
     --cert=new-certificate.pem \
     --key=new-private-key.pem \
     -n food-service
   
   # Update deployment to use new secret
   kubectl patch deployment food-service \
     -p '{"spec":{"template":{"spec":{"volumes":[{"name":"ssl-certs","secret":{"secretName":"food-service-ssl-new"}}]}}}}'
   ```

3. **Zero-Downtime Certificate Rotation**
   ```bash
   # Rolling update with new certificate
   kubectl set env deployment/food-service SSL_CERT_VERSION=v2
   kubectl rollout status deployment/food-service
   ```

### Let's Encrypt Integration

1. **Cert-Manager Setup**
   ```yaml
   apiVersion: cert-manager.io/v1
   kind: Certificate
   metadata:
     name: food-service-tls
   spec:
     secretName: food-service-ssl
     issuerRef:
       name: letsencrypt-prod
       kind: ClusterIssuer
     dnsNames:
     - api.food-service.com
   ```

## Configuration Migration

### Environment Variable Migration

1. **Old Configuration**
   ```yaml
   env:
   - name: SERVER_PORT
     value: "8443"
   - name: SSL_ENABLED
     value: "true"
   ```

2. **New Configuration**
   ```yaml
   env:
   - name: SERVER_SSL_ENABLED
     value: "true"
   - name: SERVER_SSL_KEY_STORE
     value: "/etc/ssl/keystore.p12"
   envFrom:
   - configMapRef:
       name: food-service-config
   ```

### ConfigMap Migration

1. **Migration Script**
   ```bash
   #!/bin/bash
   # Backup existing ConfigMap
   kubectl get configmap food-service-config -o yaml > old-config.yaml
   
   # Apply new ConfigMap
   kubectl apply -f new-configmap.yaml
   
   # Restart deployment to pick up changes
   kubectl rollout restart deployment/food-service
   ```

## Rollback Procedures

### Application Rollback

1. **Kubernetes Rollback**
   ```bash
   # Check rollout history
   kubectl rollout history deployment/food-service -n food-service
   
   # Rollback to previous version
   kubectl rollout undo deployment/food-service -n food-service
   
   # Rollback to specific revision
   kubectl rollout undo deployment/food-service --to-revision=3 -n food-service
   ```

2. **Database Rollback**
   ```bash
   # Flyway rollback (if supported)
   mvn flyway:undo -Dflyway.url=jdbc:postgresql://localhost:5432/foodservice
   
   # Manual rollback
   psql -U postgres -d foodservice -f rollback-script.sql
   ```

### Configuration Rollback

```bash
# Restore from backup
kubectl apply -f config-backup-$(date +%Y%m%d).yaml

# Verify restoration
kubectl get configmaps,secrets -n food-service
```

## Testing Migration Changes

### Pre-Migration Testing

1. **Environment Validation**
   ```bash
   # Test target environment
   kubectl cluster-info
   kubectl get nodes
   kubectl get storageclass
   ```

2. **Resource Validation**
   ```bash
   # Check available resources
   kubectl describe nodes | grep -A 5 "Allocated resources"
   ```

### Post-Migration Testing

1. **Functional Testing**
   ```bash
   # API endpoint tests
   curl -k https://api.food-service.com/api/foods
   curl -k https://api.food-service.com/actuator/health
   
   # Load testing
   ab -n 1000 -c 10 https://api.food-service.com/api/foods
   ```

2. **Performance Testing**
   ```bash
   # JMeter test plan
   jmeter -n -t migration-test-plan.jmx -l results.jtl
   ```

### Migration Validation Checklist

- [ ] All endpoints responding correctly
- [ ] SSL certificates valid and trusted
- [ ] Performance metrics within acceptable range
- [ ] Logs showing no errors
- [ ] Health checks passing
- [ ] Database connections working (if applicable)
- [ ] External integrations functioning
- [ ] Monitoring and alerting operational

## Troubleshooting Migration Issues

### Common Issues

1. **Certificate Issues**
   ```bash
   # Verify certificate
   openssl x509 -in certificate.pem -text -noout
   
   # Test SSL connection
   openssl s_client -connect api.food-service.com:443
   ```

2. **Resource Constraints**
   ```bash
   # Check resource usage
   kubectl top pods -n food-service
   kubectl describe pod <pod-name> -n food-service
   ```

3. **Configuration Issues**
   ```bash
   # Verify ConfigMap
   kubectl describe configmap food-service-config -n food-service
   
   # Check environment variables
   kubectl exec -it deployment/food-service -- env | grep -i food
   ```

### Emergency Procedures

1. **Immediate Rollback**
   ```bash
   # Quick rollback to last known good version
   kubectl rollout undo deployment/food-service -n food-service
   kubectl rollout status deployment/food-service -n food-service
   ```

2. **Service Restoration**
   ```bash
   # Restore from backup
   kubectl apply -f backup-$(date +%Y%m%d).yaml
   
   # Scale up if needed
   kubectl scale deployment food-service --replicas=3 -n food-service
   ```

---

**Last Updated:** [Current Date]
**Document Owner:** [Platform Team]
**Review Schedule:** Before each major release
