package info.smart_tools.smartactors.actors.prepare_registration_mail.wrapper;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

import java.util.List;

/**
 * Wrapper for message for {@link info.smart_tools.smartactors.actors.prepare_registration_mail.PrepareRegistrationMailActor}
 */
public interface PrepareMailMessage {
    /**
     * Getter for email
     * @return email
     * @throws ReadValueException
     */
    String getEmail() throws ReadValueException;

    /**
     * Getter for token
     * @return token
     * @throws ReadValueException
     */
    String getToken() throws ReadValueException;

    /**
     * Getter for url
     * @return url
     * @throws ReadValueException
     */
    String getUrl() throws ReadValueException;

    /**
     * Setter for email parts
     * @param parts list of IObjects
     * @throws ChangeValueException somrtimes
     */
    void setMessageParts(List<IObject> parts) throws ChangeValueException;

    /**
     * Setter for email parts
     * @param attr IObject
     * @throws ChangeValueException somrtimes
     */
    void setMessageAttributes(IObject attr) throws ChangeValueException;

    /**
     * Setter for recipients
     * @param recipients list of emails
     * @throws ChangeValueException sometimes
     */
    void setRecipients(List<String> recipients) throws ChangeValueException;
}
