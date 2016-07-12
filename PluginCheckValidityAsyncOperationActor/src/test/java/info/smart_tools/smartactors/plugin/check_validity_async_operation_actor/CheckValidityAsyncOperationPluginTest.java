package info.smart_tools.smartactors.plugin.check_validity_async_operation_actor;

import info.smart_tools.smartactors.core.actors.check_validity_async_operation.CheckValidityAsyncOperationActor;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;

@PrepareForTest({IOC.class, CheckValidityAsyncOperationActor.class})
@RunWith(PowerMockRunner.class)
public class CheckValidityAsyncOperationPluginTest {

    private CheckValidityAsyncOperationPlugin plugin;
    private IBootstrap bootstrap;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(IOC.class);

        IKey key = Mockito.mock(IKey.class);
        IKey operationKey = Mockito.mock(IKey.class);
        Mockito.when(IOC.getKeyForKeyStorage()).thenReturn(key);
        PowerMockito.when(Keys.getOrAdd(CheckValidityAsyncOperationActor.class.toString())).thenReturn(operationKey);

        bootstrap = Mockito.mock(IBootstrap.class);
        plugin = new CheckValidityAsyncOperationPlugin(bootstrap);
    }

    @Test
    public void ShouldAddNewItemDuringLoad() throws Exception {
        BootstrapItem item = Mockito.mock(BootstrapItem.class);
        PowerMockito.whenNew(BootstrapItem.class).withArguments("CreateCheckValidityAsyncOperationActor").thenReturn(item);
        plugin.load();
        //TODO:: Cannot mock Bootstrap item
        //PowerMockito.verifyNew(BootstrapItem.class).withArguments(Mockito.eq("CreateCheckValidityAsyncOperationActor"));
        Mockito.verify(bootstrap).add(Mockito.any());
    }
}