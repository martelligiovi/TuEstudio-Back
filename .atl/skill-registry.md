# Skill Registry — TuEstudio-Back

**Project**: TuEstudio-Back
**Stack**: Java 21 · Spring Boot 3.3.5 · PostgreSQL · Hexagonal Architecture · Maven
**Generated**: 2026-05-11

## User Skills

| Skill | Trigger |
|-------|---------|
| tdd | Writing features/fixes with TDD, red-green-refactor loop |
| hexagonal-architecture-layers-java | Structuring Java by Domain/Application/Infrastructure |
| spring-boot-3 | Building or refactoring Spring Boot 3 applications |
| java-21 | Writing Java 21 code with records, sealed types, virtual threads |
| branch-pr | Creating pull requests |
| issue-creation | Creating GitHub issues |
| chained-pr | PRs > 400 changed lines |
| work-unit-commits | Committing work units |
| simplify | Code review, refactoring, post-implementation cleanup |
| comment-writer | PR/issue comments, review feedback, async messages |
| cognitive-doc-design | Writing guides, READMEs, architecture docs |
| judgment-day | Adversarial review of code ("judgment day", "doble review") |
| sdd-explore | `/sdd-explore`, codebase investigation, feature ideation |
| sdd-apply | `/sdd-apply`, implementing tasks — Strict TDD active |
| sdd-verify | `/sdd-verify`, validating implementation vs specs |

## Compact Rules

### simplify
- Remove duplication before adding abstraction
- Prefer existing Spring/Java utilities over custom implementations
- Flag dead code, unused imports, and over-engineered patterns
- Verify no logic was dropped during simplification

### judgment-day
- Launches two independent blind judge sub-agents simultaneously
- Each reviews the target independently; findings are synthesized
- Applies fixes and re-judges until both pass or escalates after 2 iterations

### tdd
- Tests verify behavior through public interfaces only, never implementation details
- Vertical slices: one test → one impl → repeat (never write all tests first)
- Never refactor while RED — get to GREEN first
- Test names describe behavior: "user_can_checkout_with_valid_cart"
- Only mock at ports (infrastructure boundaries), never internal collaborators

### hexagonal-architecture-layers-java
- Domain: pure Java, no framework annotations, no I/O, no Spring imports
- Application: use cases as interfaces + services; defines ports (in/out)
- Infrastructure: adapters implementing ports; holds @Repository/@Service/@Controller
- Dependency direction: Infrastructure → Application → Domain (never reversed)
- One hexagon per bounded context; no shared domain models across contexts
- Communicate across hexagons via ports, DTOs, or anti-corruption layers

### spring-boot-3
- Constructor injection only — never field @Autowired
- @ConfigurationProperties + @Validated for config, never scattered @Value
- @Transactional on application services, not controllers

### java-21
- Records for immutable DTOs and value objects; validate in compact constructor
- Sealed types + switch pattern matching for closed hierarchies
- Virtual threads (already enabled via spring.threads.virtual.enabled: true)

## Project Conventions

- Package: `com.tuestudio.{context}.{domain|application.{usecase|port}|infrastructure.{persistence|security|web}}`
- Bounded contexts: auth · tutor · catalog · shared
- Current feature branch: `feature/tutor-contact-request-details`
- Google OAuth: `GoogleOAuthService`, `SocialAuthController` (recently added)
- Contact request flow: `ContactRequest` domain, `ContactRequestStatus` enum, `AttendRequestUseCase`, `GetTeacherRequestsUseCase`
- Unit tests: `@ExtendWith(MockitoExtension.class)` + AssertJ assertions
- Integration tests: `@SpringBootTest @AutoConfigureMockMvc @Testcontainers @ActiveProfiles("test")`
- Testcontainers: `PostgreSQLContainer<?>` with `@ServiceConnection`
- Test command: `mvn test`
- Strict TDD Mode: **enabled**
