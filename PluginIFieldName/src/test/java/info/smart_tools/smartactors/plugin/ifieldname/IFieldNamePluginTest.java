package info.smart_tools.smartactors.plugin.ifieldname;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({IOC.class, Keys.class, IFieldNamePlugin.class, IFieldName.class})
@RunWith(PowerMockRunner.class)
public class IFieldNamePluginTest {

    private IFieldNamePlugin plugin;
    private IBootstrap bootstrap;

    @Before
    public void setUp() throws Exception {

        mockStatic(IOC.class);
        mockStatic(IKey.class);

        bootstrap = mock(IBootstrap.class);
        plugin = new IFieldNamePlugin(bootstrap);
    }

    @Test
    public void MustCorrectLoadPlugin() throws Exception {

        ConcurrentHashMap<String, IFieldName> fieldNamesMap = mock(ConcurrentHashMap.class);
        whenNew(ConcurrentHashMap.class).withNoArguments().thenReturn(fieldNamesMap);

        BootstrapItem item = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments(("IFieldNamePlugin")).thenReturn(item);

        when(item.after(anyString())).thenReturn(item);
    }

}