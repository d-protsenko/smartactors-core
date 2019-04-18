package info.smart_tools.smartactors.actor.exception_handler_actor.wrapper;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

/**
 * Created by sevenbits on 8/10/16.
 */
public interface IExceptionHandler {

    IObject getMessage() throws ReadValueException;

    IObject getContext() throws ReadValueException;

    IObject getResponse() throws ReadValueException;

    IObject getConfig() throws ReadValueException;
}
