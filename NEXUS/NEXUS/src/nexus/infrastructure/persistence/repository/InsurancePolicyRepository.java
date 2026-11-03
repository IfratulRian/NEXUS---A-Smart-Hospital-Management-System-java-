package nexus.infrastructure.persistence.repository;

import nexus.domain.insurance.InsurancePolicy;
import java.util.List;
import java.util.Optional;

public interface InsurancePolicyRepository extends Repository<InsurancePolicy, String> {
    Optional<InsurancePolicy> findByPatientId(String patientId);
    List<InsurancePolicy> findByProvider(String providerName);
}
