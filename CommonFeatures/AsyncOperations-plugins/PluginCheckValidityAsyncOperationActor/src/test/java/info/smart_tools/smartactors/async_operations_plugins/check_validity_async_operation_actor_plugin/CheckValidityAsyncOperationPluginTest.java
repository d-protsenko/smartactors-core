package info.smart_tools.smartactors.async_operations_plugins.check_validity_async_operation_actor_plugin;

import info.smart_tools.smartactors.async_operations.check_validity_async_operation.CheckValidityAsyncOperationActor;
import info.smart_tools.smartactors.base.interfaces.iaction.IPoorAction;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PrepareForTest({IOC.class, CheckValidityAsyncOperationPlugin.class})
@RunWith(PowerMockRunner.class)
public class CheckValidityAsyncOperationPluginTest {

    private CheckValidityAsyncOperationPlugin plugin;
    private IBootstrap bootstrap;

    private IKey operationKey;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(IOC.class);

        IKey key = Mockito.mock(IKey.class);
        operationKey = Mockito.mock(IKey.class);
        when(IOC.getKeyForKeyByNameResolveStrategy()).thenReturn(key);
        PowerMockito.when(Keys.getOrAdd(CheckValidityAsyncOperationActor.class.getCanonicalName())).thenReturn(operationKey);

        bootstrap = Mockito.mock(IBootstrap.class);
        plugin = new CheckValidityAsyncOperationPlugin(bootstrap);
    }

    @Test
    public void ShouldAddNewItemDuringLoad() throws Exception {
        BootstrapItem bootstrapItem = Mockito.mock(BootstrapItem.class);
        PowerMockito.whenNew(BootstrapItem.class).withAnyArguments().thenReturn(bootstrapItem);
        when(bootstrapItem.after(anyString())).thenReturn(bootstrapItem);
        when(bootstrapItem.before(anyString())).thenReturn(bootstrapItem);

        CheckValidityAsyncOperationActor actor = Mockito.mock(CheckValidityAsyncOperationActor.class);
        PowerMockito.whenNew(CheckValidityAsyncOperationActor.class).withAnyArguments().thenReturn(actor);

        plugin.load();

        PowerMockito.verifyNew(BootstrapItem.class).withArguments("CreateCheckValidityAsyncOperationActor");

        ArgumentCaptor<IPoorAction> actionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);
        verify(bootstrapItem).process(actionArgumentCaptor.capture());

        ArgumentCaptor<ApplyFunctionToArgumentsStrategy> createNewInstanceStrategyArgumentCaptor =
                ArgumentCaptor.forClass(ApplyFunctionToArgumentsStrategy.class);
        actionArgumentCaptor.getValue().execute();

        PowerMockito.verifyStatic();
        IOC.register(Mockito.eq(operationKey), createNewInstanceStrategyArgumentCaptor.capture());

        verify(bootstrap).add(Mockito.any());
    }
}