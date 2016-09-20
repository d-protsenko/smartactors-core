package info.smart_tools.smartactors.plugin.base.bootstrap_plugin;

import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test for {@link BootstrapPlugin}.
 */
public class BootstrapPluginTest {
    private IBootstrap bootstrapMock;
    private ArgumentCaptor<IBootstrapItem> itemArgumentCaptor;

    @Before
    public void setUp()
            throws Exception {
        bootstrapMock = mock(IBootstrap.class);
        itemArgumentCaptor = ArgumentCaptor.forClass(IBootstrapItem.class);
    }

    @Test(expected = PluginException.class)
    public void Should_throwWhenPluginContainsInvalidMethods()
            throws Exception {
        IPlugin plugin = new BootstrapPlugin(bootstrapMock) {
            @BootstrapPlugin.Item("wrongItem")
            public void wrongItemMethod(int x) {}
        };

        plugin.load();
    }

    @Test
    public void Should_createItemsForAnnotatedMethods()
            throws Exception {
        IPlugin plugin = new BootstrapPlugin(bootstrapMock) {
            @BootstrapPlugin.Item("item1")
            public void item1Method() {
            }

            @BootstrapPlugin.Item("item2")
            public void item2Method() {
            }

            public void notItemMethod() {}
        };

        plugin.load();

        verify(bootstrapMock, times(2)).add(itemArgumentCaptor.capture());

        for (IBootstrapItem item : itemArgumentCaptor.getAllValues()) {
            assertTrue(item instanceof MethodBootstrapItem);
        }
    }
}
