package info.smart_tools.smartactors.core.standard_config_sections;

import info.smart_tools.smartactors.core.HttpEndpoint;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.endpoint_handler.exceptions.EndpointException;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.core.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.core.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EndpointsSectionProcessingStrategyTest {

    private IQueue<ITask> taskQueue;
    private Object mapId;
    private IChainStorage chainStorage;
    private IReceiverChain receiverChain;

    @Before
    public void setUp() throws ScopeProviderException, RegistrationException, ResolutionException, InvalidArgumentException {
        taskQueue = mock(IQueue.class);
        mapId = mock(Object.class);
        receiverChain = mock(IReceiverChain.class);
        chainStorage = mock(IChainStorage.class);
        ScopeProvider.subscribeOnCreationNewScope(
                scope -> {
                    try {
                        scope.setValue(IOC.getIocKey(), new StrategyContainer());
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }
        );

        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope mainScope = ScopeProvider.getScope(keyOfMainScope);
        ScopeProvider.setCurrentScope(mainScope);
        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy()
        );

        IKey httpEndpointKey = Keys.getOrAdd(HttpEndpoint.class.getCanonicalName());

        IOC.register(httpEndpointKey,
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new HttpEndpoint((Integer) args[0],
                                        (Integer) args[1], (IScope) args[2],
                                        (IEnvironmentHandler) args[3],
                                        (IReceiverChain) args[4]
                                );
                            } catch (EndpointException e) {
                            }
                            return null;
                        }
                )
        );

        IKey iFieldNameKey = Keys.getOrAdd(IFieldName.class.getCanonicalName());

        IOC.register(iFieldNameKey,
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new FieldName((String) args[0]);
                            } catch (InvalidArgumentException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                )
        );

        IKey chainIdFromMapNameKey = Keys.getOrAdd("chain_id_from_map_name");
        IOC.register(chainIdFromMapNameKey,
                new SingletonStrategy(mapId));

        IKey taskQueueKey = Keys.getOrAdd("task_queue");
        IOC.register(taskQueueKey,
                new SingletonStrategy(taskQueue));

        IKey chainStorageKey = Keys.getOrAdd(IChainStorage.class.getCanonicalName());
        IOC.register(chainStorageKey,
                new SingletonStrategy(chainStorage));


    }

    @Test
    public void testLoadingConfig() throws InvalidArgumentException, ResolutionException, ConfigurationProcessingException, ChainNotFoundException {
        when(chainStorage.resolve(mapId)).thenReturn(receiverChain);
        DSObject config = new DSObject("\n" +
                "     {\n" +
                "         \"endpoints\": [\n" +
                "             {\n" +
                "                 \"endpointName\": \"enpointName\"," +
                "                 \"type\": \"http\",\n" +
                "                 \"port\": 8080,\n" +
                "                 \"startChain\": \"mainChain\",\n" +
                "                 \"maxContentLength\": 4098,\n" +
                "                 \"stackDepth\": 5\n" +
                "             }\n" +
                "         ]\n" +
                "     }");
        EndpointsSectionProcessingStrategy strategy = new EndpointsSectionProcessingStrategy();
        strategy.onLoadConfig(config);
    }
}
