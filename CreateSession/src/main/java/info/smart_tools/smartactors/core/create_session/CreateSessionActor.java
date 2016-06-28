package info.smart_tools.smartactors.core.create_session;

import info.smart_tools.smartactors.core.create_session.wrapper.CreateSessionMessage;
import info.smart_tools.smartactors.core.create_session.wrapper.Session;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

public class CreateSessionActor {

    public CreateSessionActor (IObject initIObject) {

    }

    public void createSession(CreateSessionMessage inputMessage) {
        try {
            Session curSession = inputMessage.getSession();
            if (curSession == null){
                IObject authInfo = inputMessage.getAuthInfo();
                curSession = IOC.resolve(Keys.getOrAdd(Session.class.toString()));
                curSession.setAuthInfo(authInfo);
                inputMessage.setSession(curSession);
            }
        } catch (ReadValueException | ChangeValueException e) {
            //TODO:: handle exception
        } catch (ResolutionException e) {
            //TODO:: maybe throw new MessageHandleException
            throw new RuntimeException("Cannot resolve Session.class", e);
        }
    }

}
