package info.smart_tools.smartactors.core.iresponse;

/**
 * Interface for response object
 */
public interface IResponse {
    /**
     * Method, that setting some environment
     * @param key Key of the environment
     * @param environment Environment object
     */
    void setEnvironment(String key, Object environment);

    /**
     * Method, that setting content for response
     * @param response Byte array of response
     */
    void setContent(final byte[] response);

    /**
     * @return content of the response
     */
    byte[] getContent();

    /**
     * @param key key of the environment
     * @param <T> Type of the environment
     * @return Environment object
     */
    <T> T getEnvironment(String key);
}
