package nexus.domain.billing.payment;

import java.time.Instant;

public class MobileBankingPayment extends Payment {
    private final String provider;
    private final String mobileNumber;
    private final String transactionId;

    public MobileBankingPayment(String id, String invoiceId, double amount, String provider, String mobileNumber, String transactionId) {
        this(id, invoiceId, amount, Instant.now(), provider, mobileNumber, transactionId);
    }

    public MobileBankingPayment(String id, String invoiceId, double amount, Instant timestamp,
                                String provider, String mobileNumber, String transactionId) {
        super(id, invoiceId, amount, timestamp, "MOBILE_BANKING");
        this.provider = provider == null ? "MobileBanking" : provider.trim();
        this.mobileNumber = mobileNumber == null ? "" : mobileNumber.trim();
        this.transactionId = transactionId == null ? "TRX" : transactionId.trim();
    }

    public String getProvider() { return provider; }
    public String getMobileNumber() { return mobileNumber; }
    public String getTransactionId() { return transactionId; }

    @Override
    public String getTransactionDetails() {
        return provider + " [" + mobileNumber + "] TrxID: " + transactionId;
    }
}
