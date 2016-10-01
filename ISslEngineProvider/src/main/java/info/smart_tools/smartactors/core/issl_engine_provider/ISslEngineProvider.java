package info.smart_tools.smartactors.core.issl_engine_provider;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.core.issl_engine_provider.exception.SSLEngineProviderException;

import javax.net.ssl.SSLEngine;

/**
 * Interface for ssl engine provider
 */
public interface ISslEngineProvider {

    /**
     * Method for initialize ssl engine provider
     *
     * @param params Parameters for ssl engine provider
     * @throws SSLEngineProviderException if there are some problems on initialization
     */
    void init(final IObject params) throws SSLEngineProviderException;

    /**
     * Method for getting ssl engine
     *
     * @return ssl engine
     */
    SSLEngine getServerContext();

    /**
     * Method for getting ssl engine
     *
     * @return ssl engine
     */
    SSLEngine getClientContext();

    /**
     * Is ssl engine provider initialized
     *
     * @return true if ssl engine provider initialized and false otherwise
     */
    boolean isInitialized();


}
