package uk.gov.dwp.uc.pairtest.exception;

public class InvalidPurchaseException extends RuntimeException {
    // Allows sending descriptive message showing why the purchase failed .
    public InvalidPurchaseException(String message) {
        super(message);
    }

}
