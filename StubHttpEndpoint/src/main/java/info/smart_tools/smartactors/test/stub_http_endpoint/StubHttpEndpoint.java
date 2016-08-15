package info.smart_tools.smartactors.test.stub_http_endpoint;

import info.smart_tools.smartactors.core.iaction.IFunction;
import info.smart_tools.smartactors.core.iasync_service.IAsyncService;
import info.smart_tools.smartactors.core.ichannel_handler.IChannelHandler;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;

import java.util.ArrayList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by sevenbits on 8/12/16.
 */
public class StubHttpEndpoint implements IAsyncService {

    private BlockingDeque<IObject> queue;
    private IScope scope;
    private IEnvironmentHandler handler;
    private Long timeInterval;
    private IReceiverChain chain;
    private Future future;
    private IFunction<IObject, IObject> rule;
    private ExecutorService executorService;
    private Boolean isError;

    public StubHttpEndpoint(
            final BlockingDeque<IObject> sourceQueue,
            final IScope testScope,
            final IEnvironmentHandler testHandler,
            final Long timeBetweenTests,
            final IReceiverChain routerChain,
            final IFunction<IObject, IObject> transformationRule
    ) {
        this.rule = transformationRule;
        if (null == transformationRule) {
            this.rule = defaultTransformationRule();
        }
        this.queue = sourceQueue;
        this.scope = testScope;
        this.handler = testHandler;
        this.timeInterval = timeBetweenTests;
        this.chain = routerChain;
        this.executorService = Executors.newSingleThreadExecutor();
        this.future = executorService.submit(() -> {
            try {
                ScopeProvider.setCurrentScope(this.scope);
                while (true) {
                    try {
                        IObject obj = this.rule.execute(queue.take());
                        handler.handle(obj, chain);
                        Thread.sleep(timeInterval);
                    } catch (Throwable e) {
                        e.getCause().printStackTrace();
                    }
                }
            } catch (Throwable e) {
                throw new RuntimeException("ScopeProvider not initialized.");
            }
        });
    }

    @Override
    public CompletableFuture<StubHttpEndpoint> start() {
        return CompletableFuture.runAsync(() -> {
            try {
                this.future.get();
            } catch (InterruptedException | ExecutionException e) {
                //Thread.currentThread().interrupt();
                e.getCause().printStackTrace();
            }
        }).thenApply(a -> StubHttpEndpoint.this);
    }

    @Override
    public CompletableFuture stop() {
        return CompletableFuture.runAsync(() -> this.future.cancel(false)).thenApply(a -> StubHttpEndpoint.this);
    }

    private IFunction<IObject, IObject> defaultTransformationRule() {
        return (iObject) -> {
            try {
                IFieldName messageFieldName = IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "message"
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
                IObject environment = IOC.resolve(
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
                //create environment
                environment.setValue(messageFieldName, message);
                environment.setValue(contextFieldName, context);

                return environment;
            } catch (ChangeValueException | ResolutionException | ReadValueException e) {
                e.getCause().printStackTrace();
                return null;
            }
        };
    }
}