package info.smart_tools.smartactors.core.actor_collection_receiver;

import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iroutable_object_creator.IRoutedObjectCreator;
import info.smart_tools.smartactors.core.irouter.IRouter;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.message_processing.exceptions.MessageReceiveException;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.ioc.string_ioc_key.Key;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ActorCollectionReceiver}.
 */
public class ActorCollectionReceiverTest {

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
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnCreationWithEmptyRouter()
            throws Exception {
        initIFieldNameStrategy();
        IMessageReceiver receiver = new ActorCollectionReceiver(null);
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnCreation()
            throws Exception {
        initBrokenIFieldNameStrategy();
        IRouter router = new ActorCollectionRouter();
        IMessageReceiver receiver = new ActorCollectionReceiver(router);
        fail();
    }

    @Test
    public void checkCreation() throws Exception {
        initIFieldNameStrategy();
        IRouter router = new ActorCollectionRouter();
        IMessageReceiver receiver = new ActorCollectionReceiver(router);
        assertNotNull(receiver);
    }

    @Test (expected = MessageReceiveException.class)
    public void checkMessageReceiveExceptionOnUseReceiveMethod()
            throws Exception {
        initIFieldNameStrategy();
        IRouter router = new ActorCollectionRouter();
        IMessageReceiver receiver = new ActorCollectionReceiver(router);
        receiver.receive(null);
        fail();
    }

    @Test
    public void checkUsageReceiveMethod() throws Exception {
        IRouter router = new ActorCollectionRouter();
        // init
        initIFieldNameStrategy();
        IResolveDependencyStrategy objectCreatorStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(
                        IOC.getKeyForKeyStorage(),
                        IRoutedObjectCreator.class.getCanonicalName() + "#" + "actor"
                ),
                objectCreatorStrategy
        );
        // object mocks
        IMessageProcessor processor = mock(IMessageProcessor.class);
        IMessageProcessingSequence sequence = mock(IMessageProcessingSequence.class);
        IObject section = mock(IObject.class);
        IObject subsection = mock(IObject.class);
        IObject environment = mock(IObject.class);
        String keyName = "actorId";
        String kindValue = "actor";
        Object key = "A";
        IRoutedObjectCreator creator = mock(IRoutedObjectCreator.class);
        IMessageReceiver internalReceiver = mock(IMessageReceiver.class);
        // 'when' mocks
        when(processor.getSequence()).thenReturn(sequence);
        when(processor.getEnvironment()).thenReturn(environment);
        when(sequence.getCurrentReceiverArguments()).thenReturn(section);
        when(section.getValue(new FieldName("key"))).thenReturn(keyName);
        when(section.getValue(new FieldName("new"))).thenReturn(subsection);
        when(environment.getValue(new FieldName("actorId"))).thenReturn(key);
        when(subsection.getValue(new FieldName("kind"))).thenReturn(kindValue);
        when(objectCreatorStrategy.resolve()).thenReturn(creator);
        doNothing().when(subsection).setValue(new FieldName("name"), key);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                router.register(key, internalReceiver);
                return null;
            }
        }).when(creator).createObject(router, subsection);
        doNothing().when(internalReceiver).receive(processor);
        // test
        IMessageReceiver receiver = new ActorCollectionReceiver(router);
        receiver.receive(processor);
        verify(processor, times(1)).getSequence();
        verify(processor, times(1)).getEnvironment();
        verify(sequence, times(1)).getCurrentReceiverArguments();
        verify(section, times(1)).getValue(new FieldName("key"));
        verify(section, times(1)).getValue(new FieldName("new"));
        verify(environment, times(1)).getValue(new FieldName("actorId"));
        verify(subsection, times(1)).getValue(new FieldName("kind"));
        verify(objectCreatorStrategy, times(1)).resolve();
        verify(subsection, times(1)).setValue(new FieldName("name"), key);
        assertSame(router.route(key), internalReceiver);
        verify(internalReceiver, times(1)).receive(processor);
    }

    @Test
    public void checkUsageStoredReceiver()
            throws Exception {
        IRouter router = new ActorCollectionRouter();
        Object key = "A";
        String keyName = "actorId";
        // init
        initIFieldNameStrategy();
        // object mocks
        IMessageReceiver internalReceiver = mock(IMessageReceiver.class);
        IMessageProcessor processor = mock(IMessageProcessor.class);
        IMessageProcessingSequence sequence = mock(IMessageProcessingSequence.class);
        IObject section = mock(IObject.class);
        IObject environment = mock(IObject.class);
        // 'when' mocks
        when(processor.getSequence()).thenReturn(sequence);
        when(processor.getEnvironment()).thenReturn(environment);
        when(sequence.getCurrentReceiverArguments()).thenReturn(section);
        when(section.getValue(new FieldName("key"))).thenReturn(keyName);
        when(environment.getValue(new FieldName("actorId"))).thenReturn(key);

        router.register(key, internalReceiver);

        IMessageReceiver receiver = new ActorCollectionReceiver(router);
        receiver.receive(processor);

        verify(internalReceiver, times(1)).receive(processor);
    }

    @Test
    public void checkLockOnConcurrentCreateNewCollectionItem()
            throws Exception {
        IRouter router = new ActorCollectionRouter();
        // init
        initIFieldNameStrategy();
        IResolveDependencyStrategy objectCreatorStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(
                        IOC.getKeyForKeyStorage(),
                        IRoutedObjectCreator.class.getCanonicalName() + "#" + "actor"
                ),
                objectCreatorStrategy
        );
        // object mocks
        IMessageProcessor processor1 = mock(IMessageProcessor.class);
        IMessageProcessor processor2 = mock(IMessageProcessor.class);
        IMessageProcessingSequence sequence = mock(IMessageProcessingSequence.class);
        IObject section = mock(IObject.class);
        IObject subsection = mock(IObject.class);
        IObject environment = mock(IObject.class);
        String keyName = "actorId";
        String kindValue = "actor";
        Object key = "A";
        IRoutedObjectCreator creator = mock(IRoutedObjectCreator.class);
        IMessageReceiver internalReceiver = mock(IMessageReceiver.class);
        // 'when' mocks
        when(processor1.getSequence()).thenReturn(sequence);
        when(processor1.getEnvironment()).thenReturn(environment);
        when(processor2.getSequence()).thenReturn(sequence);
        when(processor2.getEnvironment()).thenReturn(environment);
        when(sequence.getCurrentReceiverArguments()).thenReturn(section);
        when(section.getValue(new FieldName("key"))).thenReturn(keyName);
        when(section.getValue(new FieldName("new"))).thenReturn(subsection);
        when(environment.getValue(new FieldName("actorId"))).thenReturn(key);
        when(subsection.getValue(new FieldName("kind"))).thenReturn(kindValue);
        when(objectCreatorStrategy.resolve()).thenReturn(creator);
        doNothing().when(subsection).setValue(new FieldName("name"), key);
        Notification notification1 = new Notification();
        Notification notification2 = new Notification();
        IScope mainScope = ScopeProvider.getCurrentScope();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                notification1.setReached(true);
                while (!notification2.isReached()) {
                }
                router.register(key, internalReceiver);
                return null;
            }
        }).when(creator).createObject(router, subsection);
        doNothing().when(internalReceiver).receive(processor1);
        doNothing().when(internalReceiver).receive(processor2);
        // test
        IMessageReceiver receiver = new ActorCollectionReceiver(router);
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ScopeProvider.setCurrentScope(mainScope);
                    receiver.receive(processor1);
                } catch (Throwable e) {

                }
            }
        }, "thread-1");
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ScopeProvider.setCurrentScope(mainScope);
                    receiver.receive(processor2);
                } catch (Throwable e) {

                }
            }
        }, "thread-2");

        thread1.start();
        while (!notification1.isReached()) {
        }
        thread2.start();
        while (true) {
            if (thread2.getState().equals(Thread.State.WAITING)) {
                notification2.setReached(true);
                break;
            }
        }
        while (true) {
            if (thread1.getState().equals(Thread.State.TERMINATED) && thread2.getState().equals(Thread.State.TERMINATED)) {
                break;
            }
        }
        verify(creator, times(1)).createObject(router, subsection);
        assertEquals(router.route(key), internalReceiver);
        verify(internalReceiver, times(1)).receive(processor1);
        verify(internalReceiver, times(1)).receive(processor2);
    }

    private void initIFieldNameStrategy()
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
    }

    private void initBrokenIFieldNameStrategy()
            throws Exception {
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            throw new RuntimeException("Could not create new instance of FieldName");
                        }
                )
        );
    }
}

class Notification {

    private volatile boolean reached = false;

    public boolean isReached() {
        return reached;
    }

    public void setReached(boolean reached) {
        this.reached = reached;
    }
}
