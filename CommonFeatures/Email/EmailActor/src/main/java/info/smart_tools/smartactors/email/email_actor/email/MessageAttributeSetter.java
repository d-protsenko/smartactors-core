package info.smart_tools.smartactors.email.email_actor.email;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

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
