package info.smart_tools.smartactors.actor.exception_handler_actor;

import info.smart_tools.smartactors.actor.exception_handler_actor.wrapper.IExceptionHandler;
import info.smart_tools.smartactors.core.iobject.IObject;

/**
 * Created by sevenbits on 8/10/16.
 */
public class ExceptionHandlerActor {

    public void handle(final IExceptionHandler wrapper) {
        try {
            IObject message = wrapper.getMessage();
            System.out.println("--------------------------- Exception of execution message : " + (String) message.serialize());
        } catch (Exception e) {
            // do nothing
        }
    }
}
