package tv.loilo.promise;

/**
 * An Exception that Defer callback returned null.
 */
public class NullDeferredException extends RuntimeException {
    public NullDeferredException(String detailMessage) {
        super(detailMessage);
    }
}
