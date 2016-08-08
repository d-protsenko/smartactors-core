package info.smart_tools.smartactors.actors.mailing.email;

import info.smart_tools.smartactors.actors.mailing.exception.PartCreatorException;
import info.smart_tools.smartactors.core.field.Field;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagePartCreators {
    private static Map<String, MessagePartCreator> creatorsMap = new HashMap<>();
    private static Field partType_Part_F;
    private static Field partText_TextPart_F;
    private static Field partMime_Part_F;
    private static Field sourcePath_FilePart_F;
    private static Field attachmentName_FilePart_F;

    public static void addAllPartsTo(final SMTPMessageAdaptor smtpMessage, final IObject context, final List<IObject> parts) throws Exception {
        for (IObject part : parts) {
            try {
                creatorsMap
                        .get(partType_Part_F.in(part))
                        .addPartTo(smtpMessage, context, part);
            } catch (NullPointerException e) {
                throw new PartCreatorException("Part type not found.", e);
            } catch (ChangeValueException | ReadValueException | MessagingException e) {
                throw new PartCreatorException("Failed to create message part.", e);
            } catch (InvalidArgumentException e) {
                throw new Exception("Failed to extract type", e);
            }
        }
    }

    public static void add(String name, MessagePartCreator creator) {
        creatorsMap.put(name, creator);
    }

    static {
        try {
            partType_Part_F = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "type");
            partText_TextPart_F = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "text");
            partMime_Part_F = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "mime");
            sourcePath_FilePart_F = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "sourceFile");
            attachmentName_FilePart_F = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "attachmentName");
        } catch (ResolutionException e) {
            throw new RuntimeException("Failed to initialize fields", e);
        }

        add("text", (smtpMessage, context, partDescription) -> {
            try {
                MimeBodyPart part = new MimeBodyPart();
                part.setContent(
                        partText_TextPart_F.in(partDescription),
                        partMime_Part_F.in(partDescription));
                smtpMessage.addPart(part);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set part of email.", e);
            }
        });
        add("file", (smtpMessage, context, partDescription) -> {
            try {
                MimeBodyPart part = new MimeBodyPart();

                DataSource dataSource = new FileDataSource(
                        (String) sourcePath_FilePart_F.in(partDescription));

                part.setDataHandler(new DataHandler(dataSource));
                part.setFileName(
                        attachmentName_FilePart_F.in(partDescription));
                smtpMessage.addPart(part);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set file to mail", e);
            }
        });
    }
}
