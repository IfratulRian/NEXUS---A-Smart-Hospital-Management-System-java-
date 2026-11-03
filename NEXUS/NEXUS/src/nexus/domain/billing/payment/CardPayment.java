package nexus.domain.billing.payment;

import java.time.Instant;

public class CardPayment extends Payment {
    private final String cardLastFour;
    private final String authCode;

    public CardPayment(String id, String invoiceId, double amount, String cardLastFour, String authCode) {
        this(id, invoiceId, amount, Instant.now(), cardLastFour, authCode);
    }

    public CardPayment(String id, String invoiceId, double amount, Instant timestamp, String cardLastFour, String authCode) {
        super(id, invoiceId, amount, timestamp, "CARD");
        this.cardLastFour = cardLastFour == null ? "XXXX" : cardLastFour.trim();
        this.authCode = authCode == null ? "AUTH" : authCode.trim();
    }

    public String getCardLastFour() { return cardLastFour; }
    public String getAuthCode() { return authCode; }

    @Override
    public String getTransactionDetails() {
        return "Card ending in " + cardLastFour + " (Auth: " + authCode + ")";
    }
}
