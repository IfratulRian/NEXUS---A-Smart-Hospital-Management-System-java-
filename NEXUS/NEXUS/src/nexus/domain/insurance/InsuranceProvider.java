package nexus.domain.insurance;

import nexus.domain.common.Identifiable;

public record InsuranceProvider(String id, String name, String contactPhone, String email) implements Identifiable<String> {
    @Override public String getId() { return id; }
}
