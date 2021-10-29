package trybin.fdg.exception;

public class DataGenerateException extends RuntimeException{
    public DataGenerateException() {
    }

    public DataGenerateException(String message) {
        super(message);
    }

    public DataGenerateException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataGenerateException(Throwable cause) {
        super(cause);
    }

    public DataGenerateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
