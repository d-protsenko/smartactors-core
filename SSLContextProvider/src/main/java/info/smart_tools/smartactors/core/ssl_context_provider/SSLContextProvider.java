package info.smart_tools.smartactors.core.ssl_context_provider;


import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.ssl_context_provider.exceptions.SSLContextProviderException;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Scanner;

/**
 * Class for getting ssl context
 */
public class SSLContextProvider {

    private SslContext sslContext = null;
    private String keyPass;
    private String certKey;
    private String certPath;
    private String certType;
    private boolean initialized = false;
    private KeyStore keyStore;

    /**
     * Method for initialize {@link SSLContextProvider}
     *
     * @param params parameters of the configuration of the https endpoint
     * @throws SSLContextProviderException if there are not all fields at endpoint configuration
     */
    public void init(final IObject params) throws SSLContextProviderException {
        IFieldName certPassFieldName = null;
        IFieldName certPathFieldName = null;
        IFieldName keyPassFieldName = null;
        try {
            certPassFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "certPass");
            certPathFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "certPath");
        } catch (ResolutionException e) {
            throw new SSLContextProviderException("An exception on resolving \"FieldName\"", e);
        }
        try {
            certKey = (String) params.getValue(certPassFieldName);
            certPath = (String) params.getValue(certPathFieldName);
            initialized = certKey != null && certPath != null;

            if (initialized) {
                SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(new File(certPath), new File(certKey));
                sslContext = sslContextBuilder.build();
            }
        } catch (Exception e) {
            throw new SSLContextProviderException("An exception on getting values from parameters", e);
        }
    }


    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Method for getting ssl context
     *
     * @return ssl context
     * @throws SSLContextProviderException if there are some problems on getting ssl context
     */
    public SslContext get() throws SSLContextProviderException {
        return sslContext;
    }


}