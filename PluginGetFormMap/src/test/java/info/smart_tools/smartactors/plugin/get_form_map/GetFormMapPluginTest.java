package info.smart_tools.smartactors.plugin.get_form_map;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.ioc.IOC;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@PrepareForTest({IOC.class, GetFormMapPlugin.class})
@RunWith(PowerMockRunner.class)
public class GetFormMapPluginTest {
    private GetFormMapPlugin plugin;
    private IBootstrap bootstrap;

    @Before
    public void setUp() throws ResolutionException {

        mockStatic(IOC.class);

        IKey key1 = mock(IKey.class);
        IKey keyQuery = mock(IKey.class);
        when(IOC.getKeyForKeyStorage()).thenReturn(key1);
        when(IOC.resolve(eq(key1), eq(CompiledQuery.class.toString()))).thenReturn(keyQuery);

        bootstrap = mock(IBootstrap.class);
        plugin = new GetFormMapPlugin(bootstrap);
    }

    @Test
    public void ShouldAddNewItemDuringLoad() throws Exception {

        BootstrapItem bootstrapItem = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments("CreateGetFormMapActorsPlugin").thenReturn(bootstrapItem);
        plugin.load();
        verifyNew(BootstrapItem.class).withArguments("CreateGetFormMapActorsPlugin");
        verify(bootstrap).add(eq(bootstrapItem));
    }
}
