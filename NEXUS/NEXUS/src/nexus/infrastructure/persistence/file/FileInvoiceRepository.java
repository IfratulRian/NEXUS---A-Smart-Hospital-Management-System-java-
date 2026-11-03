package nexus.infrastructure.persistence.file;

import nexus.domain.billing.Billable;
import nexus.domain.billing.Invoice;
import nexus.domain.billing.InvoiceStatus;
import nexus.domain.billing.payment.CashPayment;
import nexus.domain.billing.payment.Payment;
import nexus.infrastructure.persistence.repository.InvoiceRepository;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FileInvoiceRepository extends FileRepository<Invoice> implements InvoiceRepository {
    public FileInvoiceRepository(Path file) { super(file); }

    @Override
    public List<Invoice> findByPatientId(String patientId) {
        return findAll().stream()
                .filter(i -> i.getPatientId().equalsIgnoreCase(patientId.trim()))
                .toList();
    }

    @Override
    public List<Invoice> findPendingInvoices() {
        return findAll().stream().filter(i -> i.getStatus() != InvoiceStatus.PAID).toList();
    }

    @Override
    protected String serialize(Invoice inv) {
        List<String> chargeChunks = new ArrayList<>();
        for (Billable c : inv.getCharges()) {
            chargeChunks.add(clean(c.getCategory()) + ";" + clean(c.getDescription()) + ";" + c.getAmount());
        }

        List<String> payChunks = new ArrayList<>();
        for (Payment p : inv.getPayments()) {
            payChunks.add(p.getId() + ";" + p.getMethod() + ";" + p.getAmount() + ";" + p.getTimestamp() + ";" + clean(p.getTransactionDetails()));
        }

        return String.join("\t",
                inv.getId(),
                inv.getPatientId(),
                inv.getIssueDate().toString(),
                String.valueOf(inv.getDiscountAmount()),
                String.valueOf(inv.getTaxAmount()),
                String.valueOf(inv.getPaidAmount()),
                inv.getStatus().name(),
                String.join("^", chargeChunks),
                String.join("^", payChunks));
    }

    @Override
    protected Invoice deserialize(String[] f) {
        Invoice.Builder b = Invoice.builder()
                .id(f[0])
                .patientId(f[1])
                .issueDate(LocalDate.parse(f[2]))
                .discount(Double.parseDouble(f[3]))
                .tax(Double.parseDouble(f[4]))
                .paid(Double.parseDouble(f[5]))
                .status(InvoiceStatus.valueOf(f[6]));

        if (f.length > 7 && !f[7].isBlank()) {
            for (String chunk : f[7].split("\\^")) {
                if (chunk.isBlank()) continue;
                String[] p = chunk.split(";", -1);
                if (p.length >= 3) {
                    b.charge(new SimpleCharge(p[0], p[1], Double.parseDouble(p[2])));
                }
            }
        }

        if (f.length > 8 && !f[8].isBlank()) {
            for (String chunk : f[8].split("\\^")) {
                if (chunk.isBlank()) continue;
                String[] p = chunk.split(";", -1);
                if (p.length >= 4) {
                    b.payment(new CashPayment(p[0], f[0], Double.parseDouble(p[2]), Instant.parse(p[3]), Double.parseDouble(p[2]), 0.0));
                }
            }
        }
        return b.build();
    }

    private record SimpleCharge(String category, String description, double amount) implements Billable {
        @Override public double getAmount() { return amount; }
        @Override public String getDescription() { return description; }
        @Override public String getCategory() { return category; }
    }
}
