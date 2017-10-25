package info.smart_tools.smartactors.endpoint_components_netty.ssl_channel_initialization_handler;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.io.File;
import java.util.List;

public class ServerSSLEngineResolutionStrategy implements IResolveDependencyStrategy {
    private final IFieldName serverCertificateFN, serverCertificateKeyFN, ciphersFN;

    /**
     * The constructor.
     *
     * @throws ResolutionException if error occurs resolving any dependencies
     */
    public ServerSSLEngineResolutionStrategy() throws ResolutionException {
        serverCertificateFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "serverCertificate");
        serverCertificateKeyFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "serverCertificateKey");
        ciphersFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "ciphers");
    }

    @Override
    public <T> T resolve(Object... args) throws ResolveDependencyStrategyException {
        IObject handlerConf = (IObject) args[0];
        IObject endpointConf = (IObject) args[1];

        try {
            File certFile = new File((String) endpointConf.getValue(serverCertificateFN));
            File keyFile = new File((String) endpointConf.getValue(serverCertificateKeyFN));
            List ciphers = (List) handlerConf.getValue(ciphersFN);

            SSLEngine engine = SslContextBuilder
                    .forServer(certFile, keyFile)
                    .ciphers(ciphers)
                    .build()
                    .newEngine(ByteBufAllocator.DEFAULT);

            return (T) engine;
        } catch (ReadValueException | InvalidArgumentException | SSLException e) {
            throw new ResolveDependencyStrategyException(e);
        }
    }
}
