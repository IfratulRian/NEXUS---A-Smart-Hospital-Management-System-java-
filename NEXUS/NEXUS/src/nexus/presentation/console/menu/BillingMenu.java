package nexus.presentation.console.menu;

import nexus.application.billing.BillingService;
import nexus.domain.billing.Billable;
import nexus.domain.billing.Invoice;
import nexus.domain.billing.charges.ConsultationCharge;
import nexus.domain.billing.charges.EmergencyCharge;
import nexus.domain.billing.charges.LaboratoryCharge;
import nexus.domain.billing.charges.MedicineCharge;
import nexus.domain.billing.charges.RoomCharge;
import nexus.domain.billing.charges.SurgeryCharge;
import nexus.domain.billing.payment.Payment;
import nexus.domain.billing.payment.PaymentFactory;
import nexus.domain.billing.payment.PaymentStrategy;
import nexus.presentation.console.input.ConsoleInput;
import nexus.presentation.console.output.ConsoleOutput;
import nexus.security.authentication.SessionContext;
import nexus.security.authorization.AuthorizationService;
import nexus.security.role.Permission;
import java.util.ArrayList;
import java.util.List;

public class BillingMenu extends Menu {
    private final BillingService service;

    public BillingMenu(BillingService service, ConsoleInput in, ConsoleOutput out,
                       SessionContext session, AuthorizationService authorization) {
        super("BILLING & INVOICE MANAGEMENT", in, out, session, authorization);
        this.service = service;
    }

    @Override
    protected void registerItems() {
        add("Create new patient invoice", Permission.BILLING_MANAGE, this::createInvoice);
        add("Add charge item to invoice", Permission.BILLING_MANAGE, this::addCharge);
        add("Process payment for invoice", Permission.BILLING_MANAGE, this::processPayment);
        add("View invoice details", Permission.BILLING_MANAGE, this::viewInvoice);
        add("List all invoices", Permission.BILLING_MANAGE, () -> printTable(service.listAll()));
        add("List unpaid / pending invoices", Permission.BILLING_MANAGE, () -> printTable(service.listPendingInvoices()));
    }

    private void createInvoice() {
        String patientId = in.readLine("Patient ID (e.g. PAT-00001)");
        double discount = in.readNonNegativeDouble("Discount amount (BDT)");
        double tax = in.readNonNegativeDouble("Tax / VAT amount (BDT)");
        Invoice inv = service.createInvoice(patientId, new ArrayList<>(), discount, tax);
        out.success("Invoice created with ID " + inv.getId() + " for patient " + inv.getPatientId());
    }

    private void addCharge() {
        String invId = in.readLine("Invoice ID (e.g. INV-00001)");
        out.println("Select charge type to add:");
        out.println("  1. Doctor Consultation Charge");
        out.println("  2. Pharmacy / Medicine Charge");
        out.println("  3. Room / Bed Accommodation Charge");
        out.println("  4. Laboratory Diagnostic Charge");
        out.println("  5. Emergency Trauma Charge");
        out.println("  6. Surgery / Operation Charge");
        int opt = in.readInt("Option", 1, 6);

        Billable charge = switch (opt) {
            case 1 -> new ConsultationCharge(in.readLine("Doctor ID"), in.readLine("Doctor Name"), in.readNonNegativeDouble("Consultation fee"));
            case 2 -> new MedicineCharge(in.readLine("Medicine ID"), in.readLine("Medicine Name"),
                    in.readInt("Quantity", 1, 1000), in.readNonNegativeDouble("Unit price"));
            case 3 -> new RoomCharge(in.readLine("Room / Bed ID"), in.readLine("Room Type"),
                    in.readInt("Days stayed", 1, 365), in.readNonNegativeDouble("Daily rate"));
            case 4 -> new LaboratoryCharge(in.readLine("Test Name"), in.readLine("Category"), in.readNonNegativeDouble("Test fee"));
            case 5 -> new EmergencyCharge(in.readLine("Emergency Case ID"), in.readLine("Triage Level"), in.readNonNegativeDouble("Trauma care fee"));
            case 6 -> new SurgeryCharge(in.readLine("Procedure Name"), in.readLine("Lead Surgeon"),
                    in.readNonNegativeDouble("Surgeon fee"), in.readNonNegativeDouble("OT facility fee"));
            default -> throw new IllegalStateException();
        };

        Invoice updated = service.addCharge(invId, charge);
        out.success("Charge added. New Invoice Subtotal: BDT " + String.format("%.2f", updated.getSubtotal())
                + " | Total Due: BDT " + String.format("%.2f", updated.getBalanceDue()));
    }

