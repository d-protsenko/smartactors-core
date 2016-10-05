package info.smart_tools.smartactors.endpoint.interfaces.iresponse_handler;

/**
 * Interface for response handlers
 */
public interface IResponseHandler<T, V> {
    void handle(T ctx, V response);

    boolean isReceived();
}
