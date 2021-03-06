package info.smart_tools.smartactors.message_processing.handler_routing_receiver_creator;

import info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.ioc.string_ioc_key.Key;
import info.smart_tools.smartactors.message_processing.handler_routing_receiver.HandlerRoutingReceiver;
import info.smart_tools.smartactors.message_processing_interfaces.ireceiver_generator.IReceiverGenerator;
import info.smart_tools.smartactors.message_processing_interfaces.iroutable_object_creator.exceptions.ObjectCreationException;
import info.smart_tools.smartactors.message_processing_interfaces.irouter.IRouter;
import info.smart_tools.smartactors.message_processing_interfaces.iwrapper_generator.IWrapperGenerator;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link HandlerRoutingReceiverCreator}
 */
public class HandlerRoutingReceiverCreatorTest {

    @Before
    public void init()
            throws Exception {
        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(keyOfMainScope);
        scope.setValue(IOC.getIocKey(), new StrategyContainer());
        ScopeProvider.setCurrentScope(scope);

        IOC.register(
                IOC.getKeyForKeyByNameStrategy(),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return new Key((String) a[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );
    }

    @Test
    public void checkCreationAndExecution()
            throws Exception {
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return new FieldName((String) a[0]);
                            } catch (Throwable e) {
                                throw new RuntimeException("Could not create new instance of FieldName", e);
                            }
                        }
                )
        );
        IField field = mock(IField.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameStrategy(), IField.class.getCanonicalName()),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            return field;
                        }
                )
        );
        IObject objectSection = mock(IObject.class);
        when(objectSection.getValue(new FieldName("name"))).thenReturn("actorID");
        when(objectSection.getValue(new FieldName("dependency"))).thenReturn("createSampleActorStrategy");
        IStrategy createSampleActorStrategy = mock(IStrategy.class);
        IOC.register(Keys.getKeyByName("createSampleActorStrategy"), createSampleActorStrategy);
        ConstructorWrapperImpl wrapperImpl = new ConstructorWrapperImpl();
        CustomActor a = new CustomActor(wrapperImpl);
        when(createSampleActorStrategy.resolve(objectSection))
                .thenReturn(a);

        // register wrapper generator
        IStrategy wgs = mock(IStrategy.class);
        IWrapperGenerator wg = mock(IWrapperGenerator.class);
        IOC.register(Keys.getKeyByName(IWrapperGenerator.class.getCanonicalName()), wgs);
        when(wgs.resolve()).thenReturn(wg);
        MethodWrapper mw = new MethodWrapper();
        when(wg.generate(IMethodWrapper.class)).thenReturn(mw);
        // register receiver generator
        IStrategy rgs = mock(IStrategy.class);
        IReceiverGenerator rg = mock(IReceiverGenerator.class);
        IOC.register(Keys.getKeyByName(IReceiverGenerator.class.getCanonicalName()), rgs);
        when(rgs.resolve()).thenReturn(rg);
        IMessageReceiver mr = mock(IMessageReceiver.class);
        when(rg.generate(any(CustomActor.class), any(IStrategy.class), any(String.class)))
                .thenAnswer(new Answer<IMessageReceiver>() {
                    @Override
                    public IMessageReceiver answer(InvocationOnMock invocation) throws Throwable {
                        Object[] args = invocation.getArguments();
                        assertSame(args[0], a);
                        assertEquals(((IStrategy) args[1]).resolve().getClass(), MethodWrapper.class);
                        assertEquals(args[2], "getSomeValue");
                        return mr;
                    }
                });

        HandlerRoutingReceiverCreator hrrc = new HandlerRoutingReceiverCreator();
        IRouter router = new HandlerInnerRouter();

        hrrc.createObject(router, objectSection);
        assertEquals(((HandlerInnerRouter) router).map.size(), 1);
        IMessageReceiver receiver = router.route("actorID");
        assertSame(receiver.getClass(), HandlerRoutingReceiver.class);
        // mock IMessageProcessor, IMessageProcessingSequence, IObject as current sequence
        IMessageProcessor processor = mock(IMessageProcessor.class);
        IMessageProcessingSequence sequence = mock(IMessageProcessingSequence.class);
        IObject currentSequence = mock(IObject.class);
        IObject env = mock(IObject.class);
        when(processor.getEnvironment()).thenReturn(env);
        when(processor.getSequence()).thenReturn(sequence);
        when(sequence.getCurrentReceiverArguments()).thenReturn(currentSequence);
        // configure mock of field
        when(field.in(currentSequence)).thenReturn("getSomeValue");
        doNothing().when(mr).receive(processor);
        receiver.receive(processor);
        verify(mr, times(1)).receive(processor);
        verify(createSampleActorStrategy, times(1)).resolve(objectSection);
        verify(rg, times(1)).generate(
                any(CustomActor.class),
                any(IStrategy.class),
                any(String.class)
        );
        verify(objectSection, times(1)).getValue(new FieldName("name"));
        verify(objectSection, times(1)).getValue(new FieldName("dependency"));
    }

    @Test (expected = ObjectCreationException.class)
    public void checkCreationExceptionOnWrongFieldNameStrategy()
            throws Exception {
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return null;
                            } catch (Throwable e) {
                                throw new RuntimeException("Could not create new instance of FieldName", e);
                            }
                        }
                )
        );
        HandlerRoutingReceiverCreator hrrc = new HandlerRoutingReceiverCreator();
        fail();
    }

    @Test (expected = ObjectCreationException.class)
    public void checkMethodExceptionOnWrongArgs()
            throws Exception {
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyByNameStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return new FieldName((String) a[0]);
                            } catch (Throwable e) {
                                throw new RuntimeException("Could not create new instance of FieldName", e);
                            }
                        }
                )
        );
        HandlerRoutingReceiverCreator hrrc = new HandlerRoutingReceiverCreator();
        hrrc.createObject(null, null);
        fail();
    }
}
