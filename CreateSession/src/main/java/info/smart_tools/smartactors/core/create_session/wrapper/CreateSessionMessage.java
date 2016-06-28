package info.smart_tools.smartactors.core.create_session.wrapper;

import com.sun.istack.internal.NotNull;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

public interface CreateSessionMessage {

    /**
     * Returns current session
     * @return Session session or null
     * @throws ChangeValueException
     * @throws ReadValueException
     */
    public Session getSession() throws ChangeValueException, ReadValueException;

    /**
     * Returns Authorization Info (example, device info)
     * @return IObject which contains auth info
     * @throws ChangeValueException
     * @throws ReadValueException
     */
    public IObject getAuthInfo() throws ChangeValueException, ReadValueException;


    /**
     * set session in message
     * @param session
     * @throws ReadValueException
     * @throws ChangeValueException
     */
    @NotNull
    public void setSession(Session session) throws ReadValueException, ChangeValueException;
}
