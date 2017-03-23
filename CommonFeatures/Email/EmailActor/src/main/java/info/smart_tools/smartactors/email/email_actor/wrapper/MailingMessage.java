package info.smart_tools.smartactors.email.email_actor.wrapper;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

import java.util.List;

/**
 * Message wrapper for mailing actor
 */
public interface MailingMessage {
    /**
     *getter
     * @return list
     * @throws ReadValueException when something happends
     */
    List<IObject> getSendToMessage() throws ReadValueException;

    /**
     * getter
     * @return IObject
     * @throws ReadValueException when something happends
     */
    IObject getMessageAttributesMessage() throws ReadValueException;

    /**
     * getter
     * @return list of IObject
     * @throws ReadValueException when something happends
     */
    List<IObject> getMessagePartsMessage() throws ReadValueException;
}
