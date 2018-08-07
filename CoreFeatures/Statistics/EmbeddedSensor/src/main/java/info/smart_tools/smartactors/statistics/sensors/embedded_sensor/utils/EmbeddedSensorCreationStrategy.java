package info.smart_tools.smartactors.statistics.sensors.embedded_sensor.utils;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainModificationException;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.statistics.sensors.embedded_sensor.EmbeddedSensorHandle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A strategy that creates an embedded sensor.
 *
 * <pre>
 *     {
 *         "embed": [
 *             {
 *                 "step": 0,
 *                 "dependency": "save timestamp receiver"      // Save time at step 0
 *             },
 *             {
 *                 "step": 10,
 *                 "dependency": null                           // Defaults to "embedded sensor receiver"
 *             }
 *         ],
 *         "args": {
 *             ...
 *         },
 *         "chain": "the_very_interesting_chain"
 *     }
 * </pre>
 */
public class EmbeddedSensorCreationStrategy implements IResolveDependencyStrategy {
    private static final int CHAIN_ID_STRATEGY_ARGUMENT_INDEX = 0;
    private static final int CONFIG_STRATEGY_ARGUMENT_INDEX = 1;

    private static final String SENSOR_RECEIVER_REPLACEMENT_DEPENDENCY = "prepend sensor receiver";
    private static final String RECEIVER_REPLACEMENT_MODIFICATION_DEPENDENCY = "chain modification: replace receivers";
    private static final String DEFAULT_RECEIVER_DEPENDENCY = "embedded sensor receiver";

    private final IFieldName argsFieldName;
    private final IFieldName stepFieldName;
    private final IFieldName dependencyFieldName;
    private final IFieldName statisticsChainFieldName;
    private final IFieldName replacementsFieldName;
    private final IFieldName modificationFieldName;
    private final IFieldName embedFieldName;
    private final IFieldName chainFieldName;

    /**
     * The constructor.
     *
     * @throws ResolutionException if error occurs resolving any dependencies
     */
    public EmbeddedSensorCreationStrategy()
            throws ResolutionException {
        argsFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "args");
        stepFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "step");
        dependencyFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "dependency");
        statisticsChainFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "statisticsChain");
        replacementsFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "replacements");
        modificationFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "modification");
        embedFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "embed");
        chainFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "chain");
    }

    @Override
    public <T> T resolve(final Object... args) throws ResolveDependencyStrategyException {
        try {
            Object statisticsChainId = args[CHAIN_ID_STRATEGY_ARGUMENT_INDEX];
            IObject conf = (IObject) args[CONFIG_STRATEGY_ARGUMENT_INDEX];

            IObject commonArgs = (IObject) conf.getValue(argsFieldName);
            Collection<IObject> embedConf = (Collection<IObject>) conf.getValue(embedFieldName);
            List<IObject> replacements = new ArrayList<>(embedConf.size());

            Object targetChainId = IOC.resolve(Keys.getOrAdd("chain_id_from_map_name"), conf.getValue(chainFieldName));
            IChainStorage chainStorage = IOC.resolve(Keys.getOrAdd(IChainStorage.class.getCanonicalName()));

            for (IObject embedItemConf : embedConf) {
                IObject itemArgs = (IObject) embedItemConf.getValue(argsFieldName);
                itemArgs = (itemArgs == null) ? commonArgs : itemArgs;
                final Object itemDependency = embedItemConf.getValue(dependencyFieldName);

                itemArgs.setValue(statisticsChainFieldName, statisticsChainId);

                IMessageReceiver sensor = IOC.resolve(
                        IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(),
                                (itemDependency == null) ? DEFAULT_RECEIVER_DEPENDENCY : itemDependency),
                            itemArgs
                        );

                IObject replacement = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"));

                replacement.setValue(stepFieldName, embedItemConf.getValue(stepFieldName));
                replacement.setValue(dependencyFieldName, SENSOR_RECEIVER_REPLACEMENT_DEPENDENCY);
                replacement.setValue(argsFieldName, sensor);

                replacements.add(replacement);
            }

            IObject modification = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"));

            modification.setValue(modificationFieldName, RECEIVER_REPLACEMENT_MODIFICATION_DEPENDENCY);
            modification.setValue(replacementsFieldName, replacements);

            Object modId = chainStorage.update(targetChainId, modification);

            return (T) new EmbeddedSensorHandle(chainStorage, targetChainId, modId);
        } catch (ReadValueException | ChangeValueException | InvalidArgumentException | ResolutionException | ChainNotFoundException
                | ChainModificationException e) {
            throw new ResolveDependencyStrategyException(e);
        }
    }
}
