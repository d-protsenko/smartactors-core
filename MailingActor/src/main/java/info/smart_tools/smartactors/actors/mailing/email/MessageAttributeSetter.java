package info.smart_tools.smartactors.actors.mailing.email;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import javax.mail.MessagingException;

@FunctionalInterface
public interface MessageAttributeSetter {
    /**
     * Sets some attribute of e-mail message
     *
     * @param message the message
     * @param context properties of mailing actor and other context information
     * @param value the value of the attribute
     */
    void setOn(SMTPMessageAdaptor message, IObject context, Object value)
            throws MessagingException, ReadValueException, ChangeValueException;
}
