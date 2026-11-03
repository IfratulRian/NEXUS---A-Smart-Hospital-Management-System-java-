package nexus.infrastructure.persistence.file;

import nexus.domain.insurance.InsurancePolicy;
import nexus.infrastructure.persistence.repository.InsurancePolicyRepository;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FileInsurancePolicyRepository extends FileRepository<InsurancePolicy> implements InsurancePolicyRepository {
    public FileInsurancePolicyRepository(Path file) { super(file); }

    @Override
    public Optional<InsurancePolicy> findByPatientId(String patientId) {
        return findAll().stream()
                .filter(p -> p.getPatientId().equalsIgnoreCase(patientId.trim()))
                .findFirst();
    }

    @Override
    public List<InsurancePolicy> findByProvider(String providerName) {
        String needle = providerName.toLowerCase();
        return findAll().stream()
                .filter(p -> p.getProviderName().toLowerCase().contains(needle))
                .toList();
    }

    @Override
    protected String serialize(InsurancePolicy p) {
        return String.join("\t",
                p.getPolicyNumber(),
                clean(p.getProviderName()),
                p.getPatientId(),
                String.valueOf(p.getCoveragePercent()),
                String.valueOf(p.getMaxCoverage()),
                String.valueOf(p.getUsedAmount()),
                p.getExpiryDate().toString());
    }

    @Override
    protected InsurancePolicy deserialize(String[] f) {
        return new InsurancePolicy(f[0], f[1], f[2], Double.parseDouble(f[3]),
                Double.parseDouble(f[4]), Double.parseDouble(f[5]), LocalDate.parse(f[6]));
    }
}
