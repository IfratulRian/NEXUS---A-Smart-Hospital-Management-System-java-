package nexus.application.insurance;

import nexus.application.billing.BillingService;
import nexus.application.patient.PatientService;
import nexus.domain.billing.payment.PaymentFactory;
import nexus.domain.insurance.Claim;
import nexus.domain.insurance.ClaimStatus;
import nexus.domain.insurance.InsurancePolicy;
import nexus.domain.people.patient.Patient;
import nexus.exception.EntityNotFoundException;
import nexus.exception.ValidationException;
import nexus.infrastructure.logging.AuditAction;
import nexus.infrastructure.logging.AuditEntry;
import nexus.infrastructure.logging.AuditLogger;
import nexus.infrastructure.persistence.repository.ClaimRepository;
import nexus.infrastructure.persistence.repository.InsurancePolicyRepository;
import nexus.security.authentication.SessionContext;
import nexus.utility.id.IdGenerator;
import java.util.List;
import java.util.Optional;

public class InsuranceService {
    private final InsurancePolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final PatientService patientService;
    private final IdGenerator claimIdGen;
    private final AuditLogger audit;
    private final SessionContext session;

    public InsuranceService(InsurancePolicyRepository policyRepository, ClaimRepository claimRepository,
                            PatientService patientService, IdGenerator claimIdGen,
                            AuditLogger audit, SessionContext session) {
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
        this.patientService = patientService;
        this.claimIdGen = claimIdGen;
        this.audit = audit;
        this.session = session;
    }

    public InsurancePolicy registerPolicy(InsurancePolicy policy) {
        Patient p = patientService.getPatient(policy.getPatientId());
        policyRepository.save(policy);
        return policy;
    }

    public InsurancePolicy getPolicy(String policyNumber) {
        return policyRepository.findById(policyNumber.trim().toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("Policy " + policyNumber + " not found"));
    }

    public Optional<InsurancePolicy> getPolicyForPatient(String patientId) {
        return policyRepository.findByPatientId(patientId);
    }

    public Claim submitClaim(String policyNumber, String invoiceId, double claimAmount) {
        InsurancePolicy policy = getPolicy(policyNumber);
        if (policy.isExpired()) {
            throw new ValidationException("Cannot submit claim: Insurance policy " + policyNumber + " is expired");
        }
        Claim claim = new Claim(claimIdGen.next(), policy.getPolicyNumber(), policy.getPatientId(), invoiceId, claimAmount);
        claimRepository.save(claim);
        audit.log(AuditEntry.of(session.currentUsername(), AuditAction.USER_LOGIN, "Claim", claim.getId(), "SUBMITTED"));
        return claim;
    }

    public Claim reviewClaim(String claimId) {
        Claim claim = getClaim(claimId);
        claim.review();
        claimRepository.save(claim);
        return claim;
    }

    public Claim approveClaim(String claimId, double approvedAmount) {
        Claim claim = getClaim(claimId);
        InsurancePolicy policy = getPolicy(claim.getPolicyNumber());
        double eligible = policy.calculateEligibleCoverage(claim.getClaimAmount());
        if (approvedAmount > eligible) {
            throw new ValidationException("Approved amount ($" + approvedAmount + ") exceeds eligible policy coverage ($" + eligible + ")");
        }
        policy.deduct(approvedAmount);
        policyRepository.save(policy);

        claim.approve(approvedAmount);
        claimRepository.save(claim);
        return claim;
    }

    public Claim rejectClaim(String claimId, String reason) {
        Claim claim = getClaim(claimId);
        claim.reject(reason);
        claimRepository.save(claim);
        return claim;
    }

    public Claim settleClaim(String claimId, BillingService billingService) {
        Claim claim = getClaim(claimId);
        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new ValidationException("Only approved claims can be disbursed (current status: " + claim.getStatus() + ")");
        }
        billingService.processPayment(claim.getInvoiceId(),
                PaymentFactory.insurance(claim.getPolicyNumber(), claim.getId()),
                claim.getApprovedAmount());
        claim.markPaid();
        claimRepository.save(claim);
        return claim;
    }

    public Claim getClaim(String id) {
        return claimRepository.findById(id.trim().toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("Claim " + id + " not found"));
    }

    public List<Claim> listClaims() {
        return claimRepository.findAll();
    }

    public List<InsurancePolicy> listPolicies() {
        return policyRepository.findAll();
    }
}
