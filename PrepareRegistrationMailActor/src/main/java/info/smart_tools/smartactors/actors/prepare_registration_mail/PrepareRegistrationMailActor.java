package info.smart_tools.smartactors.actors.prepare_registration_mail;

import info.smart_tools.smartactors.actors.prepare_registration_mail.wrapper.PrepareMailMessage;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

import java.util.Collections;

/**
 * Actor for preparing email for confirm registration
 */
public class PrepareRegistrationMailActor {
    private static IField signF;
    private static IField subjectF;
    private static IField typeF;
    private static IField mimeF;
    private static IField textF;


    /**
     * default constructor
     * @param params IObject with params
     */
    public PrepareRegistrationMailActor(final IObject params) {
        try {
            signF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "sign");
            subjectF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "subject");
            typeF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "type");
            mimeF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "mime");
            textF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "text");
        } catch (ResolutionException e) {
            throw new RuntimeException("Failed to create PrepareRegistrationMailActor", e);
        }

    }

    /**
     * Prepare attributes for sending email
     * @param message the message
     * @throws TaskExecutionException sometimes
     */
    public void prepare(final PrepareMailMessage message) throws TaskExecutionException {
        try {
            //TODO:: remove hardcode
            IObject attributes = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
            signF.out(attributes, "SmartActors");
            subjectF.out(attributes, "Подтверждение регистрации");
            IObject messagePart = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
            textF.out(messagePart, message.getUrl() + "?token=" + message.getToken());
            mimeF.out(messagePart, "text/plain");
            typeF.out(messagePart, "text");

            message.setRecipients(Collections.singletonList(message.getEmail()));
            message.setMessageAttributes(attributes);
            message.setMessageParts(Collections.singletonList(messagePart));
        } catch (ResolutionException | ReadValueException | ChangeValueException | InvalidArgumentException e) {
            throw new TaskExecutionException("Failed to prepare email for confirm registration", e);
        }
    }
}
