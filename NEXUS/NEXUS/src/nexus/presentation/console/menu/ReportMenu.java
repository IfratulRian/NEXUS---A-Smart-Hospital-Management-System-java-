package nexus.presentation.console.menu;

import nexus.application.report.Report;
import nexus.application.report.ReportService;
import nexus.presentation.console.input.ConsoleInput;
import nexus.presentation.console.output.ConsoleOutput;
import nexus.security.authentication.SessionContext;
import nexus.security.authorization.AuthorizationService;
import nexus.security.role.Permission;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ReportMenu extends Menu {
    private final ReportService service;

    public ReportMenu(ReportService service, ConsoleInput in, ConsoleOutput out,
                      SessionContext session, AuthorizationService authorization) {
        super("ANALYTICAL REPORTS & INTELLIGENCE", in, out, session, authorization);
        this.service = service;
    }

    @Override
    protected void registerItems() {
        List<Report> reports = service.getAvailableReports();
        for (Report r : reports) {
            add(r.getTitle(), Permission.REPORT_VIEW, () -> runReport(r));
        }
        add("Generate All Reports in Parallel (Async CompletableFuture)", Permission.REPORT_VIEW, this::generateAllAsync);
    }

    private void runReport(Report report) {
        out.header("REPORT: " + report.getTitle().toUpperCase());
        String content = service.generateReport(report);
        out.println(content);
        out.footer();
    }

    private void generateAllAsync() {
        out.info("Launching parallel background report generation via CompletableFuture...");
        List<CompletableFuture<String>> futures = service.getAvailableReports().stream()
                .map(r -> r.generateAsync().thenApply(res -> "=== " + r.getTitle() + " ===\n" + res))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenAccept(v -> {
                    out.header("ASYNC BATCH REPORTS READY");
                    for (CompletableFuture<String> f : futures) {
                        out.println(f.join());
                        out.println("----------------------------------------\n");
                    }
                    out.footer();
                }).join();
    }
}
