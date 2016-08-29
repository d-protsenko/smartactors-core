package info.smart_tools.smartactors.actors;

import info.smart_tools.smartactors.actors.exception.SampleException;
import info.smart_tools.smartactors.actors.wrapper.SampleWrapper;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.message_bus.MessageBus;

/**
 * Created by sevenbits on 7/28/16.
 */
public class SampleActor {

    private Integer state = 0;

    public void transformAndPutForResponse(SampleWrapper wrapper)
            throws SampleException {
        try {
            ++this.state;
            String s = wrapper.getSomeField();
            wrapper.setSomeValueForRequest(s + "_transformed");
            if (wrapper.resetState()) {
                this.state = 0;
            }
            wrapper.setCurrentActorState(this.state);
            IObject newMessage = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.getCanonicalName())
            );
            IFieldName name = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                    "value"
            );
            IFieldName nameMessageMapId = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                    "messageMapId"
            );
            newMessage.setValue(name, s + "_sendingMessage");
            newMessage.setValue(nameMessageMapId, "myChainOther");
            MessageBus.send(newMessage);
        } catch (Exception e) {
            throw new SampleException();
        }
    }
}
