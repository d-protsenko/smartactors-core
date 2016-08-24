package info.smart_tools.smartactors.test.test_http_endpoint;

import info.smart_tools.smartactors.core.iaction.IFunction;
import info.smart_tools.smartactors.core.iasync_service.IAsyncService;
import info.smart_tools.smartactors.core.ichannel_handler.IChannelHandler;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.test.isource.ISource;

import java.util.ArrayList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Implementation of {@link IAsyncService}.
 * Imitates work of Http Endpoint and uses {@link ISource} as data source
 * as data source.
 */
public class TestHttpEndpoint implements IAsyncService {

    private ISource dataSource;
    private IScope scope;
    private IEnvironmentHandler handler;
    private Long timeInterval;
    private IReceiverChain chain;
    private IFunction<IObject, IObject> rule;
    private ExecutorService executorService;

    /**
     * Creates instance of {@link TestHttpEndpoint}.
     * @param source the instance of {@link ISource}
     * @param testScope the test scope
     * @param testHandler the handler for processing test message
     * @param timeBetweenTests the interval between starting processing next message
     * @param routerChain the chain for processing message
     * @param transformationRule the rule for transform incoming message to FullHttpRequest.
     *          If {@code null} the default strategy will be used.
     * @throws InvalidArgumentException if one of the arguments is incorrect
     */
    public TestHttpEndpoint(
            final ISource source,
            final IScope testScope,
            final IEnvironmentHandler testHandler,
            final Long timeBetweenTests,
            final IReceiverChain routerChain,
            final IFunction<IObject, IObject> transformationRule
    ) throws InvalidArgumentException {
        if (null == source) {
            throw new InvalidArgumentException("The source should not be null.");
        }
        if (null == testScope) {
            throw new InvalidArgumentException("The scope should not be null.");
        }
        if (null == testHandler) {
            throw new InvalidArgumentException("The handler should not be null.");
        }
        if (null == routerChain) {
            throw new InvalidArgumentException("The message processing chain should not be null.");
        }
        this.rule = transformationRule;
        if (null == transformationRule) {
            this.rule = defaultTransformationRule();
        }
        this.dataSource = source;
        this.scope = testScope;
        this.handler = testHandler;
        this.timeInterval = timeBetweenTests;
        this.chain = routerChain;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public CompletableFuture start() {
        return CompletableFuture.supplyAsync(this::execute, this.executorService);
    }

    @Override
    public CompletableFuture stop() {
        return CompletableFuture.runAsync(() -> this.executorService.shutdown()).thenApply(a -> TestHttpEndpoint.this);
    }

    private Future<TestHttpEndpoint> execute() {
        try {
            while (true) {
                ScopeProvider.setCurrentScope(this.scope);
                try {
                    IObject obj = this.rule.execute((IObject) dataSource.next());
                    if (obj != null) {
                        handler.handle(obj, chain, null);
                    }
                    Thread.sleep(timeInterval);
                } catch (Throwable e) {
                    System.out.println(e.toString());
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException("ScopeProvider is not initialized.");
        }
    }

    private IFunction<IObject, IObject> defaultTransformationRule() {
        return (iObject) -> {
            try {
                IFieldName messageFieldName = IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "message"
                );
                IFieldName idFieldName = IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "id"
                );
                IFieldName testResultFieldName = IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "testResult"
                );
                IFieldName contextFieldName = IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "context"
                );
                IFieldName requestFieldName = IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "request"
                );
                IFieldName channelFieldName = IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "channel"
                );
                IFieldName headersFieldName = IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "headers"
                );
                IFieldName cookiesFieldName = IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "cookies"
                );

                IObject message = (IObject) iObject.getValue(messageFieldName);
                Object id =  iObject.getValue(idFieldName);
                IObject environment = IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.getCanonicalName())
                );
                IObject testResult = IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.getCanonicalName())
                );
                IObject context = IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.getCanonicalName())
                );

                IChannelHandler channelHandler = IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), TestChannelHandler.class.getCanonicalName()));
                context.setValue(channelFieldName, channelHandler);

                context.setValue(cookiesFieldName, new ArrayList<IObject>());
                context.setValue(headersFieldName, new ArrayList<IObject>());
                TestFullHttpRequest request = new TestFullHttpRequest((IObject) iObject.getValue(requestFieldName));
                context.setValue(requestFieldName, request);

                environment.setValue(messageFieldName, message);
                environment.setValue(contextFieldName, context);
                environment.setValue(idFieldName, id);
                environment.setValue(testResultFieldName, testResult);

                return environment;
            } catch (ChangeValueException | ResolutionException | ReadValueException e) {
                e.getCause().printStackTrace();
                return null;
            }
        };
    }
}