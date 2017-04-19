package info.smart_tools.smartactors.core_service_starter.core_starter;

import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainCreationException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

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
        this.name = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "maps");
        this.mapIdFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "id");
    }

    @Override
    public void onLoadConfig(final IObject config)
            throws ConfigurationProcessingException {
        try {
            List<IObject> section = (List<IObject>) config.getValue(name);

            IChainStorage chainStorage = IOC.resolve(Keys.getOrAdd(IChainStorage.class.getCanonicalName()));

            for (IObject mapDescription : section) {
                Object mapId = IOC.resolve(Keys.getOrAdd("chain_id_from_map_name"), mapDescription.getValue(mapIdFieldName));
                try {
                    chainStorage.register(mapId, mapDescription);
                } catch (ChainCreationException e) {
                    throw new ConfigurationProcessingException("Could not create chain for map #'" + String.valueOf(mapId) + "'.", e);
                }
            }
        } catch (InvalidArgumentException | ResolutionException | ReadValueException e) {
            throw new ConfigurationProcessingException("Error occurred loading \"maps\" section of configuration.", e);
        }
    }

    @Override
    public IFieldName getSectionName() {
        return name;
    }
}
