package info.smart_tools.smartactors.core.standard_config_sections;

import info.smart_tools.smartactors.core.config_loader.ISectionStrategy;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;

import java.util.List;

/**
 * Creates and registers message maps (receiver chains) using configuration.
 *
 * Expects the following configuration format:
 *
 * <pre>
 *     {
 *         "maps": [
 *             {
 *                 "id": "mapName",
 *                 // . . .
 *             },
 *             {
 *                 "id": "map2Name",
 *                 // . . .
 *             }
 *         ]
 *     }
 * </pre>
 */
public class MapsSectionProcessingStrategy implements ISectionStrategy {
    private final IFieldName name;

    private final IFieldName mapIdFieldName;

    /**
     * The constructor.
     *
     * @throws ResolutionException if fails to resolve any dependencies
     */
    public MapsSectionProcessingStrategy()
            throws ResolutionException {
        this.name = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "maps");
        this.mapIdFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "id");
    }

    @Override
    public void onLoadConfig(final IObject config)
            throws ReadValueException {
        try {
            List<IObject> section = (List<IObject>) config.getValue(name);

            for (IObject mapDescription : section) {
                Object mapId = mapDescription.getValue(mapIdFieldName);
                IReceiverChain chain = IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyStorage(), IReceiverChain.class.getCanonicalName()),
                        mapId, mapDescription, null/*TODO: Chains storage*/, null/*TODO: Router*/);
                // TODO: register map
            }
        } catch (InvalidArgumentException | ResolutionException e) {
            // TODO:
        }
        // TODO: Implement
    }

    @Override
    public IFieldName getSectionName() {
        return name;
    }
}
