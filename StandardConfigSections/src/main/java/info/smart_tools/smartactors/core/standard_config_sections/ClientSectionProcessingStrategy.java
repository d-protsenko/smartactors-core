package info.smart_tools.smartactors.core.standard_config_sections;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.http_response_handler.HttpResponseHandler;
import info.smart_tools.smartactors.core.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.core.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.core.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.core.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.iresponse_handler.IResponseHandler;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

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
            IResponseHandler handler = new HttpResponseHandler(queue, stackDepth, chain);
            IOC.register(Keys.getOrAdd(IResponseHandler.class.getCanonicalName()), new SingletonStrategy(handler));
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new ConfigurationProcessingException("Error occurred loading \"endpoint\" configuration section.", e);
        } catch (ResolutionException e) {
            throw new ConfigurationProcessingException("Error occurred resolving \"endpoint\".", e);
        } catch (ChainNotFoundException e) {
            throw new ConfigurationProcessingException("Error occurred resolving \"chain\".", e);
        } catch (ChangeValueException e) {
            e.printStackTrace();
        } catch (RegistrationException e) {
        }
    }

    @Override
    public IFieldName getSectionName() {
        return name;
    }
}
