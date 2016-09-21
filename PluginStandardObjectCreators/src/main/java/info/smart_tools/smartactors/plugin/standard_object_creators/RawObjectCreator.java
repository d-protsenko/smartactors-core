package info.smart_tools.smartactors.plugin.standard_object_creators;

import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iroutable_object_creator.IRoutedObjectCreator;
import info.smart_tools.smartactors.core.iroutable_object_creator.exceptions.ObjectCreationException;
import info.smart_tools.smartactors.core.irouter.IRouter;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

/**
 * Implementation of {@link IRoutedObjectCreator} that creates receiver of some class define in configuration.
 *
 * Expects the following description fields:
 *
 * <pre>
 *     {
 *         "dependency": "org.my.receiver.Receiver",// name of receiver dependency (should implement {@link IMessageReceiver})
 *         "name": "receiver"                       // identifier of the receiver in the router
 *     }
 * </pre>
 */
public class RawObjectCreator implements IRoutedObjectCreator {
    private final IFieldName dependencyFieldName;
    private final IFieldName nameFieldName;

    /**
     * The constructor.
     *
     * @throws ResolutionException if fails to resolve any dependency
     */
    public RawObjectCreator()
            throws ResolutionException {
        dependencyFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "dependency");
        nameFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "name");
    }

    @Override
    public void createObject(final IRouter router, final IObject description)
            throws ObjectCreationException, InvalidArgumentException {
        if (null == router) {
            throw new InvalidArgumentException("Router should not be null.");
        }

        if (null == description) {
            throw new InvalidArgumentException("Description should not be null.");
        }

        try {
            Object address = IOC.resolve(Keys.getOrAdd("route_from_object_name"), description.getValue(nameFieldName));
            Object receiver = IOC.resolve(Keys.getOrAdd(String.valueOf(description.getValue(dependencyFieldName))), description);

            if (!(receiver instanceof IMessageReceiver)) {
                throw new ObjectCreationException("Resolved dependency does not implement receiver interface.");
            }

            router.register(address, (IMessageReceiver) receiver);
        } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
            throw new ObjectCreationException("Error occurred creating the receiver.", e);
        }
    }
}