    private void processPayment() {
        String invId = in.readLine("Invoice ID");
        Invoice inv = service.getInvoice(invId);
        if (inv.isPaid()) {
            out.info("Invoice " + invId + " is already fully paid.");
            return;
        }

        out.info("Total Amount: BDT " + String.format("%.2f", inv.getTotalAmount())
                + " | Paid: BDT " + String.format("%.2f", inv.getPaidAmount())
                + " | Balance Due: BDT " + String.format("%.2f", inv.getBalanceDue()));

        double amount = in.readNonNegativeDouble("Payment amount to collect (BDT)");

        out.println("Select payment method:");
        out.println("  1. Cash (Strategy)");
        out.println("  2. Credit / Debit Card (Strategy)");
        out.println("  3. Mobile Banking [bKash/Nagad] (Strategy)");
        out.println("  4. Insurance Settlement (Strategy)");
        int methodChoice = in.readInt("Method", 1, 4);

        PaymentStrategy strategy = switch (methodChoice) {
            case 1 -> {
                double tendered = in.readNonNegativeDouble("Tendered cash received (BDT)");
                yield PaymentFactory.cash(tendered);
            }
            case 2 -> {
                String cardLast4 = in.readLine("Card last 4 digits");
                String auth = in.readLine("Auth approval code");
                yield PaymentFactory.card(cardLast4, auth);
            }
            case 3 -> {
                String prov = in.readLine("Provider (e.g. bKash, Nagad)");
                String phone = in.readLine("Wallet phone number");
                String trx = in.readLine("Transaction ID (TrxID)");
                yield PaymentFactory.mobileBanking(prov, phone, trx);
            }
            case 4 -> {
                String pol = in.readLine("Insurance Policy Number");
                String claim = in.readLine("Approved Claim ID");
                yield PaymentFactory.insurance(pol, claim);
            }
            default -> throw new IllegalStateException();
        };

        Payment p = service.processPayment(invId, strategy, amount);
        out.success("Payment successful! Receipt ID: " + p.getId());
        out.info("Method: " + p.getMethod() + " | " + p.getTransactionDetails());
        Invoice refreshed = service.getInvoice(invId);
        out.info("Remaining Balance Due: BDT " + String.format("%.2f", refreshed.getBalanceDue()) + " (Status: " + refreshed.getStatus() + ")");
    }

    private void viewInvoice() {
        String invId = in.readLine("Invoice ID");
        Invoice inv = service.getInvoice(invId);
        out.header("INVOICE " + inv.getId());
        out.field("Patient ID", inv.getPatientId());
        out.field("Issue Date", inv.getIssueDate());
        out.field("Status", inv.getStatus());
        out.println("\n  --- Line Items ---");
        for (Billable c : inv.getCharges()) {
            out.info(String.format("  [%s] %s : BDT %.2f", c.getCategory(), c.getDescription(), c.getAmount()));
        }
        out.println("\n  --- Summary ---");
        out.field("Subtotal", String.format("BDT %.2f", inv.getSubtotal()));
        out.field("Discount", String.format("BDT %.2f", inv.getDiscountAmount()));
        out.field("Tax", String.format("BDT %.2f", inv.getTaxAmount()));
        out.field("Total", String.format("BDT %.2f", inv.getTotalAmount()));
        out.field("Paid", String.format("BDT %.2f", inv.getPaidAmount()));
        out.field("Balance Due", String.format("BDT %.2f", inv.getBalanceDue()));

        if (!inv.getPayments().isEmpty()) {
            out.println("\n  --- Payments Received ---");
            for (Payment p : inv.getPayments()) {
                out.info(String.format("  %s | %s | BDT %.2f (%s)", p.getId(), p.getMethod(), p.getAmount(), p.getTransactionDetails()));
            }
        }
        out.footer();
    }

    private void printTable(List<Invoice> invoices) {
        if (invoices.isEmpty()) { out.info("No invoices found."); return; }
        out.table(List.of("ID", "Patient", "Date", "Total", "Paid", "Due", "Status"),
                invoices.stream().map(i -> List.of(
                        i.getId(), i.getPatientId(), i.getIssueDate().toString(),
                        String.format("%.2f", i.getTotalAmount()),
                        String.format("%.2f", i.getPaidAmount()),
                        String.format("%.2f", i.getBalanceDue()),
                        i.getStatus().name())).toList());
    }
}
