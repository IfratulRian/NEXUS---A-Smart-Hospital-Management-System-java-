package nexus.domain.billing;

/** Interface for all billable hospital charges, enabling polymorphic invoice aggregation. */
public interface Billable {
    double getAmount();
    String getDescription();
    String getCategory();
}
