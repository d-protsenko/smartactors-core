package info.smart_tools.smartactors.plugin.actor_collection_receiver;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        verify(bootstrap, times(1)).add(any(IBootstrapItem.class));
    }

    @Test (expected = InvalidArgumentException.class)
    public void CheckInvalidArgumentExceptionOnCreationWithEmptyBootstrap()
            throws Exception {
        new ActorCollectionReceiverPlugin(null);
        fail();
    }

    @Test (expected = PluginException.class)
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
        final IBootstrapItem[] items = new IBootstrapItem[1];
        IBootstrap<IBootstrapItem<String>> bootstrap = mock(IBootstrap.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                items[0] = (BootstrapItem)invocationOnMock.getArguments()[0];
                return null;
            }
        }).when(bootstrap).add(any(IBootstrapItem.class));
        IPlugin plugin = new ActorCollectionReceiverPlugin(bootstrap);
        plugin.load();
        items[0].executeProcess();
        IMessageReceiver receiver = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), "ActorCollection"));
        assertNotNull(receiver);
    }

    private void registerKeyStorage()
            throws Exception {
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

    private void registerBrokenKeyStorage()
            throws Exception {
        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy((a) -> null)
        );
    }

    private void registerIFieldNameStrategy()
            throws Exception {
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
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
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                new ResolveByNameIocStrategy((a) -> null)
        );
    }
}
