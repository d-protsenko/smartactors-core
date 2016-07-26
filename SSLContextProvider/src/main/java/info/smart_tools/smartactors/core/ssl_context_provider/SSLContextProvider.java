package info.smart_tools.smartactors.core.ssl_context_provider;


import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.ssl_context_provider.exceptions.SSLContextProviderException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;

/**
 * Class for getting ssl context
 */
public class SSLContextProvider {

    private SSLContext sslContext = null;
    private String storePass;
    private String keyPass;
    private String certPath;
    private String certType;

    /**
     * Method for initialize {@link SSLContextProvider}
     *
     * @param params parameters of the configuration of the https endpoint
     * @throws SSLContextProviderException if there are not all fields at endpoint configuration
     */
    public void init(final IObject params) throws SSLContextProviderException {
        IFieldName storePassFieldName = null;
        IFieldName keyPassFieldName = null;
        IFieldName certPathFieldName = null;
        IFieldName certTypeFieldName = null;
        FileSystem fileSystem = FileSystems.getDefault();

        try {
            storePassFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "storePass");
            keyPassFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "keyPass");
            certPathFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "certPath");
            certTypeFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "certType");
        } catch (ResolutionException e) {
            throw new SSLContextProviderException("An exception on resolving \"FieldName\"", e);
        }
        try {
            storePass = (String) params.getValue(storePassFieldName);
            keyPass = (String) params.getValue(keyPassFieldName);
            certPath = (String) params.getValue(certPathFieldName);
            certType = (String) params.getValue(certTypeFieldName);
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new SSLContextProviderException("An exception on getting values from parameters", e);
        }
    }


    /**
     * Method for getting ssl context
     *
     * @return ssl context
     * @throws SSLContextProviderException if there are some problems on getting ssl context
     */
    public SSLContext get() throws SSLContextProviderException {
        if (sslContext == null) {
            try {
                KeyStore ks = KeyStore.getInstance(certType);
                InputStream inputStream = IOC.resolve(Keys.getOrAdd(FileInputStream.class.getCanonicalName()), certPath);
                ks.load(inputStream, storePass.toCharArray());

                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, keyPass.toCharArray());

                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(kmf.getKeyManagers(), null, null);
            } catch (Exception e) {
                throw new SSLContextProviderException(e);
            }
        }
        return sslContext;
    }


}