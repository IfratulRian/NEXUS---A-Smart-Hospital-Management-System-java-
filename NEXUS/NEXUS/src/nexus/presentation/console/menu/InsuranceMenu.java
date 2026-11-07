package nexus.presentation.console.menu;

import nexus.application.billing.BillingService;
import nexus.application.insurance.InsuranceService;
import nexus.domain.insurance.Claim;
import nexus.domain.insurance.InsurancePolicy;
import nexus.presentation.console.input.ConsoleInput;
import nexus.presentation.console.output.ConsoleOutput;
import nexus.security.authentication.SessionContext;
import nexus.security.authorization.AuthorizationService;
import nexus.security.role.Permission;
import nexus.utility.validation.Validators;
import java.time.LocalDate;
import java.util.List;

public class InsuranceMenu extends Menu {
    private final InsuranceService insuranceService;
    private final BillingService billingService;

    public InsuranceMenu(InsuranceService insuranceService, BillingService billingService,
                         ConsoleInput in, ConsoleOutput out,
                         SessionContext session, AuthorizationService authorization) {
        super("HEALTH INSURANCE & CLAIMS", in, out, session, authorization);
        this.insuranceService = insuranceService;
        this.billingService = billingService;
    }

    @Override
    protected void registerItems() {
        add("Register patient policy", Permission.INSURANCE_MANAGE, this::registerPolicy);
        add("Submit insurance claim", Permission.INSURANCE_MANAGE, this::submitClaim);
        add("Review claim (Adjudication)", Permission.INSURANCE_MANAGE, this::reviewClaim);
        add("Approve claim", Permission.INSURANCE_MANAGE, this::approveClaim);
        add("Reject claim", Permission.INSURANCE_MANAGE, this::rejectClaim);
        add("Disburse approved claim to invoice", Permission.INSURANCE_MANAGE, this::settleClaim);
        add("List all claims", Permission.INSURANCE_MANAGE, () -> printClaims(insuranceService.listClaims()));
        add("List all policies", Permission.INSURANCE_MANAGE, () -> printPolicies(insuranceService.listPolicies()));
    }

    private void registerPolicy() {
        String polNum = in.readValidated("Policy number", Validators.NOT_BLANK);
        String prov = in.readValidated("Insurance provider name", Validators.NOT_BLANK);
        String patId = in.readLine("Patient ID (e.g. PAT-00001)");
        double cov = in.readNonNegativeDouble("Coverage percentage (e.g. 80.0)");
        double maxCov = in.readNonNegativeDouble("Maximum coverage limit (BDT)");
        LocalDate exp = in.readDate("Policy expiry date");

        InsurancePolicy pol = insuranceService.registerPolicy(new InsurancePolicy(polNum, prov, patId, cov, maxCov, exp));
        out.success("Insurance policy registered: " + pol.getPolicyNumber() + " with " + pol.getCoveragePercent() + "% coverage.");
    }

    private void submitClaim() {
        String polNum = in.readLine("Policy number");
        String invId = in.readLine("Invoice ID");
        double amount = in.readNonNegativeDouble("Claim amount (BDT)");
        Claim claim = insuranceService.submitClaim(polNum, invId, amount);
        out.success("Claim submitted with ID " + claim.getId() + " [Status: " + claim.getStatus() + "]");
    }

    private void reviewClaim() {
        String claimId = in.readLine("Claim ID (e.g. CLM-00001)");
        Claim claim = insuranceService.reviewClaim(claimId);
        out.success("Claim " + claim.getId() + " status is now " + claim.getStatus());
    }

    private void approveClaim() {
        String claimId = in.readLine("Claim ID");
        double approvedAmount = in.readNonNegativeDouble("Approved settlement amount (BDT)");
        Claim claim = insuranceService.approveClaim(claimId, approvedAmount);
        out.success("Claim " + claim.getId() + " approved for BDT " + String.format("%.2f", claim.getApprovedAmount()));
    }

    private void rejectClaim() {
        String claimId = in.readLine("Claim ID");
        String reason = in.readLine("Rejection reason");
        Claim claim = insuranceService.rejectClaim(claimId, reason);
        out.success("Claim " + claim.getId() + " rejected. Reason: " + claim.getNotes());
    }

    private void settleClaim() {
        String claimId = in.readLine("Claim ID");
        Claim claim = insuranceService.settleClaim(claimId, billingService);
        out.success("Claim " + claim.getId() + " disbursed and applied to Invoice " + claim.getInvoiceId() + " [Status: PAID]");
    }

    private void printClaims(List<Claim> claims) {
        if (claims.isEmpty()) { out.info("No claims found."); return; }
        out.table(List.of("Claim ID", "Policy #", "Patient", "Invoice", "Claimed", "Approved", "Status"),
                claims.stream().map(c -> List.of(
                        c.getId(), c.getPolicyNumber(), c.getPatientId(), c.getInvoiceId(),
                        String.format("%.2f", c.getClaimAmount()),
                        String.format("%.2f", c.getApprovedAmount()),
                        c.getStatus().name())).toList());
    }

    private void printPolicies(List<InsurancePolicy> policies) {
        if (policies.isEmpty()) { out.info("No policies found."); return; }
        out.table(List.of("Policy #", "Provider", "Patient", "Coverage %", "Remaining BDT", "Expiry"),
                policies.stream().map(p -> List.of(
                        p.getPolicyNumber(), p.getProviderName(), p.getPatientId(),
                        p.getCoveragePercent() + "%",
                        String.format("%.2f", p.getRemainingCoverage()),
                        p.getExpiryDate().toString())).toList());
    }
}
