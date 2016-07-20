package info.smart_tools.smartactors.core.standard_config_sections;

import info.smart_tools.smartactors.core.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.core.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iroutable_object_creator.IRoutedObjectCreator;
import info.smart_tools.smartactors.core.iroutable_object_creator.exceptions.ObjectCreationException;
import info.smart_tools.smartactors.core.irouter.IRouter;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.util.List;

/**
 * Creates objects using configuration.
 *
 * Expects the following configuration format:
 *
 * <pre>
 *     {
 *         "objects": [
 *             {
 *                 "kind": "actor",
 *                 // . . .
 *             },
 *             {
 *                 // . . .
 *             }
 *         ]
 *     }
 * </pre>
 */
public class ObjectsSectionProcessingStrategy implements ISectionStrategy {
    private final IFieldName name;

    private final IFieldName objectKindField;

    /**
     * The constructor.
     *
     * @throws ResolutionException if fails to resolve any dependencies
     */
    public ObjectsSectionProcessingStrategy()
            throws ResolutionException {
        this.name = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "objects");
        this.objectKindField = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "kind");
    }

    @Override
    public void onLoadConfig(final IObject config) throws ConfigurationProcessingException {
        try {
            List<IObject> section = (List<IObject>) config.getValue(name);
            IRouter router = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IRouter.class.getCanonicalName()));

            for (IObject objDesc : section) {
                Object kindId = objDesc.getValue(objectKindField);
                IRoutedObjectCreator objectCreator = IOC.resolve(
                        Keys.getOrAdd(IRoutedObjectCreator.class.getCanonicalName() + "#" + String.valueOf(kindId)));

                objectCreator.createObject(router, objDesc);
            }
        } catch (ResolutionException | ReadValueException | InvalidArgumentException e) {
            throw new ConfigurationProcessingException("Error occurred loading \"objects\" configuration section.", e);
        } catch (ObjectCreationException e) {
            throw new ConfigurationProcessingException("Could not create object described in \"objects\" section.", e);
        }
    }

    @Override
    public IFieldName getSectionName() {
        return name;
    }
}
