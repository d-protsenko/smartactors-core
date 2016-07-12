package info.smart_tools.smartactors.plugin.create_session;

import info.smart_tools.smartactors.actors.create_session.CreateSessionActor;
import info.smart_tools.smartactors.actors.create_session.wrapper.CreateSessionConfig;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import jdk.nashorn.internal.runtime.linker.Bootstrap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;

@PrepareForTest({IOC.class, Keys.class, CreateSessionActor.class})
@RunWith(PowerMockRunner.class)
public class CreateSessionPluginTest {

    private CreateSessionPlugin plugin;
    private IBootstrap bootstrap;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(IOC.class);
        PowerMockito.mockStatic(Keys.class);

        IKey key = Mockito.mock(IKey.class);
        Mockito.when(IOC.getKeyForKeyStorage()).thenReturn(key);

        IKey configKey = Mockito.mock(IKey.class);
        CreateSessionConfig config = Mockito.mock(CreateSessionConfig.class);
        Mockito.when(IOC.resolve(Mockito.eq(key), Mockito.eq(IPool.class.toString()))).thenReturn(configKey);
        Mockito.when(IOC.resolve(Mockito.eq(configKey), Mockito.any())).thenReturn(config);

        Mockito.when(Keys.getOrAdd(Mockito.eq(CreateSessionActor.class.toString()))).thenReturn(Mockito.mock(IKey.class));

        bootstrap = Mockito.mock(IBootstrap.class);
        plugin = new CreateSessionPlugin(bootstrap);
    }

    @Test
    public void ShouldAddNewItemDuringLoad() throws Exception {
        BootstrapItem bootstrapItem = Mockito.mock(BootstrapItem.class);
        PowerMockito.whenNew(BootstrapItem.class).withArguments("CreateSessionActorPlugin").thenReturn(bootstrapItem);
        plugin.load();
        //баг: method not invoke
        PowerMockito.verifyNew(BootstrapItem.class).withArguments("CreateSessionActorPlugin");
        Mockito.verify(bootstrap).add(Mockito.eq(bootstrapItem));
    }
}