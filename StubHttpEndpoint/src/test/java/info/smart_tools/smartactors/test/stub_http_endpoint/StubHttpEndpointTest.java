package info.smart_tools.smartactors.test.stub_http_endpoint;

import info.smart_tools.smartactors.core.iaction.IFunction;
import info.smart_tools.smartactors.core.iasync_service.IAsyncService;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link StubHttpEndpoint}.
 */
public class StubHttpEndpointTest {

    private IStrategyContainer container = new StrategyContainer();
    private IReceiverChain chain = mock(IReceiverChain.class);
    private BlockingDeque<IObject> queue = new LinkedBlockingDeque<>();
    private IEnvironmentHandler handler = mock(IEnvironmentHandler.class);

    @Before
    public void init()
            throws Exception {
        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(keyOfMainScope);
        scope.setValue(IOC.getIocKey(), this.container);
        ScopeProvider.setCurrentScope(scope);

        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return new Key((String) a[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );
    }

    @Test
    public void checkExecution()
            throws Exception {
        doThrow(RuntimeException.class).when(this.handler).handle(null, this.chain);
        System.out.println(Thread.currentThread().getName());
        IObject a = mock(IObject.class);
        IObject b = mock(IObject.class);
        IObject c = mock(IObject.class);
        IObject d = mock(IObject.class);
        IObject e = mock(IObject.class);
        IObject f = mock(IObject.class);
        IObject g = mock(IObject.class);
        IObject h = mock(IObject.class);
        IObject i = mock(IObject.class);
        IObject j = mock(IObject.class);
        queue.put(a);
        queue.put(b);
        queue.put(c);
        queue.put(d);
        queue.put(e);
        queue.put(f);
        queue.put(g);
        queue.put(h);
        queue.put(i);
        queue.put(j);
        queue.put(a);
        IAsyncService endpoint = new StubHttpEndpoint(this.queue, ScopeProvider.getCurrentScope(), this.handler, 100L, chain, null);
        try {
            endpoint.start();
        } catch (Throwable er) {
            throw new Exception("Test Endpoint execution failed.", er);
        }

        Thread.sleep(1000000);
    }
}
