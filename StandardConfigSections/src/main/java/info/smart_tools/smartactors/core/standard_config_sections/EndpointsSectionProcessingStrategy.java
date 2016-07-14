package info.smart_tools.smartactors.core.standard_config_sections;

import info.smart_tools.smartactors.core.config_loader.ISectionStrategy;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;

/**
 * Creates endpoints using configuration.
 *
 * Expects the following configuration format:
 *
 * <pre>
 *     {
 *         "endpoints": [
 *             {
 *                 "type": "http",
 *                 // . . .
 *             },
 *             {
 *                 // . . .
 *             }
 *         ]
 *     }
 * </pre>
 */
public class EndpointsSectionProcessingStrategy implements ISectionStrategy {
    private final IFieldName name;

    /**
     * The constructor.
     *
     * @throws ResolutionException if fails to resolve any dependencies
     */
    public EndpointsSectionProcessingStrategy()
            throws ResolutionException {
        this.name = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "endpoints");
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
