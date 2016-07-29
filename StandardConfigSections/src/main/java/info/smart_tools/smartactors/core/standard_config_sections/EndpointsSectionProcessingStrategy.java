package info.smart_tools.smartactors.core.standard_config_sections;

import info.smart_tools.smartactors.core.HttpEndpoint;
import info.smart_tools.smartactors.core.environment_handler.EnvironmentHandler;
import info.smart_tools.smartactors.core.iasync_service.IAsyncService;
import info.smart_tools.smartactors.core.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.core.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.core.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.core.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;

import java.util.List;

/**
 * Creates endpoints using configuration.
 * <p>
 * Expects the following configuration format:
 * <p>
 * <pre>
 *     {
 *         "endpoints": [
 *             {
 *                 "name": "endpointName",
 *                 "type": "http",
 *                 "port": 8080,
 *                 "startChain": "mainChain",
 *                 "maxContentLength": 4098,
 *                 "stackDepth": 5 (temporarily)
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
    private final IFieldName typeFieldName;
    private final IFieldName portFieldName;
    private final IFieldName startChainNameFieldName;
    private final IFieldName stackDepthFieldName;
    private final IFieldName maxContentLengthFieldName;
    private final IFieldName endpointNameFieldName;
    private final IFieldName queueFieldName;

    /**
     * The constructor.
     *
     * @throws ResolutionException if fails to resolve any dependencies
     */
    public EndpointsSectionProcessingStrategy()
            throws ResolutionException {
        this.name = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "endpoints");
        this.typeFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "type");
        this.portFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "port");
        this.startChainNameFieldName =
                IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "startChain");
        this.stackDepthFieldName =
                IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "stackDepth");
        this.maxContentLengthFieldName =
                IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "maxContentLength");
        this.endpointNameFieldName =
                IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "endpointName");
        this.queueFieldName =
                IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "queue");
    }

    @Override
    public void onLoadConfig(final IObject config) throws ConfigurationProcessingException {
        try {
            List<IObject> endpointObjects = (List<IObject>) config.getValue(name);
            IChainStorage chainStorage = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(),
                    IChainStorage.class.getCanonicalName()));
            IQueue<ITask> queue = IOC.resolve(Keys.getOrAdd("task_queue"));
            for (IObject endpoint : endpointObjects) {
                // TODO: 25.07.16 add endpoint type
                // TODO: 25.07.16 remove stack depth from endpoint config
                // TODO: 25.07.16 add endpoint name
                String type = (String) endpoint.getValue(typeFieldName);
                String startChainName = (String) endpoint.getValue(startChainNameFieldName);
                Object mapId = IOC.resolve(Keys.getOrAdd("chain_id_from_map_name"), startChainName);
                IReceiverChain chain = chainStorage.resolve(mapId);

                endpoint.setValue(startChainNameFieldName, chain);
                endpoint.setValue(queueFieldName, queue);
                IAsyncService endpointService =
                        IOC.resolve(Keys.getOrAdd(type + "_endpoint"), endpoint);
                endpointService.start();
            }
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new ConfigurationProcessingException("Error occurred loading \"endpoint\" configuration section.", e);
        } catch (ResolutionException e) {
            throw new ConfigurationProcessingException("Error occurred resolving \"endpoint\".", e);
        } catch (ChainNotFoundException e) {
            throw new ConfigurationProcessingException("Error occurred resolving \"chain\".", e);
        } catch (ChangeValueException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IFieldName getSectionName() {
        return name;
    }
}
