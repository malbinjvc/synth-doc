#!/usr/bin/env python3
"""Generate SynthDoc Project Report PDF."""

from fpdf import FPDF
from datetime import datetime

class Report(FPDF):
    BLUE = (30, 64, 175)
    BLACK = (0, 0, 0)
    GRAY = (100, 100, 100)
    LIGHT_GRAY = (240, 240, 240)

    def header(self):
        self.set_font("Helvetica", "B", 10)
        self.set_text_color(*self.GRAY)
        self.cell(0, 8, "SynthDoc - Project Report", ln=True, align="R")
        self.set_draw_color(*self.BLUE)
        self.set_line_width(0.5)
        self.line(10, self.get_y(), 200, self.get_y())
        self.ln(4)

    def footer(self):
        self.set_y(-15)
        self.set_font("Helvetica", "I", 8)
        self.set_text_color(*self.GRAY)
        self.cell(0, 10, f"Page {self.page_no()}/{{nb}}", align="C")

    def section_title(self, title):
        self.set_font("Helvetica", "B", 14)
        self.set_text_color(*self.BLUE)
        self.cell(0, 10, title, ln=True)
        self.set_draw_color(*self.BLUE)
        self.set_line_width(0.3)
        self.line(10, self.get_y(), 200, self.get_y())
        self.ln(3)

    def sub_title(self, title):
        self.set_font("Helvetica", "B", 11)
        self.set_text_color(*self.BLUE)
        self.cell(0, 8, title, ln=True)
        self.ln(1)

    def body_text(self, text):
        self.set_font("Helvetica", "", 10)
        self.set_text_color(*self.BLACK)
        self.multi_cell(0, 5, text)
        self.ln(2)

    def bullet(self, text):
        self.set_font("Helvetica", "", 10)
        self.set_text_color(*self.BLACK)
        self.cell(8)
        self.multi_cell(0, 5, f"- {text}")
        self.ln(1)

    def key_value(self, key, value):
        self.set_font("Helvetica", "B", 10)
        self.set_text_color(*self.BLACK)
        self.cell(50, 6, f"{key}:")
        self.set_font("Helvetica", "", 10)
        self.cell(0, 6, str(value), ln=True)


