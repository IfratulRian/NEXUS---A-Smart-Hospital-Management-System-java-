package nexus.application.report;

import nexus.application.billing.BillingService;
import nexus.domain.billing.Invoice;
import nexus.domain.billing.payment.Payment;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FinancialRevenueReport implements Report {
    private final BillingService service;

    public FinancialRevenueReport(BillingService service) {
        this.service = service;
    }

    @Override public String getTitle() { return "Financial Revenue & Accounts Receivable"; }

    @Override
    public String generate() {
        List<Invoice> invoices = service.listAll();
        double totalBilled = invoices.stream().mapToDouble(Invoice::getTotalAmount).sum();
        double totalCollected = service.getTotalRevenue();
        double outstanding = service.getTotalOutstanding();

        List<Payment> allPayments = invoices.stream()
                .flatMap(i -> i.getPayments().stream())
                .toList();

        Map<String, Double> byMethod = allPayments.stream()
                .collect(Collectors.groupingBy(Payment::getMethod, Collectors.summingDouble(Payment::getAmount)));

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Total Billed:      BDT %.2f\n", totalBilled));
        sb.append(String.format("Total Collected:   BDT %.2f (%.1f%% collection rate)\n", totalCollected,
                totalBilled == 0 ? 0.0 : (totalCollected / totalBilled * 100.0)));
        sb.append(String.format("Outstanding Dues:  BDT %.2f\n\n", outstanding));

        sb.append("--- Revenue by Payment Channel ---\n");
        byMethod.forEach((method, amount) ->
                sb.append(String.format("  * %-18s : BDT %.2f\n", method, amount)));
        return sb.toString();
    }
}
