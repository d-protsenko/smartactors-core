package info.smart_tools.smartactors.core.actor_receiver_creator;

import info.smart_tools.smartactors.core.actor_receiver.ActorReceiver;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ireceiver_generator.IReceiverGenerator;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iroutable_object_creator.exceptions.ObjectCreationException;
import info.smart_tools.smartactors.core.irouter.IRouter;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iwrapper_generator.IWrapperGenerator;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import org.junit.Before;
import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ActorReceiverCreator}
 */
public class ActorReceiverCreatorTest {

    @Before
    public void init()
            throws Exception {
        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(keyOfMainScope);
        scope.setValue(IOC.getIocKey(), new StrategyContainer());
        ScopeProvider.setCurrentScope(scope);

        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return new Key((String) a[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );

        Queue queue = mock(Queue.class);
        when(queue.isEmpty()).thenReturn(true);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), "actor_receiver_queue"),
                new SingletonStrategy(queue)
        );

        AtomicBoolean isBusy = new AtomicBoolean();
        isBusy.set(false);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), "actor_receiver_busyness_flag"),
                new SingletonStrategy(isBusy)
        );

        // register wrapper generator
        IResolveDependencyStrategy wgs = mock(IResolveDependencyStrategy.class);
        IWrapperGenerator wg = mock(IWrapperGenerator.class);
        IOC.register(Keys.getOrAdd(IWrapperGenerator.class.getCanonicalName()), wgs);
        when(wgs.resolve()).thenReturn(wg);
        MethodWrapper mw = new MethodWrapper();
        when(wg.generate(IMethodWrapper.class)).thenReturn(mw);
    }

    @Test
    public void checkCreationAndExecution()
            throws Exception {
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
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
                IOC.resolve(IOC.getKeyForKeyStorage(), IField.class.getCanonicalName()),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            return field;
                        }
                )
        );
        // register receiver generator
        IResolveDependencyStrategy rgs = mock(IResolveDependencyStrategy.class);
        IReceiverGenerator rg = mock(IReceiverGenerator.class);
        IOC.register(Keys.getOrAdd(IReceiverGenerator.class.getCanonicalName()), rgs);
        when(rgs.resolve()).thenReturn(rg);
        IMessageReceiver mr = mock(IMessageReceiver.class);
        when(rg.generate(any(CustomActor.class), any(IResolveDependencyStrategy.class), any(String.class))).thenReturn(mr);

        IObject objectSection = mock(IObject.class);
        when(objectSection.getValue(new FieldName("name"))).thenReturn("actorID");
        when(objectSection.getValue(new FieldName("dependency"))).thenReturn("createSampleActorStrategy");
        IObject wrapper = mock(IObject.class);
        when(objectSection.getValue(new FieldName("wrapper"))).thenReturn(wrapper);
        IResolveDependencyStrategy createSampleActorStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(Keys.getOrAdd("createSampleActorStrategy"), createSampleActorStrategy);
        ConstructorWrapperImpl wrapperImpl = new ConstructorWrapperImpl();
        CustomActor a = new CustomActor(wrapperImpl);
        when(createSampleActorStrategy.resolve(wrapper))
                .thenReturn(a);

        ActorReceiverCreator arc = new ActorReceiverCreator();
        IRouter router = new Router();

        arc.createObject(router, objectSection);
        assertEquals(((Router) router).map.size(), 1);
        IMessageReceiver actorReceiver = router.route("actorID");
        assertSame(actorReceiver.getClass(), ActorReceiver.class);

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
        // configure handler message receiver mock
        doNothing().when(mr).receive(processor);
        actorReceiver.receive(processor);
        verify(mr, times(1)).receive(processor);
    }

    @Test (expected = ObjectCreationException.class)
    public void checkCreationExceptionOnWrongFieldNameStrategy()
            throws Exception {
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
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
        ActorReceiverCreator arc = new ActorReceiverCreator();
        fail();
    }

    @Test (expected = ObjectCreationException.class)
    public void checkMethodExceptionOnWrongArgs()
            throws Exception {
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
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
        ActorReceiverCreator arc = new ActorReceiverCreator();
        arc.createObject(null, null);
        fail();
    }
}
