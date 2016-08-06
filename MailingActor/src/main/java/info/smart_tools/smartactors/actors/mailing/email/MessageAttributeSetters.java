package info.smart_tools.smartactors.actors.mailing.email;

import info.smart_tools.smartactors.core.field.Field;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MessageAttributeSetters {
    private static Map<String, MessageAttributeSetter> settersMap = new HashMap<>();
    private static Field senderAddress_Context_F;

    public static void applyAll(IObject attributes, IObject context, SMTPMessageAdaptor to) {
        Iterator<Map.Entry<IFieldName, Object>> iterator = attributes.iterator();
        Map.Entry<IFieldName, Object> entry = iterator.next();

        while (entry != null) {
            iterator.next();
            try {
                settersMap.get(entry.getKey().toString()).setOn(to, context, entry.getValue());
            } catch (NullPointerException e) {
                // Looks like setter not found.
                /*TODO: Handle.*/
            } catch (MessagingException | ReadValueException | ChangeValueException e) {
                // Failed to set attribute.
                /*TODO: Handle.*/
            }
        }
    }

    public static void add(String name, MessageAttributeSetter setter) {
        settersMap.put(name, setter);
    }

    static {
        try {
            senderAddress_Context_F = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "senderAddress");
        } catch (Exception ignore) {}
        add("subject", (message, context, value) ->
                message.getMimeMessage().setSubject(String.valueOf(value)));
        add("sign", (message, context, value) ->
        {
            try {
                message.getMimeMessage().setFrom(
                        new InternetAddress(String.format("%s<%s>",
                                String.valueOf(value),
                                senderAddress_Context_F.in(context))));
            } catch (Exception ignored) {}
        });
    }
}
