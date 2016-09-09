package info.smart_tools.smartactors.core.actor_collection_receiver;

import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iroutable_object_creator.IRoutedObjectCreator;
import info.smart_tools.smartactors.core.irouter.IRouter;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.message_processing.exceptions.AsynchronousOperationException;
import info.smart_tools.smartactors.core.message_processing.exceptions.MessageReceiveException;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of {@link IMessageReceiver}.
 * Specific kind of receiver that provides creation of actors collection and its usage by keys.
 * * Expected format of config section (example):
 * <pre>
 * {
 *     objects: {
 *     . . .
 *        {
 *            "kind": "raw",
 *            "dependency": "ActorCollection",
 *            "name": "my-actor-collection"
 *        },
 *     . . .
 *     },
 *     . . .
 *     {
 *         {
 *             "target": "my-actor-collection",
 *             "handler": "transformAndPutForResponse",
 *             "new": {
 *                 "kind": "actor",
 *                 "dependency": "SampleActor"
 *             },
 *             "key": "in_actorId",
 *             "wrapper": {
 *                 "in_actorId": "message/ActorId",
 *                 "in_getSomeField": "message/Field",
 *                 "out_setSomeValueForRequest": "response/TransformedField",
 *                 "out_setCurrentActorState": "response/CurrentState",
 *                 "in_resetState": "message/Reset"
 *             }
 *         }
 *     }
 *     . . .
 * }
 * </pre>
 */
public class ActorCollectionReceiver implements IMessageReceiver {

    private IFieldName keyFieldName;
    private IFieldName newFieldName;
    private IFieldName kindFieldName;
    private IFieldName nameFieldName;
    private IRouter router;

    private Lock lock = new ReentrantLock();

    /**
     * Default constructor
     * @param router the instance of {@link ActorCollectionRouter}.
     * @throws InvalidArgumentException if any errors occurred
     */
    public ActorCollectionReceiver(final IRouter router)
            throws InvalidArgumentException {
        if (null == router) {
            throw new InvalidArgumentException("Router should not be null.");
        }
        try {
            this.router = router;
            this.keyFieldName  = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "key"
            );
            this.newFieldName  = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "new"
            );
            this.kindFieldName  = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "kind"
            );
            this.nameFieldName  = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "name"
            );
        } catch (ResolutionException e) {
            throw new InvalidArgumentException("Could not create instance of ActorCollectionReceiver", e);
        }
    }

    @Override
    public void receive(final IMessageProcessor processor)
            throws MessageReceiveException, AsynchronousOperationException {
        try {
            IObject section = processor.getSequence().getCurrentReceiverArguments();
            String keyName = (String) section.getValue(this.keyFieldName);
            IObject subSectionNew = (IObject) section.getValue(this.newFieldName);
            IFieldName fieldNameForKeyName = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), keyName
            );
            Object key =  processor.getEnvironment().getValue(fieldNameForKeyName);
            IMessageReceiver receiver = router.route(key);
            if (null == receiver) {
                lock.lock();
                receiver = router.route(key);
                if (null == receiver) {
                    String kindValue = (String) subSectionNew.getValue(this.kindFieldName);
                    IRoutedObjectCreator objectCreator = IOC.resolve(
                            IOC.resolve(
                                    IOC.getKeyForKeyStorage(),
                                    IRoutedObjectCreator.class.getCanonicalName() + "#" + kindValue
                            )
                    );
                    subSectionNew.setValue(this.nameFieldName, key);
                    objectCreator.createObject(this.router, subSectionNew);
                    // TODO: bad solution, need refactoring. Change interface of IRoutedObjectCreator
                    receiver = this.router.route(key);
                }
                lock.unlock();
            }
            receiver.receive(processor);
        } catch (Throwable e) {
            throw new MessageReceiveException("Could not execute ActorCollectionReceiver.receive.", e);
        }
    }
}