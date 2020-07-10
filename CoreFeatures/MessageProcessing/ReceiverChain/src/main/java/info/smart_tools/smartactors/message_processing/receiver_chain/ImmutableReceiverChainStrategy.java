package info.smart_tools.smartactors.message_processing.receiver_chain;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy;
import info.smart_tools.smartactors.base.interfaces.istrategy.exception.StrategyException;
import info.smart_tools.smartactors.class_management.interfaces.imodule.IModule;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.irouter.IRouter;
import info.smart_tools.smartactors.message_processing_interfaces.irouter.exceptions.RouteNotFoundException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.scope.iscope.IScope;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Strategy of creation of an {@link ImmutableReceiverChain}.
 *
 * Expected arguments:
 * - {@link Object} - identifier of the chain
 * - {@link IObject} describing the chain
 * - {@link IChainStorage} to look for exceptional chains
 * - {@link IRouter} to find the receivers
 * Expected format of description:
 * <pre>
 *     {
 *         "steps": [
 *             {                            // object describing a single step of message processing (a single receiver)
 *                                          // this object will be passed to {@link IMessageReceiver#receive} method
 *                                          // as second argument.
 *                 "target": "theTarget",   // (just for example) fields defining the receiver. concrete field names
 *                                          // depend on implementation of "receiver_id_from_iobject" strategy
 *             },
 *             {
 *                 . . .
 *             }
 *         ],
 *         "exceptional": [                 // exception chains in the order the exception classes should be checked
 *             {
 *                 "class": "java.lang.NullPointerException",   // exception class
 *                 "chain": "myExceptionalChain"                // exceptional chain name
 *             },
 *             {
 *                 "class": "org.my.Exception",
 *                 "chain": "myExceptionalChain2"
 *             }
 *         ]
 *     }
 * </pre>
 */
public class ImmutableReceiverChainStrategy implements IStrategy {
    private static final int CHAIN_ID_ARG_INDEX = 0;
    private static final int DESCRIPTION_ARG_INDEX = 1;
    private static final int ROUTER_ARG_INDEX = 2;
    private static final int SCOPE_ARG_INDEX = 3;
    private static final int MODULE_ARG_INDEX = 4;

    @Override
    public <T> T resolve(final Object... args) throws StrategyException {
        try {
            Object chainId = args[CHAIN_ID_ARG_INDEX];
            IObject description = (IObject) args[DESCRIPTION_ARG_INDEX];
            IRouter router = (IRouter) args[ROUTER_ARG_INDEX];
            IScope scope = (IScope) args[SCOPE_ARG_INDEX];
            IModule module = (IModule) args[MODULE_ARG_INDEX];

            IKey fieldNameKey = Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName");
            IKey receiverIdKey = Keys.getKeyByName("receiver_id_from_iobject");

            IFieldName stepsFieldName = IOC.resolve(fieldNameKey, "steps");
            IFieldName exceptionalChainsFieldName = IOC.resolve(fieldNameKey, "exceptional");
            IFieldName exceptionClassFieldName = IOC.resolve(fieldNameKey, "class");
            IFieldName exceptionChainNameFieldName = IOC.resolve(fieldNameKey, "chain");
            IFieldName exceptionAfterFieldName = IOC.resolve(fieldNameKey, "after");

            List chainSteps = (List) description.getValue(stepsFieldName);
            List exceptionalChains = (List) description.getValue(exceptionalChainsFieldName);

            IMessageReceiver[] receivers = new IMessageReceiver[chainSteps.size()];
            IObject[] arguments = new IObject[chainSteps.size()];

            for (int i = 0; i < chainSteps.size(); i++) {
                IObject step = (IObject) chainSteps.get(i);

                receivers[i] = router.route(IOC.resolve(receiverIdKey, step));
                arguments[i] = step;
            }

            LinkedHashMap<Class<? extends Throwable>, IObject> exceptionalChainNamesMap = new LinkedHashMap<>();

            for (Object chainDesc : exceptionalChains) {
                IObject desc = (IObject) chainDesc;


                Class<?> clazz = module.getClassLoader().loadClass(String.valueOf(desc.getValue(exceptionClassFieldName)));
                Object chainName = desc.getValue(exceptionChainNameFieldName);
                IAction<IMessageProcessingSequence> afterExceptionAction = IOC.resolve(
                        Keys.getKeyByName("afterExceptionAction#" + desc.getValue(exceptionAfterFieldName))
                );
                //Object afterExceptionAction = "afterExceptionAction#" + desc.getValue(exceptionAfterFieldName);
                IObject chainNameAndEnv = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"));
                chainNameAndEnv.setValue(exceptionChainNameFieldName, chainName);
                chainNameAndEnv.setValue(exceptionAfterFieldName, afterExceptionAction);
                exceptionalChainNamesMap.put((Class<? extends Throwable>) clazz, chainNameAndEnv);
            }

            return (T) new ImmutableReceiverChain(String.valueOf(chainId), description, receivers, arguments, exceptionalChainNamesMap, scope, module);
        } catch (ClassNotFoundException | ResolutionException | ReadValueException |
                RouteNotFoundException | ChangeValueException | InvalidArgumentException e) {
            throw new StrategyException(e);
        }
    }
}
