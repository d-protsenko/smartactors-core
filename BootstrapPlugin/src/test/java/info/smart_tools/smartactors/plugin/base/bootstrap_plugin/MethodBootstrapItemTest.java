package info.smart_tools.smartactors.plugin.base.bootstrap_plugin;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap_item.exception.ProcessExecutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test for {@link MethodBootstrapItem}.
 */
public class MethodBootstrapItemTest {
    @Test
    public void Should_readAnnotationsAndInvokeMethod()
            throws Exception {
        IAction<IPlugin> actionMock = mock(IAction.class);
        IPlugin plugin = new IPlugin() {
            @Override
            public void load() throws PluginException {
                //
            }

            @BootstrapPlugin.Item("theItem")
            @BootstrapPlugin.After({"aItem1", "aItem2"})
            @BootstrapPlugin.Before({"bItem1", "bItem2"})
            public void methodOfTheItem()
                    throws ActionExecuteException, InvalidArgumentException {
                actionMock.execute(this);
            }
        };

        MethodBootstrapItem item = new MethodBootstrapItem(plugin, plugin.getClass().getMethod("methodOfTheItem"));

        assertEquals("theItem", item.getItemName());
        assertEquals(Arrays.asList("aItem1", "aItem2"), item.getAfterItems());
        assertEquals(Arrays.asList("bItem1", "bItem2"), item.getBeforeItems());

        verify(actionMock, times(0)).execute(same(plugin));
        item.executeProcess();
        verify(actionMock, times(1)).execute(same(plugin));
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_throwWhenMethodHasParameters()
            throws Exception {
        IPlugin plugin = new IPlugin() {
            @Override
            public void load() throws PluginException {
                //
            }

            @BootstrapPlugin.Item("theItem")
            public void methodOfTheItem(Object par1) {
            }
        };

        assertNotNull(new MethodBootstrapItem(plugin, plugin.getClass().getMethod("methodOfTheItem", Object.class)));
    }

    @Test(expected = ProcessExecutionException.class)
    public void Should_wrapExceptionThrownByItemBody()
            throws Exception {
        IPlugin plugin = new IPlugin() {
            @Override
            public void load() throws PluginException {
                //
            }

            @BootstrapPlugin.Item("theItem")
            public void methodOfTheItem() {
                throw new RuntimeException();
            }
        };

        new MethodBootstrapItem(plugin, plugin.getClass().getMethod("methodOfTheItem")).executeProcess();
    }
}
