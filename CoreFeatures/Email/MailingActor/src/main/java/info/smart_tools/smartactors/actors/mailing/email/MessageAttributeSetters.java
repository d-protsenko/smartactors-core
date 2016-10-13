package info.smart_tools.smartactors.actors.mailing.email;

import info.smart_tools.smartactors.actors.mailing.exception.AttributeSetterException;
import info.smart_tools.smartactors.field.field.Field;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * MessageAttributeSetters
 */
public class MessageAttributeSetters {
    private static Map<String, MessageAttributeSetter> settersMap = new HashMap<>();
    private static Field senderAddress_Context_F;

    /**
     * apply all setters to SMTPMessageAdaptor
     * @param attributes IObject
     * @param context IObject
     * @param to SMTPMessageAdaptor
     */
    public static void applyAll(final IObject attributes, final IObject context, final SMTPMessageAdaptor to)
            throws AttributeSetterException {
        Iterator<Map.Entry<IFieldName, Object>> iterator = attributes.iterator();

        while (iterator.hasNext()) {
            Map.Entry<IFieldName, Object> entry = iterator.next();
            try {
                settersMap.get(entry.getKey().toString()).setOn(to, context, entry.getValue());
            } catch (NullPointerException e) {
                throw new AttributeSetterException("Setter not found", e);
            } catch (MessagingException | ReadValueException | ChangeValueException e) {
                throw new AttributeSetterException("Failed to set attribute", e);
            }
        }
    }

    /**
     * Set value to setterMap
     * @param name the name
     * @param setter the setter
     */
    public static void add(final String name, final MessageAttributeSetter setter) {
        settersMap.put(name, setter);
    }

    static {
        try {
            senderAddress_Context_F = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "senderAddress");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize field", e);
        }

        add("subject", (message, context, value) ->
                message.getMimeMessage().setSubject(String.valueOf(value)));
        add("sign", (message, context, value) -> {
            try {
                message.getMimeMessage().setFrom(
                        new InternetAddress(String.format("%s<%s>",
                                String.valueOf(value),
                                senderAddress_Context_F.in(context))));
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize static block", e);
            }
        });
    }
}
