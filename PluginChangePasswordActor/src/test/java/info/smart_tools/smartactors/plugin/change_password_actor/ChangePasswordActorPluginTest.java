package info.smart_tools.smartactors.plugin.change_password_actor;

import info.smart_tools.smartactors.actor.change_password.ChangePasswordActor;
import info.smart_tools.smartactors.actor.change_password.wrapper.ChangePasswordConfig;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.iaction.IPoorAction;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@PrepareForTest({IOC.class, Keys.class, ChangePasswordActorPlugin.class, ApplyFunctionToArgumentsStrategy.class})
@RunWith(PowerMockRunner.class)
public class ChangePasswordActorPluginTest {

    private ChangePasswordActorPlugin plugin;
    private IBootstrap bootstrap;

    @Before
    public void setUp() throws Exception {

        mockStatic(IOC.class);
        mockStatic(Keys.class);

        bootstrap = mock(IBootstrap.class);
        plugin = new ChangePasswordActorPlugin(bootstrap);
    }


    @Test
    public void ShouldCorrectLoadPlugin() throws Exception {

        BootstrapItem bootstrapItem = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments("ChangePasswordActorPlugin").thenReturn(bootstrapItem);

        when(bootstrapItem.after(anyString())).thenReturn(bootstrapItem);
        when(bootstrapItem.before(anyString())).thenReturn(bootstrapItem);

        plugin.load();

        verifyNew(BootstrapItem.class).withArguments("ChangePasswordActorPlugin");

        ArgumentCaptor<IPoorAction> actionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);

        verify(bootstrapItem).after("IOC");
        verify(bootstrapItem).before("starter");
        verify(bootstrapItem).process(actionArgumentCaptor.capture());
        verify(bootstrap).add(bootstrapItem);

        IKey changePasswordActorKey = mock(IKey.class);
        when(Keys.getOrAdd(ChangePasswordActor.class.getCanonicalName())).thenReturn(changePasswordActorKey);

        actionArgumentCaptor.getValue().execute();

        verifyStatic();
        Keys.getOrAdd(ChangePasswordConfig.class.getCanonicalName());

        verifyStatic();
        Keys.getOrAdd(ChangePasswordActor.class.getCanonicalName());

        ArgumentCaptor<ApplyFunctionToArgumentsStrategy> argumentCaptor = ArgumentCaptor.forClass(ApplyFunctionToArgumentsStrategy.class);

        verifyStatic();
        IOC.register(eq(changePasswordActorKey), argumentCaptor.capture());

        IObject configObj = mock(IObject.class);
        IKey configKey = mock(IKey.class);
        when(Keys.getOrAdd(ChangePasswordConfig.class.getCanonicalName())).thenReturn(configKey);
        ChangePasswordConfig config = mock(ChangePasswordConfig.class);
        when(IOC.resolve(configKey, configObj)).thenReturn(config);

        ChangePasswordActor actor = mock(ChangePasswordActor.class);
        whenNew(ChangePasswordActor.class).withArguments(config).thenReturn(actor);

        argumentCaptor.getValue().resolve(configObj);

        verifyNew(ChangePasswordActor.class);
    }

    @Test(expected = PluginException.class)
    public void ShouldThrowException_When_InternalExceptionIsThrown() throws Exception {

        whenNew(BootstrapItem.class).withArguments("ChangePasswordActorPlugin").thenThrow(new InvalidArgumentException(""));
        plugin.load();
    }
}
