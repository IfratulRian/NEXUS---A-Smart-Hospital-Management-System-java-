package nexus.application.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ReportService {
    private final List<Report> reports = new ArrayList<>();

    public ReportService(List<Report> reports) {
        if (reports != null) this.reports.addAll(reports);
    }

    public List<Report> getAvailableReports() {
        return Collections.unmodifiableList(reports);
    }

    public String generateReport(Report report) {
        return report.generate();
    }

    public CompletableFuture<String> generateReportAsync(Report report) {
        return report.generateAsync();
    }
}
