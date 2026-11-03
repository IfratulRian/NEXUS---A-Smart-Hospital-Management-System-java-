package nexus.domain.medical;

import java.time.LocalDate;

/** Value object: laboratory or diagnostic test result. */
public record TestResult(String testName, String category, String resultValue, String referenceRange,
                         LocalDate testDate, String remarks) {
    public TestResult(String testName, String category, String resultValue, String referenceRange, String remarks) {
        this(testName, category, resultValue, referenceRange, LocalDate.now(), remarks);
    }
}
