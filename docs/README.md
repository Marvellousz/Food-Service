# Documentation Index

Welcome to the Food Service application documentation. This directory contains comprehensive documentation for developers, architects, operations teams, and contributors.

## 📖 Documentation Overview

### API Documentation
- **[API Specification](api-specification.yaml)** - Complete OpenAPI 3.0.3 specification with all endpoints, schemas, and examples

### Architecture & Design
- **[Architecture Guide](ARCHITECTURE.md)** - System architecture, design patterns, and component interactions
- **[Security Documentation](SECURITY.md)** - Security architecture, SSL/TLS, container security, and compliance

### Development
- **[Contributing Guidelines](../CONTRIBUTING.md)** - Development setup, coding standards, and contribution process
- **[Development Setup](../scripts/DEVELOPMENT-SETUP.md)** - Automated development environment setup scripts

### Operations & Deployment
- **[Operations Runbook](OPERATIONS.md)** - Incident response, troubleshooting, and maintenance procedures
- **[CI/CD Pipeline](CI-CD.md)** - Continuous integration and deployment setup for multiple platforms
- **[Migration Guide](MIGRATION.md)** - Version upgrades, environment migrations, and rollback procedures
- **[Performance Guide](PERFORMANCE.md)** - Performance optimization, monitoring, and tuning

## 🎯 Documentation by Audience

### For New Developers
Start here to get up and running quickly:
1. [Development Setup](../scripts/DEVELOPMENT-SETUP.md) - Automated environment setup
2. [Contributing Guidelines](../CONTRIBUTING.md) - Development workflow and standards
3. [Architecture Guide](ARCHITECTURE.md) - Understanding the system design
4. [API Specification](api-specification.yaml) - API endpoints and usage

### For DevOps Engineers
Focus on deployment and operations:
1. [CI/CD Pipeline](CI-CD.md) - Build and deployment automation
2. [Operations Runbook](OPERATIONS.md) - Day-to-day operations and troubleshooting
3. [Security Documentation](SECURITY.md) - Security configurations and best practices
4. [Migration Guide](MIGRATION.md) - Deployment strategies and upgrades

### For Architects
Understand the system design and patterns:
1. [Architecture Guide](ARCHITECTURE.md) - System architecture and design decisions
2. [Security Documentation](SECURITY.md) - Security architecture and threat model
3. [Performance Guide](PERFORMANCE.md) - Performance characteristics and optimization
4. [API Specification](api-specification.yaml) - API design and contracts

### For Operations Teams
Day-to-day operations and incident response:
1. [Operations Runbook](OPERATIONS.md) - Incident response and troubleshooting procedures
2. [Performance Guide](PERFORMANCE.md) - Performance monitoring and optimization
3. [Migration Guide](MIGRATION.md) - Rollback procedures and environment management
4. [Security Documentation](SECURITY.md) - Security monitoring and incident response

## 📋 Quick Reference

### Common Tasks

| Task | Documentation | Commands |
|------|---------------|----------|
| Set up development environment | [Development Setup](../scripts/DEVELOPMENT-SETUP.md) | `./scripts/setup-dev.sh` |
| Build and test | [Contributing Guidelines](../CONTRIBUTING.md) | `mvn clean test` |
| Deploy to Kubernetes | [Operations Runbook](OPERATIONS.md) | `kubectl apply -f k8s-ssl-deployment.yaml` |
| Check application health | [Operations Runbook](OPERATIONS.md) | `curl -k https://localhost:8443/actuator/health` |
| View API documentation | [API Specification](api-specification.yaml) | Open in Swagger UI |
| Troubleshoot issues | [Operations Runbook](OPERATIONS.md) | Check logs and metrics |
| Upgrade application | [Migration Guide](MIGRATION.md) | Follow upgrade procedures |
| Performance tuning | [Performance Guide](PERFORMANCE.md) | Apply optimization settings |

### Key Endpoints

| Endpoint | Purpose | Documentation |
|----------|--------|---------------|
| `/api/foods` | Food management API | [API Specification](api-specification.yaml) |
| `/actuator/health` | Health checks | [Operations Runbook](OPERATIONS.md) |
| `/actuator/metrics` | Application metrics | [Performance Guide](PERFORMANCE.md) |
| `/actuator/info` | Application information | [Operations Runbook](OPERATIONS.md) |

### Configuration Files

| File | Purpose | Documentation |
|------|---------|---------------|
| `application.yml` | Main configuration | [Architecture Guide](ARCHITECTURE.md) |
| `application-ssl.yml` | SSL configuration | [Security Documentation](SECURITY.md) |
| `k8s-ssl-deployment.yaml` | Kubernetes deployment | [Operations Runbook](OPERATIONS.md) |
| `Dockerfile` | Container image | [CI/CD Pipeline](CI-CD.md) |

## 🔧 Documentation Maintenance

### Updating Documentation

When making changes to the application:

1. **Code Changes**: Update relevant sections in architecture and API documentation
2. **Configuration Changes**: Update security, operations, and migration documentation
3. **New Features**: Update API specification and contributing guidelines
4. **Deployment Changes**: Update operations runbook and CI/CD documentation
5. **Performance Changes**: Update performance guide and optimization recommendations

### Documentation Standards

- Use clear, concise language
- Include practical examples and code snippets
- Maintain consistent formatting and structure
- Keep documentation up-to-date with code changes
- Include diagrams and visuals where helpful
- Test all commands and procedures
- Review documentation with each release

### Contributing to Documentation

See [Contributing Guidelines](../CONTRIBUTING.md) for information on:
- Documentation style guide
- Review process for documentation changes
- How to suggest improvements
- Community standards for documentation

## 📞 Support and Contact

### Getting Help

1. **Development Issues**: Check [Development Setup](../scripts/DEVELOPMENT-SETUP.md) and [Contributing Guidelines](../CONTRIBUTING.md)
2. **Deployment Issues**: Refer to [Operations Runbook](OPERATIONS.md) troubleshooting section
3. **Security Concerns**: Follow procedures in [Security Documentation](SECURITY.md)
4. **Performance Issues**: Consult [Performance Guide](PERFORMANCE.md)

### Community

- **Issues**: Report bugs and request features via GitHub Issues
- **Discussions**: Join community discussions in GitHub Discussions
- **Security**: Report security issues following the security policy
- **Contributions**: Submit pull requests following the contributing guidelines

---

**Last Updated:** [Current Date]  
**Documentation Version:** 1.0  
**Maintained by:** [Development Team]

For questions about this documentation, please create an issue or reach out to the development team.
