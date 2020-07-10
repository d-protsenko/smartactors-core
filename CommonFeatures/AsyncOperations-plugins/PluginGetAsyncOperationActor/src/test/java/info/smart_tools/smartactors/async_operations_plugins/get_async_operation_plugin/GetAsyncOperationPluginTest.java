package info.smart_tools.smartactors.async_operations_plugins.get_async_operation_plugin;

import info.smart_tools.smartactors.async_operations.get_async_operation.GetAsyncOperationActor;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IActionNoArgs;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({IOC.class, Keys.class, GetAsyncOperationPlugin.class})
@RunWith(PowerMockRunner.class)
public class GetAsyncOperationPluginTest {

    private GetAsyncOperationPlugin plugin;
    private IBootstrap bootstrap;

    @Before
    public void setUp() throws Exception {

        mockStatic(IOC.class);
        mockStatic(Keys.class);
        mockStatic(GetAsyncOperationActor.class);

        IKey key = mock(IKey.class);
        IKey keyPlugin = mock(IKey.class);
        when(IOC.getKeyForKeyByNameStrategy()).thenReturn(key);
        when(IOC.resolve(eq(key), eq("GetFormActorPlugin"))).thenReturn(keyPlugin);

        bootstrap = mock(IBootstrap.class);
        plugin = new GetAsyncOperationPlugin(bootstrap);
    }
    @Test
    public void ShouldCorrectLoadPlugin() throws Exception {
        IKey actorKey = mock(IKey.class);
        when(Keys.getKeyByName(eq(GetAsyncOperationActor.class.getCanonicalName()))).thenReturn(actorKey);

        BootstrapItem bootstrapItem = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments("CreateGetAsyncOperationActor").thenReturn(bootstrapItem);
        when(bootstrapItem.after(anyString())).thenReturn(bootstrapItem);
        when(bootstrapItem.before(anyString())).thenReturn(bootstrapItem);

        plugin.load();

        verifyNew(BootstrapItem.class).withArguments("CreateGetAsyncOperationActor");

        ArgumentCaptor<IActionNoArgs> actionArgumentCaptor = ArgumentCaptor.forClass(IActionNoArgs.class);
        verify(bootstrapItem).process(actionArgumentCaptor.capture());

        ArgumentCaptor<ApplyFunctionToArgumentsStrategy> createNewInstanceStrategyArgumentCaptor =
                ArgumentCaptor.forClass(ApplyFunctionToArgumentsStrategy.class);
        actionArgumentCaptor.getValue().execute();

        verifyStatic();
        IOC.register(eq(actorKey), createNewInstanceStrategyArgumentCaptor.capture());

        GetAsyncOperationActor actor = mock(GetAsyncOperationActor.class);
        whenNew(GetAsyncOperationActor.class).withAnyArguments().thenReturn(actor);

        verify(bootstrap).add(bootstrapItem);
    }

    @Test(expected = PluginException.class)
    public void ShouldThrowPluginException_When_BootstrapItemThrowsException() throws Exception {
        whenNew(BootstrapItem.class).withArguments("GetFormActorPlugin").thenThrow(new InvalidArgumentException(""));
        plugin.load();
    }
}