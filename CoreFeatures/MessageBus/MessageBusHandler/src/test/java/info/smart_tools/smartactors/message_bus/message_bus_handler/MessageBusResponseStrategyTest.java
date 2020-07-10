package info.smart_tools.smartactors.message_bus.message_bus_handler;

import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_bus.interfaces.imessage_bus_container.exception.SendingMessageException;
import info.smart_tools.smartactors.message_bus.interfaces.imessage_bus_handler.IMessageBusHandler;
import info.smart_tools.smartactors.message_bus.message_bus.MessageBus;
import info.smart_tools.smartactors.message_processing_interfaces.iresponse_strategy.IResponseStrategy;
import info.smart_tools.smartactors.message_processing_interfaces.iresponse_strategy.exceptions.ResponseException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Test for {@link MessageBusResponseStrategy}.
 */
public class MessageBusResponseStrategyTest extends PluginsLoadingTestBase {
    private Object replyChainId = new Object();

    private IMessageBusHandler messageBusHandlerMock;
    private IResponseStrategy nullResponseStrategyMock;

    private IObject environment;
    private IObject context;
    private IObject response;

    private MessageBusResponseStrategy strategy;

    @Override
    protected void loadPlugins() throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(PluginDSObject.class);
        load(IFieldNamePlugin.class);
    }

    @Override
    protected void registerMocks() throws Exception {
        messageBusHandlerMock = mock(IMessageBusHandler.class);
        nullResponseStrategyMock = mock(IResponseStrategy.class);

        ScopeProvider.getCurrentScope().setValue(MessageBus.getMessageBusKey(), messageBusHandlerMock);

        IOC.register(Keys.getKeyByName("null response strategy"), new SingletonStrategy(nullResponseStrategyMock));

        environment = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"));
        context = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"));
        response = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"), "{\"itIsAResponse\":true}");

        environment.setValue(IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "context"), context);
        environment.setValue(IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "response"), response);

        strategy = new MessageBusResponseStrategy();

        context.setValue(IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "responseStrategy"), strategy);
        context.setValue(IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "messageBusReplyTo"), replyChainId);
    }

    @Test
    public void Should_sendResponseAndSetResponseStrategyToNullImplementation()
            throws Exception {
        strategy.sendResponse(environment);

        ArgumentCaptor<IObject> responseCaptor = ArgumentCaptor.forClass(IObject.class);
        verify(messageBusHandlerMock).handle(responseCaptor.capture(), same(replyChainId), eq(true));

        assertEquals(Boolean.TRUE, responseCaptor.getValue().getValue(
                IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "itIsAResponse")
        ));

        assertSame(
                nullResponseStrategyMock,
                context.getValue(IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "responseStrategy")));
    }

    @Test(expected = ResponseException.class)
    public void Should_wrapExceptions()
            throws Exception {
        doThrow(SendingMessageException.class).when(messageBusHandlerMock).handle(any(), any(), eq(true));

        strategy.sendResponse(environment);
    }
}
