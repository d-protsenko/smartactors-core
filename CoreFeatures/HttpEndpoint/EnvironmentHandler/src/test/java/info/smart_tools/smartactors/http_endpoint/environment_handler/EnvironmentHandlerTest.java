package info.smart_tools.smartactors.http_endpoint.environment_handler;


import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.endpoint.interfaces.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.message_processing.message_processing_sequence.MessageProcessingSequence;
import info.smart_tools.smartactors.message_processing.receiver_chain.ImmutableReceiverChain;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.task.blocking_queue.BlockingQueue;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class EnvironmentHandlerTest {

    IMessageProcessor messageProcessor;

    @Before
    public void setUp()
            throws ScopeProviderException, RegistrationException, ResolutionException, InvalidArgumentException {
        ScopeProvider.subscribeOnCreationNewScope(
                scope -> {
                    try {
                        scope.setValue(IOC.getIocKey(), new StrategyContainer());
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }
        );

        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope mainScope = ScopeProvider.getScope(keyOfMainScope);
        ScopeProvider.setCurrentScope(mainScope);
        IOC.register(
                IOC.getKeyForKeyByNameStrategy(),
                new ResolveByNameIocStrategy()
        );
        IKey keyIObjectByString = Keys.getKeyByName("IObjectByString");
        IKey keyIObject = Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject");
        IKey keyIMessageProcessingSequence = Keys.getKeyByName("info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence");
        IKey keyIReceiverChain = Keys.getKeyByName(IReceiverChain.class.toString());
        IKey keyIFieldName = Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName");
        IKey keyFieldName = Keys.getKeyByName(IFieldName.class.getCanonicalName());
        IOC.register(
                keyIObjectByString,
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            try {
                                return new DSObject((String) args[0]);
                            } catch (InvalidArgumentException ignored) {
                            }
                            return null;
                        }
                )
        );
        IOC.register(
                keyIMessageProcessingSequence,
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            try {
                                boolean switchScopeOnStartup = args.length > 3 ? (Boolean)args[3] : true;
                                return new MessageProcessingSequence((int) args[0], args[1], (IObject)args[2], switchScopeOnStartup);
                            } catch (InvalidArgumentException | ResolutionException | ChainNotFoundException ignored) {
                            }
                            return null;
                        }
                )
        );
        IOC.register(
                keyIFieldName,
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            try {
                                return new FieldName((String) args[0]);
                            } catch (InvalidArgumentException ignored) {
                            }
                            return null;
                        }
                )
        );
        IOC.register(
                keyFieldName,
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            try {
                                return new FieldName((String) args[0]);
                            } catch (InvalidArgumentException ignored) {
                            }
                            return null;
                        }
                )
        );
    }

    @Test
    public void whenEnvironmentHandlerReceiveEnvironment_ItShouldProcessMessageProcessor()
            throws Exception {
        messageProcessor = mock(IMessageProcessor.class);
        IKey keyIMessageProcessor = Keys.getKeyByName("info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor");
        IOC.register(
                keyIMessageProcessor,
                new SingletonStrategy(messageProcessor)
        );
        IObject iObject = IOC.resolve(Keys.getKeyByName("IObjectByString"), "{}");
        IKey keyIObject = Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject");
        IOC.register(
                keyIObject,
                new SingletonStrategy(iObject)
        );
        IObject environment = IOC.resolve(Keys.getKeyByName("IObjectByString"), "{\"message\": {\"hello\": \"world\"}, \"context\": {}}");
        Map<Class<? extends Throwable>, IObject> exceptionalChainsAndEnv = new HashMap<>();
//        exceptionalChainsAndEnv.put(InvalidArgumentException.class, null);
        IMessageReceiver messageReceivers[] = new IMessageReceiver[1];
        IObject iObjects[] = new IObject[1];
        messageReceivers[0] = null;
        iObjects[0] = null;
        IReceiverChain chain = new ImmutableReceiverChain("name", mock(IObject.class), messageReceivers, iObjects, exceptionalChainsAndEnv, null, null);
        // ToDo: put chain to chain storage
        IQueue<ITask> queue = new BlockingQueue(null);

        IEnvironmentHandler handler = new EnvironmentHandler(queue, 1, true);
        handler.handle(environment, "name", null);
        try {
            verify(messageProcessor, times(1)).process(
                    (IObject) environment.getValue(IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "message")),
                    (IObject) environment.getValue(IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "context")));
        } catch (ReadValueException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(((boolean) ((IObject)environment.getValue(new FieldName("context"))).getValue(new FieldName("fromExternal"))));
    }
}
