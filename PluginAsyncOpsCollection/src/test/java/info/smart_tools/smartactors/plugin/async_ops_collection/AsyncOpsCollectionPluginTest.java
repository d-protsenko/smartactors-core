package info.smart_tools.smartactors.plugin.async_ops_collection;

import info.smart_tools.smartactors.core.async_operation_collection.AsyncOperationCollection;
import info.smart_tools.smartactors.core.async_operation_collection.IAsyncOperationCollection;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.base.interfaces.iaction.IPoorAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_by_composite_name_ioc_with_lambda_strategy.ResolveByCompositeNameIOCStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({IOC.class, Keys.class, AsyncOpsCollectionPlugin.class, ResolveByCompositeNameIOCStrategy.class})
@RunWith(PowerMockRunner.class)
public class AsyncOpsCollectionPluginTest {

    private AsyncOpsCollectionPlugin testPlugin;
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    @Before
    public void before() {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        bootstrap = mock(IBootstrap.class);

        testPlugin = new AsyncOpsCollectionPlugin(bootstrap);
    }

    @Test
    public void MustCorrectLoad() throws Exception {
        BootstrapItem item = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments("AsyncOpsCollectionPlugin").thenReturn(item);

        when(item.after(any(String.class))).thenReturn(item);
        when(item.before(any(String.class))).thenReturn(item);
        when(item.process(any(IPoorAction.class))).thenReturn(item);

        ArgumentCaptor<IPoorAction> actionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);

        testPlugin.load();

        verifyNew(BootstrapItem.class).withArguments("AsyncOpsCollectionPlugin");

        verify(item).after("IOC");
        verify(item).before("starter");
        verify(item).process(actionArgumentCaptor.capture());

        verify(bootstrap).add(item);

        //------testing IPoorAction
        IKey asyncCollectionKey = mock(IKey.class);
        when(Keys.getOrAdd(IAsyncOperationCollection.class.getCanonicalName())).thenReturn(asyncCollectionKey);

        ArgumentCaptor<IResolveDependencyStrategy> resolveDependencyStrategyArgumentCaptor =
                ArgumentCaptor.forClass(IResolveDependencyStrategy.class);

        actionArgumentCaptor.getValue().execute();

        verifyStatic();
        Keys.getOrAdd(IAsyncOperationCollection.class.getCanonicalName());

        verifyStatic();
        IOC.register(eq(asyncCollectionKey), resolveDependencyStrategyArgumentCaptor.capture());

        //------testing dependency strategy
        String collectionName = "exampleCN";

        IKey connectionOptionsKey = mock(IKey.class);
        when(Keys.getOrAdd("PostgresConnectionOptions")).thenReturn(connectionOptionsKey);
        ConnectionOptions connectionOptions = mock(ConnectionOptions.class);
        when(IOC.resolve(connectionOptionsKey)).thenReturn(connectionOptions);

        IKey connectionPoolKey = mock(IKey.class);
        when(Keys.getOrAdd("PostgresConnectionPool")).thenReturn(connectionPoolKey);
        IPool connectionPool = mock(IPool.class);
        when(IOC.resolve(connectionPoolKey, connectionOptions)).thenReturn(connectionPool);

        AsyncOperationCollection collection = mock(AsyncOperationCollection.class);
        whenNew(AsyncOperationCollection.class).withArguments(connectionPool, collectionName).thenReturn(collection);

        assertTrue(resolveDependencyStrategyArgumentCaptor.getValue().resolve(collectionName) == collection);

        verifyStatic();
        Keys.getOrAdd("PostgresConnectionOptions");

        verifyStatic();
        IOC.resolve(connectionOptionsKey);

        verifyStatic();
        Keys.getOrAdd("PostgresConnectionPool");

        verifyStatic();
        IOC.resolve(connectionPoolKey, connectionOptions);

        verifyNew(AsyncOperationCollection.class).withArguments(connectionPool, collectionName);
    }



    @Test
    public void MustInCorrectLoadWhenNewBootstrapItemThrowException() throws Exception {
        whenNew(BootstrapItem.class).withArguments("AsyncOpsCollectionPlugin").thenThrow(new InvalidArgumentException(""));
        try {
            testPlugin.load();
        } catch (PluginException e) {
            verifyNew(BootstrapItem.class).withArguments("AsyncOpsCollectionPlugin");
            return;
        }
        fail("Must catch exception");
    }



    @Test
    public void MustInCorrectExecuteActionWhenKeysGetOrAddThrowException() throws Exception {
        BootstrapItem item = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments("AsyncOpsCollectionPlugin").thenReturn(item);

        when(item.after(any(String.class))).thenReturn(item);
        when(item.before(any(String.class))).thenReturn(item);
        when(item.process(any(IPoorAction.class))).thenReturn(item);

        ArgumentCaptor<IPoorAction> actionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);

        testPlugin.load();

        verifyNew(BootstrapItem.class).withArguments("AsyncOpsCollectionPlugin");

        verify(item).after("IOC");
        verify(item).before("starter");
        verify(item).process(actionArgumentCaptor.capture());

        verify(bootstrap).add(item);

        //------testing IPoorAction
        when(Keys.getOrAdd(IAsyncOperationCollection.class.getCanonicalName())).thenThrow(new ResolutionException(""));

        try {
            actionArgumentCaptor.getValue().execute();
        } catch (ActionExecuteException e) {
            verifyStatic();
            Keys.getOrAdd(IAsyncOperationCollection.class.getCanonicalName());
            return;
        }
        fail("Must catch exception");
    }

}