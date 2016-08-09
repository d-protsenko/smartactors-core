package info.smart_tools.smartactors.core.actor_collection_receiver;

import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iroutable_object_creator.IRoutedObjectCreator;
import info.smart_tools.smartactors.core.iroutable_object_creator.exceptions.ObjectCreationException;
import info.smart_tools.smartactors.core.irouter.IRouter;
import info.smart_tools.smartactors.core.irouter.exceptions.RouteNotFoundException;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.message_processing.exceptions.AsynchronousOperationException;
import info.smart_tools.smartactors.core.message_processing.exceptions.MessageReceiveException;
import info.smart_tools.smartactors.core.wds_object.WDSObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sevenbits on 8/8/16.
 */
public class ActorCollectionReceiver implements IMessageReceiver {

    private IFieldName keyFieldName;
    private IFieldName wrapperFieldName;
    private IRouter router = new ActorCollectionRouter();

    public ActorCollectionReceiver(final IObject configSection)
            throws InvalidArgumentException {
        try {
            this.wrapperFieldName = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "wrapper"
            );
            this.keyFieldName  = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "key"
            );
        } catch (ResolutionException e) {
            throw new InvalidArgumentException("Could not create instance of ActorCollectionReceiver", e);
        }
    }

    @Override
    public void receive(final IMessageProcessor processor)
            throws MessageReceiveException, AsynchronousOperationException {
        try {
            IObject mapSection = processor.getSequence().getCurrentReceiverArguments();
            String keyName = (String) mapSection.getValue(this.keyFieldName);
            IFieldName fieldNameForKeyName = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), keyName
            );
            Object key = ((IObject) processor.getEnvironment().getValue(this.wrapperFieldName))
                    .getValue(fieldNameForKeyName);
            IMessageReceiver receiver = router.route(key);
            if (null == receiver) {

                IFieldName kindField = IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "kind"
                );
                IRoutedObjectCreator objectCreator = IOC.resolve(
                        IOC.resolve(
                                IOC.getKeyForKeyStorage(),
                                IRoutedObjectCreator.class.getCanonicalName() + "#" + String.valueOf(kindField)
                        )
                );
                objectCreator.createObject(this.router, mapSection);
                // TODO: bad solution, need refactoring. Change interface of IRoutedObjectCreator
                receiver = this.router.route(key);
            }
            receiver.receive(processor);
        } catch (Exception e) {
            throw new MessageReceiveException("Could not execute ActorCollectionReceiver.receive.", e);
        }
    }
}

class ActorCollectionRouter implements IRouter {

    private Map<Object, IMessageReceiver> storage = new HashMap<>();

    @Override
    public IMessageReceiver route(final Object targetId)
            throws RouteNotFoundException {
        return this.storage.get(targetId);
    }

    @Override
    public void register(final Object targetId, final IMessageReceiver receiver) {
        this.storage.put(targetId, receiver);
    }
}
