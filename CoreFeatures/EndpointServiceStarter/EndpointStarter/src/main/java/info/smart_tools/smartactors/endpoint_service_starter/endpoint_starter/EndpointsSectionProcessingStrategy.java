package info.smart_tools.smartactors.endpoint_service_starter.endpoint_starter;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.endpoint.interfaces.iasync_service.IAsyncService;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.class_management.version_manager.VersionManager;

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
 *                 "stackDepth": 5 (temporarily),
 *                 "templates": [
 *                     "/messageMapId/:messageMapId/bar/:barId",
 *                     "/:messageMapId"
 *                 ]
 *                 // . . .
 *             },
 *             {
 *                 "name": "httpsEndpointName",
 *                 "type": "https",
 *                 "port": 9909,
 *                 "startChain": "mainChain",
 *                 "maxContentLength": 4098,
 *                 "stackDepth": 5,
 *                 "certPath": "/home/sevenbits/workspace/smartactors-core_v2/ssl/cert.pem",
 *                 "keyPass": "123456",
 *                 "storePass": "123456",
 *                 "templates": [
 *                     "/messageMapId/:messageMapId/bar/:barId",
 *                     "/:messageMapId"
 *                 ]
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
        this.name = IOC.resolve(IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "endpoints");
        this.typeFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "type");
        this.portFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "port");
        this.startChainNameFieldName =
                IOC.resolve(IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "startChain");
        this.stackDepthFieldName =
                IOC.resolve(IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "stackDepth");
        this.maxContentLengthFieldName =
                IOC.resolve(IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "maxContentLength");
        this.endpointNameFieldName =
                IOC.resolve(IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "endpointName");
        this.queueFieldName =
                IOC.resolve(IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "queue");
    }

    @Override
    public void onLoadConfig(final IObject config) throws ConfigurationProcessingException {
        try {
            List<IObject> endpointObjects = (List<IObject>) config.getValue(name);
            IChainStorage chainStorage = IOC.resolve(IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(),
                    IChainStorage.class.getCanonicalName()));
            IQueue<ITask> queue = IOC.resolve(Keys.getOrAdd("task_queue"));
            for (IObject endpoint : endpointObjects) {
                // TODO: 25.07.16 remove stack depth from endpoint config
                String type = (String) endpoint.getValue(typeFieldName);
                String startChainName = (String) endpoint.getValue(startChainNameFieldName);
                Object mapId = IOC.resolve(Keys.getOrAdd("chain_id_from_map_name"), startChainName);
                VersionManager.setCurrentMessage(null);
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
    public void onRevertConfig(final IObject config) throws ConfigurationProcessingException {
        // ToDo: write corresponding revert code
    }

    @Override
    public IFieldName getSectionName() {
        return name;
    }
}
