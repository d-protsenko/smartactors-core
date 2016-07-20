package info.smart_tools.smartactors.core.icookies_extractor;

import info.smart_tools.smartactors.core.iobject.IObject;

/**
 * Interface for extracting cookies from environment
 */
public interface ICookiesSetter {
    /**
     * Method for setting cookies from environment
     * @param response Response object in which cookies will add
     * @param environment Environment of the message processor
     */
    void set(Object response, IObject environment);
}
