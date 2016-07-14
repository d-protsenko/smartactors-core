package info.smart_tools.smartactors.core.receiver_generator;

import info.smart_tools.smartactors.core.class_generator_java_compile_api.ClassGenerator;
import info.smart_tools.smartactors.core.class_generator_java_compile_api.class_builder.ClassBuilder;
import info.smart_tools.smartactors.core.class_generator_java_compile_api.class_builder.Modifiers;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.iclass_generator.IClassGenerator;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iobject_wrapper.IObjectWrapper;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ireceiver_generator.IReceiverGenerator;
import info.smart_tools.smartactors.core.ireceiver_generator.exception.ReceiverGeneratorException;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.message_processing.exceptions.AsynchronousOperationException;
import info.smart_tools.smartactors.core.message_processing.exceptions.MessageReceiveException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.wds_object.WDSObject;

import java.lang.reflect.Method;

/**
 * Implementation of {@link IReceiverGenerator}.
 * <pre>
 * Main features of implementation:
 * - use class generator based on java compile API;
 * </pre>
 */
public class ReceiverGenerator implements IReceiverGenerator {

    private IClassGenerator<String> classGenerator;

    private FieldName actorIdFieldName;
    private FieldName handlerFieldName;
    private FieldName wrapperFieldName;

    /**
     * Constructor.
     * Create new instance of {@link ReceiverGenerator} by given {@link ClassLoader}
     * @param classLoader the instance of {@link ClassLoader}
     * @throws InvalidArgumentException if initialization inner instances of {@link FieldName} was failed
     */
    public ReceiverGenerator(final ClassLoader classLoader)
            throws InvalidArgumentException {
        this.classGenerator = new ClassGenerator(classLoader);
        this.actorIdFieldName = new FieldName("actor");
        this.handlerFieldName = new FieldName("handler");
        this.wrapperFieldName = new FieldName("wrapper");
    }

    @Override
    public IMessageReceiver generate(
            final IObject wrapperConfiguration
    ) throws InvalidArgumentException, ReceiverGeneratorException {
        if (null == wrapperConfiguration) {
            throw new InvalidArgumentException("Actor canonical class name should not be null.");
        }
        String actorID = null;
        String handlerName = null;
        IMessageReceiver instance = null;
        try {
            actorID = (String) wrapperConfiguration.getValue(actorIdFieldName);
            handlerName = (String) wrapperConfiguration.getValue(handlerFieldName);
            if (null == actorID || null == handlerName || actorID.isEmpty() || handlerName.isEmpty()) {
                throw new ReadValueException("Actor ID and handler name should not be null or empty.");
            }
        } catch (ReadValueException e) {
            throw new InvalidArgumentException("Given wrapper configuration is incorrect.");
        }
        try {
            Object actor = IOC.resolve(Keys.getOrAdd(actorID));
            if (null == actor) {
                throw new Exception("Actor not registered in IOC.");
            }
            Class<IMessageReceiver> clazz = generateClass(
                    actorID,
                    handlerName,
                    actor
            );
            return clazz.getConstructor(new Class[]{IObject.class})
                    .newInstance(new Object[]{wrapperConfiguration.getValue(this.wrapperFieldName)});
        } catch (Throwable e) {
            throw new ReceiverGeneratorException(
                    "Could not generate message receiver because of the following error:",
                    e
            );
        }
    }

    private Class<IMessageReceiver> generateClass(
            final String actorID,
            final String handlerName,
            final Object actor
    )
            throws Exception {
        ClassBuilder cb = new ClassBuilder("\t", "\n");
        Class wrapperInterface = findWrapperInterface(actor, handlerName);

        // Add package name and imports
        cb
                .addPackageName(actor.getClass().getPackage().getName())
                .addImport(actor.getClass().getCanonicalName())
                .addImport(wrapperInterface.getCanonicalName())
                .addImport(FieldName.class.getCanonicalName())
                .addImport(InvalidArgumentException.class.getCanonicalName())
                .addImport(IObject.class.getCanonicalName())
                .addImport(IObjectWrapper.class.getCanonicalName())
                .addImport(IOC.class.getCanonicalName())
                .addImport(IMessageProcessor.class.getCanonicalName())
                .addImport(IMessageReceiver.class.getCanonicalName())
                .addImport(AsynchronousOperationException.class.getCanonicalName())
                .addImport(MessageReceiveException.class.getCanonicalName())
                .addImport(Keys.class.getCanonicalName())
                .addImport(WDSObject.class.getCanonicalName());

        // Add class header
        cb
                .addClass()
                .setClassModifier(Modifiers.PUBLIC)
                .setClassName(actorID + "_" + handlerName + "_" + "receiver")
                .setInterfaces(IMessageReceiver.class.getSimpleName());

        // Add fields
        cb
                .addField()
                .setModifier(Modifiers.PRIVATE)
                .setType(IObject.class.getSimpleName())
                .setName("wrappedIObject");

        // Add constructor
        cb
                .addConstructor()
                .setModifier(Modifiers.PUBLIC)
                .setExceptions(InvalidArgumentException.class.getSimpleName())
                .setParameters()
                    .setType(IObject.class.getSimpleName())
                    .setName("configuration")
                    .next()
                .addStringToBody("try {")
                .addStringToBody("\tthis.wrappedIObject = new WDSObject((IObject) configuration.getValue(new FieldName(\"wrapper\")));")
                .addStringToBody("} catch (Throwable e) {")
                .addStringToBody("throw new InvalidArgumentException(")
                .addStringToBody("\"Could not create instance of \" + this.getClass().getCanonicalName() + \".\", e")
                .addStringToBody(");")
                .addStringToBody("}");

        // Add method
        cb
                .addMethod()
                .setModifier(Modifiers.PUBLIC)
                .addParameter()
                        .setType(IMessageProcessor.class.getSimpleName())
                        .setName("processor")
                        .next()
                .setReturnType("void")
                .setName("receive")
                .setExceptions(MessageReceiveException.class.getSimpleName())
                .setExceptions(AsynchronousOperationException.class.getSimpleName())
                .addStringToBody("try {")
                .addStringToBody(
                        actor.getClass().getSimpleName() + " a = IOC.resolve(Keys.getOrAdd(\"" + actorID + "\"));"
                )
                .addStringToBody("((IObjectWrapper) this.wrappedIObject).init(processor.getEnvironment());")
                .addStringToBody(
                        "IObjectWrapper wrapper = IOC.resolve(Keys.getOrAdd("
                        + wrapperInterface.getSimpleName() + ".class.getCanonicalName()));"
                )
                .addStringToBody("wrapper.init(this.wrappedIObject);")
                .addStringToBody("a." + handlerName + "((" + wrapperInterface.getSimpleName() + ") wrapper);")
                .addStringToBody("} catch (Throwable e) {")
                .addStringToBody("throw new MessageReceiveException(\"Could not execute receiver operation.\", e);")
                .addStringToBody("}");

        return (Class<IMessageReceiver>) classGenerator.generate(cb.buildClass().toString());
    }

    private Class findWrapperInterface(final Object actor, final String handler) {
        Method[] methods = actor.getClass().getDeclaredMethods();
        Class wrapperInterface = null;
        for (Method m : methods) {
            if (m.getName().equals(handler)) {
                wrapperInterface = m.getParameterTypes()[0];
            }
        }

        return wrapperInterface;
    }
}
