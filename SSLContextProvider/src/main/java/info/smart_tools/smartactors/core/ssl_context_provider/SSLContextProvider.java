package info.smart_tools.smartactors.core.ssl_context_provider;


import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.ssl_context_provider.exceptions.SSLContextProviderException;

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

    private SSLContext sslContext = null;
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
        IFieldName certKeyFieldName = null;
        IFieldName certPathFieldName = null;
        IFieldName keyPassFieldName = null;
        try {
            certKeyFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "certKey");
            certPathFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "keystorePath");
            keyPassFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "keystorePass");
        } catch (ResolutionException e) {
            throw new SSLContextProviderException("An exception on resolving \"FieldName\"", e);
        }
        try {
            certKey = (String) params.getValue(certKeyFieldName);
            keyPass = (String) params.getValue(keyPassFieldName);
            certPath = (String) params.getValue(certPathFieldName);
            initialized = certKey != null && certPath != null;

            if (initialized) {
                final KeyStore keyStore = KeyStore.getInstance("JKS");
                try (final InputStream is = new FileInputStream(certPath)) {
                    keyStore.load(is, certKey.toCharArray());
                }
                final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                        .getDefaultAlgorithm());
                kmf.init(keyStore, keyPass.toCharArray());
                final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory
                        .getDefaultAlgorithm());
                tmf.init(keyStore);

                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
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
    public SSLContext get() throws SSLContextProviderException {
        return sslContext;
    }


}