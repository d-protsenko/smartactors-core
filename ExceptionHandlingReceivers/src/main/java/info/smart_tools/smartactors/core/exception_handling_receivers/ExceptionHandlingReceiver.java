package info.smart_tools.smartactors.core.exception_handling_receivers;

import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;

/**
 *
 */
public abstract class ExceptionHandlingReceiver implements IMessageReceiver {
    private final IFieldName causeLevelFieldName;
    private final IFieldName causeStepFieldName;
    private final IFieldName catchLevelFieldName;
    private final IFieldName catchStepFieldName;
    private final IFieldName exceptionFieldName;

    /**
     * The constructor.
     *
     * @throws ResolutionException if fails to resolve any dependencies
     */
    protected ExceptionHandlingReceiver() throws ResolutionException {
        causeLevelFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.toString()), "causeLevel");
        causeStepFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.toString()), "causeStep");
        catchLevelFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.toString()), "catchLevel");
        catchStepFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.toString()), "catchStep");
        exceptionFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.toString()), "exception");
    }

    protected int getCauseLevel(final IObject context) throws ReadValueException {
        return (Integer) context.getValue(causeLevelFieldName);
    }

    protected int getCauseStep(final IObject context) throws ReadValueException {
        return (Integer) context.getValue(causeStepFieldName);
    }

    protected int getCatchLevel(final IObject context) throws ReadValueException {
        return (Integer) context.getValue(catchLevelFieldName);
    }

    protected int getCatchStep(final IObject context) throws ReadValueException {
        return (Integer) context.getValue(catchStepFieldName);
    }

    protected Throwable getException(final IObject context) throws ReadValueException {
        return (Throwable) context.getValue(exceptionFieldName);
    }
}
