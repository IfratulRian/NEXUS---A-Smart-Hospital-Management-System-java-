package nexus.infrastructure.persistence.repository;

import nexus.domain.insurance.Claim;
import nexus.domain.insurance.ClaimStatus;
import java.util.List;

public interface ClaimRepository extends Repository<Claim, String> {
    List<Claim> findByPatientId(String patientId);
    List<Claim> findByPolicyNumber(String policyNumber);
    List<Claim> findByStatus(ClaimStatus status);
}
