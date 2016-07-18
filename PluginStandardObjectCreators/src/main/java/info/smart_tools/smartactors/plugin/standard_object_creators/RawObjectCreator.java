package info.smart_tools.smartactors.plugin.standard_object_creators;

import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
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
 *         "class": "org.my.receiver.Receiver",     // class of the receiver (should implement {@link IMessageReceiver})
 *         "name": "receiver"                       // identifier of the receiver in the router
 *     }
 * </pre>
 *
 * TODO: Pass arguments from configuration to receiver's constructor
 */
public class RawObjectCreator implements IRoutedObjectCreator {
    private final IFieldName classFieldName;
    private final IFieldName nameFieldName;

    /**
     * The constructor.
     *
     * @throws ResolutionException if fails to resolve any dependency
     */
    public RawObjectCreator()
            throws ResolutionException {
        classFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "class");
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
            Class<?> clazz = this.getClass().getClassLoader().loadClass(String.valueOf(description.getValue(classFieldName)));

            if (!IMessageReceiver.class.isAssignableFrom(clazz)) {
                throw new ObjectCreationException("Required receiver class does not implement receiver interface.");
            }

            router.register(address, (IMessageReceiver) clazz.newInstance());
        } catch (ClassNotFoundException e) {
            throw new ObjectCreationException("Required receiver class not found.", e);
        } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
            throw new ObjectCreationException("Error occurred creating the receiver.", e);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ObjectCreationException("Could not invoke receiver's constructor.", e);
        }
    }
}
