package nexus.application.billing;

import nexus.application.patient.PatientService;
import nexus.domain.billing.Billable;
import nexus.domain.billing.Invoice;
import nexus.domain.billing.PaymentCompletedEvent;
import nexus.domain.billing.payment.Payment;
import nexus.domain.billing.payment.PaymentStrategy;
import nexus.domain.people.patient.Patient;
import nexus.exception.EntityNotFoundException;
import nexus.exception.ValidationException;
import nexus.infrastructure.logging.AuditAction;
import nexus.infrastructure.logging.AuditEntry;
import nexus.infrastructure.logging.AuditLogger;
import nexus.infrastructure.persistence.repository.InvoiceRepository;
import nexus.patterns.observer.EventBus;
import nexus.security.authentication.SessionContext;
import nexus.utility.id.IdGenerator;
import java.time.LocalDate;
import java.util.List;

public class BillingService {
    private final InvoiceRepository repository;
    private final PatientService patientService;
    private final IdGenerator invoiceIdGen;
    private final IdGenerator paymentIdGen;
    private final EventBus eventBus;
    private final AuditLogger audit;
    private final SessionContext session;

    public BillingService(InvoiceRepository repository, PatientService patientService,
                          IdGenerator invoiceIdGen, IdGenerator paymentIdGen,
                          EventBus eventBus, AuditLogger audit, SessionContext session) {
        this.repository = repository;
        this.patientService = patientService;
        this.invoiceIdGen = invoiceIdGen;
        this.paymentIdGen = paymentIdGen;
        this.eventBus = eventBus;
        this.audit = audit;
        this.session = session;
    }

    public Invoice createInvoice(String patientId, List<Billable> charges, double discountAmount, double taxAmount) {
        Patient patient = patientService.getPatient(patientId);
        Invoice invoice = Invoice.builder()
                .id(invoiceIdGen.next())
                .patientId(patient.getId())
                .issueDate(LocalDate.now())
                .charges(charges)
                .discount(discountAmount)
                .tax(taxAmount)
                .build();
        repository.save(invoice);
        return invoice;
    }

    public Invoice addCharge(String invoiceId, Billable charge) {
        Invoice inv = getInvoice(invoiceId);
        inv.addCharge(charge);
        repository.save(inv);
        return inv;
    }

    public Payment processPayment(String invoiceId, PaymentStrategy strategy, double amount) {
        Invoice inv = getInvoice(invoiceId);
        if (amount <= 0) throw new ValidationException("Payment amount must be greater than zero");
        if (amount > inv.getBalanceDue() + 0.001) {
            throw new ValidationException("Payment amount exceeds balance due (BDT " + String.format("%.2f", inv.getBalanceDue()) + ")");
        }

        Payment payment = strategy.pay(paymentIdGen.next(), inv.getId(), amount);
        inv.recordPayment(payment);
        repository.save(inv);

        audit.log(AuditEntry.of(session.currentUsername(), AuditAction.PAYMENT_COMPLETED, "Payment", payment.getId(), "SUCCESS"));
        eventBus.publish(new PaymentCompletedEvent(inv, payment));
        return payment;
    }

    public Invoice getInvoice(String id) {
        return repository.findById(id.trim().toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("Invoice " + id + " not found"));
    }

    public List<Invoice> listInvoicesForPatient(String patientId) {
        return repository.findByPatientId(patientId);
    }

    public List<Invoice> listPendingInvoices() {
        return repository.findPendingInvoices();
    }

    public List<Invoice> listAll() {
        return repository.findAll();
    }

    public double getTotalRevenue() {
        return repository.findAll().stream().mapToDouble(Invoice::getPaidAmount).sum();
    }

    public double getTotalOutstanding() {
        return repository.findAll().stream().mapToDouble(Invoice::getBalanceDue).sum();
    }
}
