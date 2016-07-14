package info.smart_tools.smartactors.plugin.create_user;

import info.smart_tools.smartactors.actors.create_user.CreateUserActor;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.*;

@PrepareForTest({IOC.class, CreateUserPlugin.class})
@RunWith(PowerMockRunner.class)
public class CreateUserPluginTest {
    private IPlugin plugin;
    private IBootstrap bootstrap;

    private IKey createUserKey;

    @org.junit.Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(IOC.class);

        bootstrap = mock(IBootstrap.class);
        IKey key = mock(IKey.class);
        createUserKey = mock(IKey.class);
        when(IOC.getKeyForKeyStorage()).thenReturn(key);
        when(IOC.resolve(eq(key), eq(CreateUserActor.class.toString()))).thenReturn(createUserKey);

        plugin = new CreateUserPlugin(bootstrap);
    }

    @Test
    public void Should_CorrectLoadPlugin() throws Exception {
        BootstrapItem item = mock(BootstrapItem.class);
        PowerMockito.whenNew(BootstrapItem.class).withAnyArguments().thenReturn(item);

        CreateUserActor actor = mock(CreateUserActor.class);
        PowerMockito.whenNew(CreateUserActor.class).withArguments(any()).thenReturn(actor);

        plugin.load();

        verify(bootstrap).add(eq(item));
    }

}