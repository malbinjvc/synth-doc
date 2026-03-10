# SynthDoc Error Log

## Build Errors

### Error 1: JUnit Platform Launcher Missing
- **Phase**: Initial test run
- **Error**: `Could not start Gradle Test Executor. Failed to load JUnit Platform.`
- **Cause**: Gradle 9.4 with JDK 25 toolchain targeting Java 21 requires explicit JUnit Platform Launcher dependency
- **Fix**: Added `testRuntimeOnly 'org.junit.platform:junit-platform-launcher'` to build.gradle

### Error 2: Jackson Unrecognized Field "duplex"
- **Phase**: API integration tests (Javalin TestTools + OkHttp)
- **Error**: `Unrecognized field "duplex" (class com.synthdoc.models.Template)`
- **Cause**: OkHttp RequestBody includes internal metadata fields (like "duplex") that Jackson tried to deserialize
- **Fix 1**: Added `mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)` to ObjectMapper config
- **Fix 2**: Rewrote API integration tests to use `java.net.http.HttpClient` instead of OkHttp RequestBody for cleaner HTTP requests without internal metadata leakage

### Error 3: Template Name Required (400 from API)
- **Phase**: API integration tests after Fix 1
- **Error**: `Template name is required` - 400 response when creating templates
- **Cause**: Even after ignoring unknown properties, the OkHttp test client's request body format caused Jackson to create empty Template objects (null fields)
- **Fix**: Replaced Javalin TestTools OkHttp client with direct `java.net.http.HttpClient` calls, setting `Content-Type: application/json` explicitly

## Runtime Errors
None encountered during development.

---

## Security Audit Findings (10-Point)

| # | Area | Status | Notes |
|---|------|--------|-------|
| 1 | Hardcoded Credentials | PASS | Only test mock key "sk-test-key"; API key from env var |
| 2 | Sensitive File Exposure | PASS | .gitignore covers .env, credentials.json |
| 3 | SQL Injection | PASS | N/A - in-memory ConcurrentHashMap |
| 4 | XSS & Input Validation | FAIL | No input validation on titles/descriptions; error messages exposed |
| 5 | Authentication | FAIL | No auth on any endpoint |
| 6 | CORS & Headers | FAIL | No CORS, no security headers |
| 7 | File Upload Security | PASS | N/A - no upload endpoints |
| 8 | Docker Security | PASS | Multi-stage build, non-root user, version pinned |
| 9 | Infrastructure | PASS | CI/CD properly configured |
| 10 | Dependencies | PASS | Javalin 6.6.0, Jackson 2.18.0 from Maven Central |

**Critical**: No authentication - all endpoints publicly accessible
**Note**: Acceptable for portfolio demo; add auth before production use

---

## Final Status
- **Tests**: 117/117 passing
- **Build**: Successful
- **Docker**: Builds successfully
- **CI/CD**: Configured
