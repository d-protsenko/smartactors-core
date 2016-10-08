package info.smart_tools.smartactors.core.standard_config_sections;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.http_endpoint.http_response_handler.HttpResponseHandler;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.endpoint.interfaces.iresponse_handler.IResponseHandler;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;

/**
 * Creates endpoints using configuration.
 * <p>
 * Expects the following configuration format:
 * <p>
 * <pre>
 * "client":  {
 *      "startChain": "mainChain",
 *      "stackDepth": 5 (temporarily)
 * }
 */
public class ClientSectionProcessingStrategy implements ISectionStrategy {

    private IFieldName name, startChainNameFieldName, queueFieldName, stackDepthFieldName;

    /**
     * Constructor
     *
     * @throws ResolutionException if there are problems on resolving IFieldName
     */
    ClientSectionProcessingStrategy() throws ResolutionException {
        this.name = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "client");
        this.startChainNameFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "startChain");
        this.queueFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "queue");
        this.stackDepthFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "stackDepth");
    }

    @Override
    public void onLoadConfig(final IObject config) throws ConfigurationProcessingException {
        try {
            IObject clientObject = (IObject) config.getValue(name);
            IChainStorage chainStorage = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(),
                    IChainStorage.class.getCanonicalName()));
            IQueue<ITask> queue = IOC.resolve(Keys.getOrAdd("task_queue"));
            String startChainName = (String) clientObject.getValue(startChainNameFieldName);
            Object mapId = IOC.resolve(Keys.getOrAdd("chain_id_from_map_name"), startChainName);
            IReceiverChain chain = chainStorage.resolve(mapId);
            clientObject.setValue(startChainNameFieldName, chain);
            clientObject.setValue(queueFieldName, queue);
            Integer stackDepth = (Integer) clientObject.getValue(stackDepthFieldName);
            clientObject.setValue(stackDepthFieldName, stackDepth);
            IOC.register(Keys.getOrAdd("responseHandlerConfiguration"), new SingletonStrategy(clientObject));
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new ConfigurationProcessingException("Error occurred loading \"client\" configuration section.", e);
        } catch (ResolutionException e) {
            throw new ConfigurationProcessingException("Error occurred resolving \"endpoint\".", e);
        } catch (ChainNotFoundException e) {
            throw new ConfigurationProcessingException("Error occurred resolving \"chain\".", e);
        } catch (ChangeValueException | RegistrationException e) {
            throw new ConfigurationProcessingException("Error occurred registering \"client\".", e);
        }
    }

    @Override
    public IFieldName getSectionName() {
        return name;
    }
}
