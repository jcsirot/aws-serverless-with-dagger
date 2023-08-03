package translate.ports.out;

public class TranslateServiceException extends Exception {

  public TranslateServiceException(String message) {
    super(message);
  }

  public TranslateServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public TranslateServiceException(Throwable cause) {
    super(cause);
  }

  public TranslateServiceException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
