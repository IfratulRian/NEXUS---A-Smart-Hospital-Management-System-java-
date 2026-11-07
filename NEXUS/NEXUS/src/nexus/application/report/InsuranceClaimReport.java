package nexus.application.report;

import nexus.application.insurance.InsuranceService;
import nexus.domain.insurance.Claim;
import nexus.domain.insurance.ClaimStatus;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InsuranceClaimReport implements Report {
    private final InsuranceService service;

    public InsuranceClaimReport(InsuranceService service) {
        this.service = service;
    }

    @Override public String getTitle() { return "Insurance Claims & Adjudication Summary"; }

    @Override
    public String generate() {
        List<Claim> all = service.listClaims();
        int total = all.size();
        double totalClaimed = all.stream().mapToDouble(Claim::getClaimAmount).sum();
        double totalApproved = all.stream().mapToDouble(Claim::getApprovedAmount).sum();

        Map<ClaimStatus, Long> byStatus = all.stream()
                .collect(Collectors.groupingBy(Claim::getStatus, Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        sb.append("Total Claims Filed:     ").append(total).append("\n");
        sb.append(String.format("Total Claim Amount:     BDT %.2f\n", totalClaimed));
        sb.append(String.format("Total Approved Amount:  BDT %.2f (%.1f%% approval rate)\n\n", totalApproved,
                totalClaimed == 0 ? 0.0 : (totalApproved / totalClaimed * 100.0)));

        sb.append("--- Claims by Adjudication Status ---\n");
        byStatus.forEach((status, count) ->
                sb.append(String.format("  * %-15s : %d\n", status, count)));
        return sb.toString();
    }
}
