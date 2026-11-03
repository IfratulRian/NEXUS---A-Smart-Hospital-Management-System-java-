package nexus.domain.billing.payment;

/** Factory Pattern: Produces PaymentStrategy instances based on user selection. */
public final class PaymentFactory {
    private PaymentFactory() { }

    public static PaymentStrategy cash(double tenderedAmount) {
        return (paymentId, invoiceId, amount) -> new CashPayment(paymentId, invoiceId, amount, tenderedAmount);
    }

    public static PaymentStrategy card(String cardLastFour, String authCode) {
        return (paymentId, invoiceId, amount) -> new CardPayment(paymentId, invoiceId, amount, cardLastFour, authCode);
    }

    public static PaymentStrategy mobileBanking(String provider, String mobileNumber, String trxId) {
        return (paymentId, invoiceId, amount) -> new MobileBankingPayment(paymentId, invoiceId, amount, provider, mobileNumber, trxId);
    }

    public static PaymentStrategy insurance(String policyNumber, String claimId) {
        return (paymentId, invoiceId, amount) -> new InsurancePayment(paymentId, invoiceId, amount, policyNumber, claimId);
    }
}
