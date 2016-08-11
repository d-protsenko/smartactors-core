package info.smart_tools.smartactors.plugin.connection_options;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.iaction.IPoorAction;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({IOC.class, Keys.class, ConnectionOptionsPlugin.class, ApplyFunctionToArgumentsStrategy.class})
@RunWith(PowerMockRunner.class)
public class ConnectionOptionsPluginTest {

    private ConnectionOptionsPlugin plugin;
    private IBootstrap bootstrap;

    @Before
    public void setUp() throws Exception {

        mockStatic(IOC.class);
        mockStatic(Keys.class);

        bootstrap = mock(IBootstrap.class);
        plugin = new ConnectionOptionsPlugin(bootstrap);
    }


    @Test
    public void ShouldCorrectLoadPlugin() throws Exception {

        BootstrapItem bootstrapItem = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments("ConnectionOptionsPlugin").thenReturn(bootstrapItem);

        when(bootstrapItem.after(anyString())).thenReturn(bootstrapItem);
        when(bootstrapItem.before(anyString())).thenReturn(bootstrapItem);

        plugin.load();

        verifyNew(BootstrapItem.class).withArguments("ConnectionOptionsPlugin");

        ArgumentCaptor<IPoorAction> actionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);

        verify(bootstrapItem).after("IOC");
        verify(bootstrapItem).before("configure");
        verify(bootstrapItem).process(actionArgumentCaptor.capture());
        verify(bootstrap).add(bootstrapItem);

        IKey connectionOptionsKey = mock(IKey.class);
        when(Keys.getOrAdd("PostgresConnectionOptions")).thenReturn(connectionOptionsKey);

        actionArgumentCaptor.getValue().execute();

        verifyStatic();
        Keys.getOrAdd("PostgresConnectionOptions");

        ArgumentCaptor<ApplyFunctionToArgumentsStrategy> argumentCaptor = ArgumentCaptor.forClass(ApplyFunctionToArgumentsStrategy.class);

        verifyStatic();
        IOC.register(eq(connectionOptionsKey), argumentCaptor.capture());

        IObject configObj = mock(IObject.class);

        Properties connectionOptions = mock(Properties.class);
        whenNew(Properties.class).withNoArguments().thenReturn(connectionOptions);

        doNothing().when(connectionOptions).load(any(InputStream.class));

        ConnectionOptions options = argumentCaptor.getValue().resolve(configObj);
        assertNotNull(options);

    }

    @Test(expected = PluginException.class)
    public void ShouldThrowException_When_InternalExceptionIsThrown() throws Exception {

        whenNew(BootstrapItem.class).withArguments("ConnectionOptionsPlugin").thenThrow(new InvalidArgumentException(""));
        plugin.load();
    }
}
