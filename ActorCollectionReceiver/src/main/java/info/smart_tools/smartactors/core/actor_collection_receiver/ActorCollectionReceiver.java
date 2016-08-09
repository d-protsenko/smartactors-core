package info.smart_tools.smartactors.core.actor_collection_receiver;

import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
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

    private Map<Object, IMessageReceiver> storage = new HashMap<>();
    private IObject keyResolutionRule;
    private IFieldName collectionItemKey;
    private IRouter router;

    public ActorCollectionReceiver(final IObject configSection)
            throws InvalidArgumentException {
        try {
            IFieldName wrapperFieldName = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "wrapper"
            );
            this.collectionItemKey  = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "getCollectionItemKey"
            );
            this.keyResolutionRule = new WDSObject((IObject) configSection.getValue(wrapperFieldName));
        } catch (ResolutionException | ReadValueException e) {
            throw new InvalidArgumentException("Could not create instance of ActorCollectionReceiver", e);
        }
    }

    @Override
    public void receive(final IMessageProcessor processor)
            throws MessageReceiveException, AsynchronousOperationException {
        try {
            Object key = this.keyResolutionRule.getValue(this.collectionItemKey);
            IMessageReceiver receiver = storage.get(key);
            if (null == receiver) {
                IObject mapSection = processor.getSequence().getCurrentReceiverArguments();
                IFieldName kindField = IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "kind"
                );
                IRoutedObjectCreator objectCreator = IOC.resolve(
                        IOC.resolve(
                                IOC.getKeyForKeyStorage(),
                                IRoutedObjectCreator.class.getCanonicalName() + "#" + String.valueOf(kindField)
                        )
                );
                objectCreator.createObject(router, objDesc);
                IRouter router = IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IRouter.class.getCanonicalName())
                );
            }
            receiver.receive(processor);
        } catch (InvalidArgumentException | ReadValueException | ResolutionException e) {
            throw new MessageReceiveException("", e);
        }
    }
}
