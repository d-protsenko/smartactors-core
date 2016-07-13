package info.smart_tools.smartactors.core.receiver_generator;

import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject_wrapper.IObjectWrapper;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.wds_object.WDSObject;

public class GeneratedReceiver implements IMessageReceiver {

    private IObject wrapperedIObject;

    public GeneratedReceiver(IObject configuration) throws InvalidArgumentException {
        try {
            this.wrapperedIObject = new WDSObject((IObject) configuration.getValue(new FieldName("wrapper")));
        } catch (Throwable e) {
            throw new InvalidArgumentException(
                    "Could not create instance of " + this.getClass().getCanonicalName() + ".", e
            );
        }
    }

    void receive(IMessageProcessor processor) throws MessageReceiveException, AsynchronousOperationException {
        try {
            IActor a = IOC.resolve(Keys.getOrAdd("customActorInstanceID"));
            ((IObjectWrapper) this.wrapperedIObject).init(processor.getEnvironment());
            IObjectWrapper customHandlerWrapper = IOC.resolve(Keys.getOrAdd("customActorInstanceID_getCustomHandlerName"));
            customHandlerWrapper.init(this.wrapperedIObject);
            a.doSomeWork((ICustomWrapper) customHandlerWrapper);
        } catch (Throwable e) {
            throw new MessageReceiveException("Could not execute receiver operation.", e);
        }
    }
}
