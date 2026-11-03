package nexus.infrastructure.persistence.repository;

import nexus.domain.billing.Invoice;
import java.util.List;

public interface InvoiceRepository extends Repository<Invoice, String> {
    List<Invoice> findByPatientId(String patientId);
    List<Invoice> findPendingInvoices();
}