def main():
    pdf = Report()
    pdf.alias_nb_pages()
    pdf.set_auto_page_break(auto=True, margin=20)

    # Title Page
    pdf.add_page()
    pdf.ln(40)
    pdf.set_font("Helvetica", "B", 28)
    pdf.set_text_color(*Report.BLUE)
    pdf.cell(0, 15, "SynthDoc", ln=True, align="C")
    pdf.set_font("Helvetica", "", 16)
    pdf.set_text_color(*Report.GRAY)
    pdf.cell(0, 10, "AI Document Generator", ln=True, align="C")
    pdf.ln(8)
    pdf.set_font("Helvetica", "", 12)
    pdf.set_text_color(*Report.BLACK)
    pdf.cell(0, 8, "Java / Javalin 6.6.0 / Gradle (Groovy DSL)", ln=True, align="C")
    pdf.cell(0, 8, f"Report Date: {datetime.now().strftime('%Y-%m-%d')}", ln=True, align="C")
    pdf.ln(20)
    pdf.set_draw_color(*Report.BLUE)
    pdf.set_line_width(1)
    pdf.line(60, pdf.get_y(), 150, pdf.get_y())

    # Project Overview
    pdf.add_page()
    pdf.section_title("1. Project Overview")
    pdf.body_text(
        "SynthDoc is an AI Document Generator built with Java and the Javalin 6.6.0 web "
        "framework. It provides document templates with AI-powered content completion, "
        "citation tracking, document versioning, and export capabilities. The system uses "
        "a mocked Claude API client to enable fully deterministic testing without real API keys."
    )
    pdf.key_value("Language", "Java (JDK 21 toolchain, built on JDK 25)")
    pdf.key_value("Framework", "Javalin 6.6.0")
    pdf.key_value("Build Tool", "Gradle 9.4.0 (Groovy DSL)")
    pdf.key_value("Testing", "JUnit 5.11.0")
    pdf.key_value("Storage", "ConcurrentHashMap (in-memory)")
    pdf.key_value("Serialization", "Jackson 2.18.0")

    # Architecture
    pdf.add_page()
    pdf.section_title("2. Architecture")
    pdf.sub_title("Models (6 classes)")
    pdf.bullet("Template - Document template with type, sections, and variables")
    pdf.bullet("Section - Document section with type (introduction/body/conclusion/references/custom)")
    pdf.bullet("Document - Document instance with sections, metadata, and version tracking")
    pdf.bullet("DocumentVersion - Immutable snapshot of a document at a point in time")
    pdf.bullet("Citation - Citation with format support (APA/MLA/Chicago)")
    pdf.bullet("GenerationRequest - AI generation request with tone and context parameters")

    pdf.sub_title("Services (7 classes)")
    pdf.bullet("TemplateService - CRUD operations for document templates")
    pdf.bullet("DocumentService - Document lifecycle management with template instantiation")
    pdf.bullet("GenerationService - AI content generation using Claude client abstraction")
    pdf.bullet("CitationService - Citation management and bibliography generation")
    pdf.bullet("VersionService - Version history, rollback, and diff capabilities")
    pdf.bullet("ExportService - Export documents as JSON, text, or markdown")
    pdf.bullet("StatsService - Platform-wide statistics and metrics")

    pdf.sub_title("Clients (3 classes)")
    pdf.bullet("ClaudeClient - Interface defining the AI generation contract")
    pdf.bullet("HttpClaudeClient - Real implementation using java.net.http.HttpClient")
    pdf.bullet("MockClaudeClient - Deterministic mock for testing with custom response registration")

    # API Endpoints
    pdf.add_page()
    pdf.section_title("3. API Endpoints")
    endpoints = [
        ("GET", "/health", "Health check"),
        ("POST", "/api/templates", "Create template"),
        ("GET", "/api/templates", "List all templates"),
        ("GET", "/api/templates/{id}", "Get template by ID"),
        ("PUT", "/api/templates/{id}", "Update template"),
        ("DELETE", "/api/templates/{id}", "Delete template"),
        ("POST", "/api/documents", "Create document from template"),
        ("GET", "/api/documents", "List all documents"),
        ("GET", "/api/documents/{id}", "Get document by ID"),
        ("POST", "/api/documents/{id}/generate", "Generate all sections"),
        ("POST", "/api/documents/{id}/sections/{sId}/generate", "Generate single section"),
        ("GET", "/api/documents/{id}/versions", "Get version history"),
        ("POST", "/api/documents/{id}/rollback/{ver}", "Rollback to version"),
        ("GET", "/api/documents/{id}/export?format=X", "Export document"),
        ("POST", "/api/citations", "Create citation"),
        ("GET", "/api/documents/{id}/citations", "Get document citations"),
        ("GET", "/api/documents/{id}/bibliography", "Generate bibliography"),
        ("GET", "/api/stats", "Platform statistics"),
    ]
    pdf.set_font("Helvetica", "B", 9)
    pdf.set_fill_color(*Report.BLUE)
    pdf.set_text_color(255, 255, 255)
    pdf.cell(20, 7, "Method", border=1, fill=True, align="C")
    pdf.cell(95, 7, "Endpoint", border=1, fill=True, align="C")
    pdf.cell(75, 7, "Description", border=1, fill=True, align="C")
    pdf.ln()

    pdf.set_font("Helvetica", "", 8)
    pdf.set_text_color(*Report.BLACK)
    for i, (method, path, desc) in enumerate(endpoints):
        fill = i % 2 == 0
        if fill:
            pdf.set_fill_color(*Report.LIGHT_GRAY)
        pdf.cell(20, 6, method, border=1, fill=fill, align="C")
        pdf.cell(95, 6, path, border=1, fill=fill)
        pdf.cell(75, 6, desc, border=1, fill=fill)
        pdf.ln()

    # Test Results
    pdf.add_page()
    pdf.section_title("4. Test Results")
    pdf.body_text("All 117 tests pass successfully across 8 test suites:")
    tests = [
        ("TemplateServiceTest", 16),
        ("DocumentServiceTest", 18),
        ("GenerationServiceTest", 10),
        ("CitationServiceTest", 15),
        ("VersionServiceTest", 14),
        ("ExportServiceTest", 16),
        ("ClaudeClientTest", 12),
        ("ApiIntegrationTest", 16),
    ]
    pdf.set_font("Helvetica", "B", 9)
    pdf.set_fill_color(*Report.BLUE)
    pdf.set_text_color(255, 255, 255)
    pdf.cell(95, 7, "Test Suite", border=1, fill=True, align="C")
    pdf.cell(45, 7, "Tests", border=1, fill=True, align="C")
    pdf.cell(50, 7, "Status", border=1, fill=True, align="C")
    pdf.ln()

    pdf.set_font("Helvetica", "", 9)
    pdf.set_text_color(*Report.BLACK)
    total = 0
    for i, (suite, count) in enumerate(tests):
        fill = i % 2 == 0
        if fill:
            pdf.set_fill_color(*Report.LIGHT_GRAY)
        pdf.cell(95, 6, suite, border=1, fill=fill)
        pdf.cell(45, 6, str(count), border=1, fill=fill, align="C")
        pdf.set_text_color(0, 128, 0)
        pdf.cell(50, 6, "PASSED", border=1, fill=fill, align="C")
        pdf.set_text_color(*Report.BLACK)
        pdf.ln()
        total += count

    pdf.set_font("Helvetica", "B", 9)
    pdf.cell(95, 7, "TOTAL", border=1, fill=True)
    pdf.cell(45, 7, str(total), border=1, fill=True, align="C")
    pdf.set_text_color(0, 128, 0)
    pdf.cell(50, 7, "ALL PASSED", border=1, fill=True, align="C")
    pdf.ln()
    pdf.set_text_color(*Report.BLACK)

    # Security Audit
    pdf.add_page()
    pdf.section_title("5. Security Audit (10-Point)")

    audit_items = [
        ("1. Secrets and API Keys",
         "PASS - No hardcoded API keys or secrets. CLAUDE_API_KEY read from environment "
         "variable only. MockClaudeClient used for all tests."),
        ("2. Input Validation",
         "PASS - All service methods validate required fields (name, type, title, source). "
         "IllegalArgumentException thrown for invalid inputs."),
        ("3. SQL Injection / NoSQL Injection",
         "N/A - No database used. ConcurrentHashMap storage eliminates injection vectors."),
        ("4. Cross-Site Scripting (XSS)",
         "LOW RISK - JSON-only API. No HTML rendering. Jackson handles output encoding. "
         "Client-side rendering responsibility."),
        ("5. Authentication and Authorization",
         "NOTE - No authentication implemented. Suitable for internal/demo use. "
         "Production deployment should add API key or JWT authentication."),
        ("6. Dependency Security",
         "PASS - Using latest stable versions: Javalin 6.6.0, Jackson 2.18.0, JUnit 5.11.0. "
         "No known CVEs in dependencies."),
        ("7. Error Handling and Information Leakage",
         "PASS - Custom exception handlers return sanitized error messages. "
         "Stack traces not exposed to clients."),
        ("8. Concurrency Safety",
         "PASS - ConcurrentHashMap for all stores. Thread-safe operations. "
         "No race conditions in version management."),
        ("9. Docker Security",
         "PASS - Multi-stage build minimizes image size. Non-root user (appuser). "
         "Alpine-based JRE image reduces attack surface."),
        ("10. CI/CD Pipeline Security",
         "PASS - GitHub Actions with pinned action versions (v4). No secrets in workflow. "
         "Docker build verification in CI pipeline."),
    ]

    for title, desc in audit_items:
        pdf.sub_title(title)
        pdf.body_text(desc)

    # Build Errors Summary
    pdf.add_page()
    pdf.section_title("6. Build Errors and Resolutions")

    pdf.sub_title("Error 1: JUnit Platform Launcher Missing")
    pdf.body_text(
        "Gradle 9.4 with JDK 25 requires explicit JUnit Platform Launcher dependency. "
        "Resolved by adding testRuntimeOnly 'org.junit.platform:junit-platform-launcher' "
        "to build.gradle dependencies."
    )

    pdf.sub_title("Error 2: Jackson Unknown Field 'duplex'")
    pdf.body_text(
        "OkHttp RequestBody includes internal metadata fields that Jackson tried to "
        "deserialize into Template objects. Resolved by configuring ObjectMapper with "
        "DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES disabled."
    )

    pdf.sub_title("Error 3: API Test Client Compatibility")
    pdf.body_text(
        "Javalin TestTools OkHttp client caused request body serialization issues. "
        "Resolved by rewriting API integration tests to use java.net.http.HttpClient "
        "with explicit Content-Type headers for clean JSON transport."
    )

    # Tech Stack Summary
    pdf.add_page()
    pdf.section_title("7. Technology Stack")
    stack = [
        ("Runtime", "Java 21 (toolchain) on JDK 25"),
        ("Web Framework", "Javalin 6.6.0"),
        ("Build Tool", "Gradle 9.4.0 (Groovy DSL)"),
        ("JSON", "Jackson 2.18.0 + JSR310 module"),
        ("Testing", "JUnit 5.11.0"),
        ("Logging", "SLF4J Simple 2.0.16"),
        ("AI Client", "Claude API (mocked for tests)"),
        ("Storage", "ConcurrentHashMap (in-memory)"),
        ("Container", "Docker (eclipse-temurin:21-jre-alpine)"),
        ("CI/CD", "GitHub Actions"),
    ]

    pdf.set_font("Helvetica", "B", 9)
    pdf.set_fill_color(*Report.BLUE)
    pdf.set_text_color(255, 255, 255)
    pdf.cell(60, 7, "Component", border=1, fill=True, align="C")
    pdf.cell(130, 7, "Technology", border=1, fill=True, align="C")
    pdf.ln()

    pdf.set_font("Helvetica", "", 9)
    pdf.set_text_color(*Report.BLACK)
    for i, (comp, tech) in enumerate(stack):
        fill = i % 2 == 0
        if fill:
            pdf.set_fill_color(*Report.LIGHT_GRAY)
        pdf.cell(60, 6, comp, border=1, fill=fill)
        pdf.cell(130, 6, tech, border=1, fill=fill)
        pdf.ln()

    # Output
    output_path = "/Users/malbinjose/Desktop/github_repo_projects/synth-doc/SynthDoc_Project_Report.pdf"
    pdf.output(output_path)
    print(f"Report generated: {output_path}")


if __name__ == "__main__":
    main()
