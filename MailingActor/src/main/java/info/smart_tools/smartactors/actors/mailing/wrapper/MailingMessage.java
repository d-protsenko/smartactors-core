package info.smart_tools.smartactors.actors.mailing.wrapper;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

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
    List<String> getSendToMessage() throws ReadValueException;

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
