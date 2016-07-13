package info.smart_tools.smartactors.core.receiver_generator;

import info.smart_tools.smartactors.core.receiver_generator.CustomActor;
import info.smart_tools.smartactors.core.receiver_generator.ICustomWrapper;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject_wrapper.IObjectWrapper;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.message_processing.exceptions.AsynchronousOperationException;
import info.smart_tools.smartactors.core.message_processing.exceptions.MessageReceiveException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.wds_object.WDSObject;

public class actorID_doSomeWork_receiver implements IMessageReceiver {
    private IObject wrappedIObject;

    public actorID_doSomeWork_receiver(IObject configuration) throws InvalidArgumentException  {
        try {
            this.wrappedIObject = new WDSObject((IObject) configuration.getValue(new FieldName("wrapper")));
        } catch (Throwable e) {
            throw new InvalidArgumentException(
                    "Could not create instance of " + this.getClass().getCanonicalName() + ".", e
            );
        }

    }

    public void receive(IMessageProcessor processor) throws MessageReceiveException, AsynchronousOperationException  {
        try {
            CustomActor a = IOC.resolve(Keys.getOrAdd("actorID"));
            ((IObjectWrapper) this.wrappedIObject).init(processor.getEnvironment());
            IObjectWrapper wrapper = IOC.resolve(Keys.getOrAdd("actorID_doSomeWork"));
            wrapper.init(this.wrappedIObject);
            a.doSomeWork((ICustomWrapper) wrapper);
        } catch (Throwable e) {
            throw new MessageReceiveException("Could not execute receiver operation.", e);
        }

    }

}