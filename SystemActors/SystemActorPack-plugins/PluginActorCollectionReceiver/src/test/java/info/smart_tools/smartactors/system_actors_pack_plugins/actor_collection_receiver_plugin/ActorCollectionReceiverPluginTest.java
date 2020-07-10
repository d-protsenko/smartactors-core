package info.smart_tools.smartactors.system_actors_pack_plugins.actor_collection_receiver_plugin;

import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.ioc.string_ioc_key.Key;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link ActorCollectionReceiverPlugin}
 */
public class ActorCollectionReceiverPluginTest {

    @Before
    public void init()
            throws Exception {
        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(keyOfMainScope);
        scope.setValue(IOC.getIocKey(), new StrategyContainer());
        ScopeProvider.setCurrentScope(scope);
    }

    @Test
    public void checkCreationAndExecutionLoad()
            throws Exception {
        IBootstrap<IBootstrapItem<String>> bootstrap = mock(IBootstrap.class);
        doNothing().when(bootstrap).add(any(IBootstrapItem.class));
        IPlugin plugin = new ActorCollectionReceiverPlugin(bootstrap);
        assertNotNull(plugin);
        plugin.load();
        verify(bootstrap, times(5)).add(any(IBootstrapItem.class));
    }

    @Test (expected = AssertionError.class)
    public void CheckInvalidArgumentExceptionOnCreationWithEmptyBootstrap()
            throws Exception {
        new ActorCollectionReceiverPlugin(null);
        fail();
    }

    @Test (expected = Exception.class)
    public void checkPluginExceptionOnExecuteLoadMethodWithBrokenBootstrap()
            throws Exception {
        IBootstrap<IBootstrapItem<String>> bootstrap = mock(IBootstrap.class);
        doThrow(Exception.class).when(bootstrap).add(any(IBootstrapItem.class));
        IPlugin plugin = new ActorCollectionReceiverPlugin(bootstrap);
        plugin.load();
        fail();
    }

    @Test
    public void checkPluginProcessExecution() throws Exception {
        registerKeyStorage();
        registerIFieldNameStrategy();
        final List<IBootstrapItem> items = new ArrayList<>();
        IBootstrap<IBootstrapItem<String>> bootstrap = mock(IBootstrap.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                items.add((BootstrapItem)invocationOnMock.getArguments()[0]);
                return null;
            }
        }).when(bootstrap).add(any(IBootstrapItem.class));
        IPlugin plugin = new ActorCollectionReceiverPlugin(bootstrap);
        plugin.load();
        for (IBootstrapItem item : items) {
            if (item.getItemName().equals("ActorCollectionReceiver")) {
                item.executeProcess();
            }
        }
        IMessageReceiver receiver = IOC.resolve(IOC.resolve(IOC.getKeyForKeyByNameStrategy(), "ActorCollection"));
        assertNotNull(receiver);
    }

    private void registerKeyStorage()
            throws Exception {
        IOC.register(
                IOC.getKeyForKeyByNameStrategy(),
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

    private void registerBrokenKeyStorage()
            throws Exception {
        IOC.register(
                IOC.getKeyForKeyByNameStrategy(),
                new ResolveByNameIocStrategy((a) -> null)
        );
    }

    private void registerIFieldNameStrategy()
            throws Exception {
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return new FieldName((String) a[0]);
                            } catch (Throwable e) {
                                throw new RuntimeException("Could not create new instance of FieldName", e);
                            }
                        }
                )
        );
    }

    private void registerBrokenIFieldNameStrategy()
            throws Exception {
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                new ResolveByNameIocStrategy((a) -> null)
        );
    }
}
