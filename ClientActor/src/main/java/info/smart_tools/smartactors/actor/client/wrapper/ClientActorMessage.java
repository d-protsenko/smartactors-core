package info.smart_tools.smartactors.actor.client.wrapper;

import info.smart_tools.smartactors.iobject.iobject.IObject;

/**
 * Wrapper for {@link info.smart_tools.smartactors.actor.client.ClientActor}
 */
public interface ClientActorMessage {
    IObject getRequest();

    IObject getRequestSettings();

    void setResponse(IObject response);
}
