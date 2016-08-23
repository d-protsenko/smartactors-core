package info.smart_tools.smartactors.actors.save_session.wrapper;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Message wrapper for {@link info.smart_tools.smartactors.actors.save_session.SaveSessionActor}
 */
public interface SaveSessionMessage {
    /**
     * getter
     * @return sessionId
     * @throws ReadValueException sometimes
     */
    IObject getSession() throws ReadValueException;


}
