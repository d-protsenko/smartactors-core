package info.smart_tools.smartactors.feature_loading_system.bootstrap;

import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.exception.ProcessExecutionException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.exception.RevertProcessExecutionException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for Bootstrap
 */
public class BootstrapTest {

    @Test
    public void checkAdditionalExecutionAndRevertingBootstrapItem()
            throws Exception {
        IBootstrap<IBootstrapItem<String>> bootstrap = new Bootstrap();
        assertNotNull(bootstrap);
        IBootstrapItem item1 = mock(IBootstrapItem.class);
        IBootstrapItem item2 = mock(IBootstrapItem.class);
        IBootstrapItem item3 = mock(IBootstrapItem.class);
        doNothing().when(item1).executeProcess();
        doNothing().when(item1).executeRevertProcess();
        doThrow(ProcessExecutionException.class).doNothing().when(item2).executeProcess();
        doNothing().when(item2).executeRevertProcess();
        doThrow(ProcessExecutionException.class).doThrow(ProcessExecutionException.class).doNothing().when(item3).executeProcess();
        doNothing().when(item3).executeRevertProcess();
        bootstrap.add(item1);
        bootstrap.add(item2);
        bootstrap.add(item3);
        List<IBootstrapItem<String>> items = bootstrap.start();
        verify(item1, times(1)).executeProcess();
        verify(item2, times(2)).executeProcess();
        verify(item2, times(1)).executeRevertProcess();
        verify(item3, times(3)).executeProcess();
        verify(item3, times(2)).executeRevertProcess();
        bootstrap.revert();
        verify(item1, times(1)).executeRevertProcess();
        IBootstrap<IBootstrapItem<String>> nextBootstrap = new Bootstrap(items);
        verify(item1, times(1)).executeProcess();
    }

    @Test (expected = ProcessExecutionException.class)
    public void checkDeadlockBootstrapItem()
            throws Exception {
        IBootstrap<IBootstrapItem<String>> bootstrap = new Bootstrap();
        assertNotNull(bootstrap);
        IBootstrapItem item1 = mock(IBootstrapItem.class);
        IBootstrapItem item2 = mock(IBootstrapItem.class);
        doNothing().when(item1).executeProcess();
        doNothing().when(item1).executeRevertProcess();
        doThrow(ProcessExecutionException.class).when(item2).executeProcess();
        doNothing().when(item2).executeRevertProcess();
        bootstrap.add(item1);
        bootstrap.add(item2);
        List<IBootstrapItem<String>> items = bootstrap.start();
        verify(item1, times(1)).executeProcess();
        verify(item2, times(2)).executeProcess();
        verify(item2, times(2)).executeRevertProcess();
        fail();
    }

    @Test (expected = ProcessExecutionException.class)
    public void checkProcessExecutionException()
            throws Exception {
        IBootstrap<IBootstrapItem<String>> bootstrap = new Bootstrap();
        IBootstrapItem item = mock(IBootstrapItem.class);
        bootstrap.add(item);
        doThrow(Exception.class).when(item).executeProcess();
        doThrow(Exception.class).when(item).executeRevertProcess();
        bootstrap.start();
        fail();
    }

    @Test (expected = RevertProcessExecutionException.class)
    public void checkRevertProcessExecutionException()
            throws Exception {
        IBootstrap<IBootstrapItem<String>> bootstrap = new Bootstrap();
        IBootstrapItem item = mock(IBootstrapItem.class);
        bootstrap.add(item);
        doThrow(RevertProcessExecutionException.class).when(item).executeRevertProcess();
        bootstrap.revert();
        fail();
    }
}
