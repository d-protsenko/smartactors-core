package info.smart_tools.smartactors.core.standard_config_sections;

import info.smart_tools.smartactors.core.config_loader.ISectionStrategy;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;

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
    public void onLoadConfig(final IObject config) throws ReadValueException {
        // TODO: Implement
    }

    @Override
    public IFieldName getSectionName() {
        return name;
    }
}
