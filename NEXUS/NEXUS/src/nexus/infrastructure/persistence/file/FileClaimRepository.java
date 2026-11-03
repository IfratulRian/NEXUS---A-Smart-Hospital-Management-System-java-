package nexus.infrastructure.persistence.file;

import nexus.domain.insurance.Claim;
import nexus.domain.insurance.ClaimStatus;
import nexus.infrastructure.persistence.repository.ClaimRepository;
import java.nio.file.Path;
import java.util.List;

public class FileClaimRepository extends FileRepository<Claim> implements ClaimRepository {
    public FileClaimRepository(Path file) { super(file); }

    @Override
    public List<Claim> findByPatientId(String patientId) {
        return findAll().stream().filter(c -> c.getPatientId().equalsIgnoreCase(patientId.trim())).toList();
    }

    @Override
    public List<Claim> findByPolicyNumber(String policyNumber) {
        return findAll().stream().filter(c -> c.getPolicyNumber().equalsIgnoreCase(policyNumber.trim())).toList();
    }

    @Override
    public List<Claim> findByStatus(ClaimStatus status) {
        return findAll().stream().filter(c -> c.getStatus() == status).toList();
    }

    @Override
    protected String serialize(Claim c) {
        return String.join("\t",
                c.getId(),
                c.getPolicyNumber(),
                c.getPatientId(),
                c.getInvoiceId(),
                String.valueOf(c.getClaimAmount()),
                String.valueOf(c.getApprovedAmount()),
                c.getStatus().name(),
                clean(c.getNotes()));
    }

    @Override
    protected Claim deserialize(String[] f) {
        return new Claim(f[0], f[1], f[2], f[3], Double.parseDouble(f[4]),
                Double.parseDouble(f[5]), ClaimStatus.valueOf(f[6]), f.length > 7 ? f[7] : "");
    }
}
