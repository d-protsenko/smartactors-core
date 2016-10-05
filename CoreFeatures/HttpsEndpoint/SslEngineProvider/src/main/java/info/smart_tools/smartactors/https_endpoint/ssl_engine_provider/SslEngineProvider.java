package info.smart_tools.smartactors.https_endpoint.ssl_engine_provider;


import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.https_endpoint.interfaces.issl_engine_provider.ISslEngineProvider;
import info.smart_tools.smartactors.https_endpoint.interfaces.issl_engine_provider.exception.SSLEngineProviderException;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import java.io.File;

/**
 * Class for getting ssl engine
 */
public class SslEngineProvider implements ISslEngineProvider {

    private SslContext sslServerContext = null;
    private SslContext sslClientContext = null;
    private String certKey;
    private String certPath;
    private boolean initialized = false;

    @Override
    public void init(final IObject params) throws SSLEngineProviderException {
        IFieldName certPassFieldName = null;
        IFieldName certPathFieldName = null;
        IFieldName keyPassFieldName = null;
        try {
            certPassFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "certPass");
            certPathFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "certPath");
        } catch (ResolutionException e) {
            throw new SSLEngineProviderException("An exception on resolving \"FieldName\"", e);
        }
        if (params != null) {
            try {
                certKey = (String) params.getValue(certPassFieldName);
                certPath = (String) params.getValue(certPathFieldName);
                initialized = certKey != null && certPath != null;
            } catch (Exception e) {
                throw new SSLEngineProviderException("An exception on getting values from parameters", e);
            }
        }
        try {
            if (initialized) {
                SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(new File(certPath), new File(certKey));
                sslServerContext = sslContextBuilder.build();
            }
            SslContextBuilder sslClientContextBuilder = SslContextBuilder.forClient();
            sslClientContext = sslClientContextBuilder.build();
        } catch (SSLException e) {
            throw new SSLEngineProviderException("An exception on building ssl context", e);
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public SSLEngine getServerContext() {
        return sslServerContext.newEngine(ByteBufAllocator.DEFAULT);
    }

    @Override
    public SSLEngine getClientContext() {
        return sslClientContext.newEngine(ByteBufAllocator.DEFAULT);
    }


}