# Contributing to Food Service

Thank you for your interest in contributing to the Food Service application! This document provides guidelines and instructions for contributors.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Contributing Process](#contributing-process)
- [Code Standards](#code-standards)
- [Testing Guidelines](#testing-guidelines)
- [Documentation Standards](#documentation-standards)
- [Pull Request Process](#pull-request-process)
- [Issue Reporting](#issue-reporting)
- [Community](#community)

## 🤝 Code of Conduct

This project adheres to a Code of Conduct that we expect all contributors to follow:

- **Be respectful**: Treat everyone with respect and consideration
- **Be inclusive**: Welcome newcomers and help them succeed
- **Be collaborative**: Share knowledge and work together effectively
- **Be patient**: Remember that everyone has different skill levels and backgrounds
- **Be constructive**: Provide helpful feedback and suggestions

## 🚀 Getting Started

### Prerequisites

Before contributing, ensure you have the following installed:

- **Java 17** or higher
- **Maven 3.6+**
- **Git**
- **Docker Desktop** (for containerization testing)
- **IDE**: IntelliJ IDEA, VS Code, or Eclipse

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/food-service.git
   cd food-service
   ```
3. Add the upstream repository:
   ```bash
   git remote add upstream https://github.com/ORIGINAL_OWNER/food-service.git
   ```

## 🛠️ Development Setup

### 1. Verify Installation
```bash
# Check Java version
java -version

# Check Maven version
mvn -version

# Verify project builds successfully
mvn clean compile
```

### 2. Run Tests
```bash
# Run all tests
mvn test

# Run with coverage report
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### 3. Start Application
```bash
# Standard HTTP mode
mvn spring-boot:run

# SSL/HTTPS mode
mvn spring-boot:run -Dspring-boot.run.profiles=ssl

# Test endpoints
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/foods
```

## 🔄 Contributing Process

### 1. Create a Branch
```bash
# Update your main branch
git checkout main
git pull upstream main

# Create a feature branch
git checkout -b feature/your-feature-name
```

### 2. Make Changes
- Write clean, maintainable code
- Follow existing code patterns and conventions
- Add/update tests for your changes
- Update documentation as needed

### 3. Commit Changes
```bash
# Stage your changes
git add .

# Commit with a descriptive message
git commit -m "feat: add new food search functionality

- Implement case-insensitive search
- Add input validation
- Include comprehensive tests
- Update API documentation"
```

### 4. Push and Create PR
```bash
# Push to your fork
git push origin feature/your-feature-name

# Create a Pull Request on GitHub
```

## 📝 Code Standards

### Java Code Style

#### Naming Conventions
- **Classes**: PascalCase (`FoodService`, `FoodController`)
- **Methods**: camelCase (`getAllFoodItems`, `searchByName`)
- **Variables**: camelCase (`foodList`, `searchTerm`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_SEARCH_LENGTH`)

#### Code Formatting
```java
// Good: Clear, readable method with proper spacing
@GetMapping("/{id}")
public ResponseEntity<Food> getFoodItemById(@PathVariable Integer id) {
    Food food = foodService.getFoodItemById(id);
    return ResponseEntity.ok(food);
}

// Good: Proper exception handling
@Override
public Food getFoodItemById(Integer id) {
    return foodMenu.getFoodList().stream()
            .filter(food -> food.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new FoodNotFoundException("Food item not found with id: " + id));
}
```

#### Lombok Usage
```java
// Preferred: Use Lombok annotations
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Food {
    private Integer id;
    private String name;
    private String price;
    private String description;
    private Integer calories;
}

// Avoid: Manual getters/setters when Lombok can be used
```

### Package Structure
```
com.example.foodservice/
├── FoodServiceApplication.java     # Main application
├── config/                         # Configuration classes
├── controller/                     # REST controllers
├── exception/                      # Exception handling
├── model/                          # Data models
└── service/                        # Business logic
```

### Documentation Standards
- Use JavaDoc for public methods and classes
- Include parameter descriptions and return value explanations
- Provide usage examples for complex methods

```java
/**
 * Searches for food items by name using case-insensitive partial matching.
 *
 * @param name the search term to match against food names (case-insensitive)
 * @return a list of food items that contain the search term in their name
 * @throws IllegalArgumentException if name is null or empty
 * 
 * @example
 * <pre>
 * List&lt;Food&gt; results = foodService.searchFoodItemsByName("paneer");
 * // Returns all food items containing "paneer" in the name
 * </pre>
 */
@Override
public List<Food> searchFoodItemsByName(String name) {
    // Implementation
}
```

## 🧪 Testing Guidelines

### Test Structure
```
src/test/java/com/example/foodservice/
├── controller/          # Controller integration tests
├── service/            # Service unit tests
├── model/              # Model unit tests
└── exception/          # Exception handling tests
```

### Test Writing Standards

#### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class FoodServiceImplTest {
    
    @Mock
    private ApplicationConfig applicationConfig;
    
    @InjectMocks
    private FoodServiceImpl foodService;
    
    @Test
    @DisplayName("Should return food item when valid ID is provided")
    void getFoodItemById_withValidId_shouldReturnFoodItem() {
        // Given
        Integer foodId = 1;
        Food expectedFood = createSampleFood(foodId);
        
        // When
        Food actualFood = foodService.getFoodItemById(foodId);
        
        // Then
        assertThat(actualFood).isNotNull();
        assertThat(actualFood.getId()).isEqualTo(foodId);
        assertThat(actualFood.getName()).isEqualTo("Palak paneer");
    }
}
```

#### Integration Tests
```java
@WebMvcTest(FoodController.class)
class FoodControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private FoodService foodService;
    
    @Test
    void getAllFoodItems_shouldReturnJsonArray() throws Exception {
        // Given
        List<Food> foods = Arrays.asList(createSampleFood(1), createSampleFood(2));
        when(foodService.getAllFoodItems()).thenReturn(foods);
        
        // When & Then
        mockMvc.perform(get("/api/foods"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }
}
```

### Test Coverage Requirements
- **Minimum Coverage**: 80% line coverage
- **Critical Paths**: 100% coverage for business logic
- **Edge Cases**: Test null values, empty collections, boundary conditions
- **Error Scenarios**: Test exception handling and error responses

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=FoodServiceImplTest

# Run tests with coverage
mvn clean test jacoco:report

# Run tests matching pattern
mvn test -Dtest="*Controller*"
```

## 📚 Documentation Standards

### README Updates
When adding new features, update the README.md with:
- New API endpoints
- Configuration options
- Example usage
- Troubleshooting information

### API Documentation
- Update OpenAPI specification (`docs/api-specification.yaml`)
- Include request/response examples
- Document error codes and messages
- Add parameter validation rules

### Code Comments
```java
// Good: Explains the why, not the what
// Sort by calories to prioritize healthier options first
foods.sort(Comparator.comparing(Food::getCalories));

// Avoid: States the obvious
// Sort the foods list
foods.sort(Comparator.comparing(Food::getCalories));
```

## 🔍 Pull Request Process

### Before Submitting
- [ ] Code compiles without warnings
- [ ] All tests pass
- [ ] Code coverage meets requirements (80%+)
- [ ] Documentation is updated
- [ ] Docker image builds successfully
- [ ] Changes tested locally

### PR Description Template
```markdown
## Description
Brief description of changes made.

## Type of Change
- [ ] Bug fix (non-breaking change that fixes an issue)
- [ ] New feature (non-breaking change that adds functionality)
- [ ] Breaking change (fix or feature that breaks existing functionality)
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing completed
- [ ] Docker build tested

## Screenshots (if applicable)
Include screenshots for UI changes.

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] Tests added/updated
- [ ] No new warnings introduced
```

### Review Process
1. **Automated Checks**: CI/CD pipeline runs tests and builds
2. **Code Review**: At least one maintainer reviews the code
3. **Testing**: Reviewer tests the changes locally
4. **Approval**: Changes approved and merged

## 🐛 Issue Reporting

### Bug Reports
Use the GitHub issue template for bug reports:

```markdown
## Bug Description
Clear description of the bug.

## Steps to Reproduce
1. Start application with `mvn spring-boot:run`
2. Call endpoint `GET /api/foods/999`
3. Observe error response

## Expected Behavior
Should return 404 with proper error message.

## Actual Behavior
Returns 500 internal server error.

## Environment
- Java Version: 17
- Maven Version: 3.9.0
- OS: Windows 11
- Profile: default

## Additional Context
Include any relevant logs or screenshots.
```

### Feature Requests
```markdown
## Feature Description
Description of the proposed feature.

## Use Case
Why is this feature needed?

## Proposed Solution
How should this feature work?

## Alternatives Considered
Any alternative solutions considered?

## Implementation Notes
Technical considerations or constraints.
```

## 📋 Development Guidelines

### Branch Naming
- **Features**: `feature/add-pagination`
- **Bug Fixes**: `bugfix/fix-search-null-pointer`
- **Hotfixes**: `hotfix/security-vulnerability`
- **Documentation**: `docs/update-api-docs`

### Commit Messages
Follow the Conventional Commits specification:

```bash
# Format
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]

# Examples
feat(api): add pagination support for food items

- Add page and size query parameters
- Implement PageRequest in service layer
- Update API documentation
- Add integration tests

fix(security): validate input parameters

Closes #123

docs(readme): update deployment instructions

Update Azure AKS deployment steps with latest CLI commands.
```

### Commit Types
- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, etc.)
- **refactor**: Code refactoring
- **test**: Adding or updating tests
- **chore**: Maintenance tasks

## 🔧 Tools and IDE Setup

### IntelliJ IDEA
1. Install Lombok plugin
2. Enable annotation processing
3. Configure code style:
   - Indentation: 4 spaces
   - Line length: 120 characters
   - Import organization: java.*, javax.*, *, org.springframework.*

### VS Code
1. Install Java Extension Pack
2. Install Spring Boot Extension Pack
3. Configure settings.json:
   ```json
   {
     "java.format.settings.url": "https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml",
     "java.format.settings.profile": "GoogleStyle"
   }
   ```

### Eclipse
1. Import as Maven project
2. Install Lombok (download lombok.jar and run `java -jar lombok.jar`)
3. Configure formatter with Google Java Style

## 🌐 Community

### Getting Help
- **GitHub Discussions**: For questions and general discussions
- **GitHub Issues**: For bug reports and feature requests
- **Stack Overflow**: Tag questions with `food-service-api`

### Communication Channels
- **GitHub**: Primary communication platform
- **Documentation**: Keep README.md updated
- **Code Reviews**: Constructive feedback in PRs

### Recognition
Contributors are recognized in the following ways:
- Listed in CONTRIBUTORS.md
- Mentioned in release notes
- GitHub contributor statistics

## 📝 Release Process

### Version Numbering
Follow Semantic Versioning (SemVer):
- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Release Checklist
- [ ] Version number updated
- [ ] CHANGELOG.md updated
- [ ] All tests passing
- [ ] Documentation updated
- [ ] Docker image built and tested
- [ ] Git tag created
- [ ] Release notes prepared

## 🙏 Thank You

Thank you for contributing to the Food Service project! Your contributions help make this application better for everyone.

For questions about contributing, please create a GitHub Discussion or reach out to the maintainers.

---

Happy coding! 🚀
