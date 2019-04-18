package info.smart_tools.smartactors.ioc_strategy_pack_plugins.resolve_standard_types_strategies_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.IAdditionDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.exception.AdditionDependencyStrategyException;
import info.smart_tools.smartactors.base.interfaces.iaction.IBiFunction;
import info.smart_tools.smartactors.base.interfaces.iaction.IFunction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.base.strategy.strategy_storage_with_cache_strategy.StrategyStorageWithCacheStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_bigdecimal.DoubleToBigDecimalResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_bigdecimal.FloatToBigDecimalResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_bigdecimal.IntegerToBigDecimalResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_bigdecimal.StringToBigDecimalResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_character_strategies.StringToCharacterResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_date_strategies.StringToDateResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_boolean_strategies.BooleanToPrimitiveResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_integer_strategies.DoubleToIntResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_integer_strategies.StringToIntResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_list_strategies.BooleanArrayToListResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_list_strategies.ByteArrayToListResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_list_strategies.CharArrayToListResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_list_strategies.DoubleArrayToListResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_list_strategies.FloatArrayToListResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_list_strategies.IntArrayToListResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_list_strategies.LongArrayToListResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_list_strategies.ObjectArrayToListResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_list_strategies.ShortArrayToListResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_self.ClassToClassResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_string_strategies.BooleanToStringResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_string_strategies.ByteToStringResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_string_strategies.CharToStringResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_string_strategies.DoubleToStringResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_string_strategies.FloatToStringResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_string_strategies.IntToStringResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_string_strategies.LongToStringResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_string_strategies.ObjectToStringResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_string_strategies.ShortToStringResolveDependencyStrategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Plugin.
 * Implements {@link IPlugin}
 * Load strategies for resolving standard types
 */
public class ResolveStandardTypesStrategiesPlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap element
     */
    public ResolveStandardTypesStrategiesPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {

        try {
            IBootstrapItem<String> item = new BootstrapItem("ResolveStandardTypesStrategiesPlugin");
            item
                    .after("IOC")
                    .process(() -> {
                        try {
                            IFunction argToKey = arg -> arg.getClass();
                            IBiFunction findValueByArgument = (map, arg) -> {
                                IResolveDependencyStrategy strategy = null;
                                for (Map.Entry<Class, IResolveDependencyStrategy> entry : ((Map<Class, IResolveDependencyStrategy>) map).entrySet()) {
                                    if (entry.getKey().isInstance(arg)) {
                                        strategy = entry.getValue();

                                        break;
                                    }
                                }
                                return strategy;
                            };

                            // to String strategies
                            IKey stringKey = Keys.getOrAdd(String.class.getCanonicalName() + "convert");
                            IKey expandableStrategyStringKey = Keys.getOrAdd("expandable_strategy#" + String.class.getCanonicalName());
                            IResolveDependencyStrategy stringStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IAdditionDependencyStrategy)stringStrategy).register(
                                    String.class,
                                    new ClassToClassResolveDependencyStrategy()
                            );
                            ((IAdditionDependencyStrategy)stringStrategy).register(
                                    Object.class,
                                    new ObjectToStringResolveDependencyStrategy()
                            );
                            ((IAdditionDependencyStrategy)stringStrategy).register(
                                    int.class,
                                    new IntToStringResolveDependencyStrategy()
                            );
                            ((IAdditionDependencyStrategy)stringStrategy).register(
                                    long.class,
                                    new LongToStringResolveDependencyStrategy()
                            );
                            ((IAdditionDependencyStrategy)stringStrategy).register(
                                    float.class,
                                    new FloatToStringResolveDependencyStrategy()
                            );
                            ((IAdditionDependencyStrategy)stringStrategy).register(
                                    double.class,
                                    new DoubleToStringResolveDependencyStrategy()
                            );
                            ((IAdditionDependencyStrategy)stringStrategy).register(
                                    boolean.class,
                                    new BooleanToStringResolveDependencyStrategy()
                            );
                            ((IAdditionDependencyStrategy)stringStrategy).register(
                                    byte.class,
                                    new ByteToStringResolveDependencyStrategy()
                            );
                            ((IAdditionDependencyStrategy)stringStrategy).register(
                                    short.class,
                                    new ShortToStringResolveDependencyStrategy()
                            );
                            ((IAdditionDependencyStrategy)stringStrategy).register(
                                    char.class,
                                    new CharToStringResolveDependencyStrategy()
                            );
                            IOC.register(stringKey, stringStrategy);
                            IOC.register(expandableStrategyStringKey, new SingletonStrategy(stringStrategy));

                            // to Character strategies
                            IKey characterKey = Keys.getOrAdd(Character.class.getCanonicalName() + "convert");
                            IKey expandableStrategyCharacterKey = Keys.getOrAdd("expandable_strategy#" + Character.class.getCanonicalName());
                            IResolveDependencyStrategy characterStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IAdditionDependencyStrategy) characterStrategy).register(
                                    Character.class,
                                    new ClassToClassResolveDependencyStrategy()
                            );
                            ((IAdditionDependencyStrategy) characterStrategy).register(
                                    String.class,
                                    new StringToCharacterResolveDependencyStrategy()
                            );
                            IOC.register(characterKey, characterStrategy);
                            IOC.register(expandableStrategyCharacterKey, new SingletonStrategy(characterStrategy));

                            // to boolean strategies
                            IKey booleanKey = Keys.getOrAdd(boolean.class.getCanonicalName() + "convert");
                            IKey expandableStrategyBooleanKey = Keys.getOrAdd("expandable_strategy#" + boolean.class.getCanonicalName());
                            IResolveDependencyStrategy booleanStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IAdditionDependencyStrategy) booleanStrategy).register(
                                    boolean.class,
                                    new ClassToClassResolveDependencyStrategy()
                            );
                            ((IAdditionDependencyStrategy) booleanStrategy).register(
                                    Boolean.class,
                                    new BooleanToPrimitiveResolveDependencyStrategy()
                            );
                            IOC.register(booleanKey, booleanStrategy);
                            IOC.register(expandableStrategyBooleanKey, new SingletonStrategy(booleanStrategy));

                            // to Integer strategies
                            IKey integerKey = Keys.getOrAdd(Integer.class.getCanonicalName() + "convert");
                            IKey expandableStrategyIntegerKey = Keys.getOrAdd("expandable_strategy#" + Integer.class.getCanonicalName());
                            IResolveDependencyStrategy integerStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IAdditionDependencyStrategy) integerStrategy).register(
                                    Integer.class,
                                    new ClassToClassResolveDependencyStrategy()
                            );
                            ((IAdditionDependencyStrategy) integerStrategy).register(
                                    String.class,
                                    new StringToIntResolveDependencyStrategy()
                            );
                            ((IAdditionDependencyStrategy) integerStrategy).register(
                                    Double.class,
                                    new DoubleToIntResolveDependencyStrategy()
                            );
                            IOC.register(integerKey, integerStrategy);
                            IOC.register(expandableStrategyIntegerKey, new SingletonStrategy(integerStrategy));

                            // to BigDecimal strategies
                            IKey bigDecimalKey = Keys.getOrAdd(BigDecimal.class.getCanonicalName() + "convert");
                            IKey expandableStrategyBigDecimalKey = Keys.getOrAdd("expandable_strategy#" + BigDecimal.class.getCanonicalName());
                            IResolveDependencyStrategy bigDecimalStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IAdditionDependencyStrategy) bigDecimalStrategy).register(
                                    BigDecimal.class,
                                    new ClassToClassResolveDependencyStrategy()
                            );
                            ((IAdditionDependencyStrategy) bigDecimalStrategy).register(
                                    String.class,
                                    new StringToBigDecimalResolveDependencyStrategy()
                            );
                            ((IAdditionDependencyStrategy) bigDecimalStrategy).register(
                                    Double.class,
                                    new DoubleToBigDecimalResolveDependencyStrategy()
                            );
                            ((IAdditionDependencyStrategy) bigDecimalStrategy).register(
                                    Float.class,
                                    new FloatToBigDecimalResolveDependencyStrategy()
                            );
                            ((IAdditionDependencyStrategy) bigDecimalStrategy).register(
                                    Integer.class,
                                    new IntegerToBigDecimalResolveDependencyStrategy()
                            );
                            IOC.register(bigDecimalKey, bigDecimalStrategy);
                            IOC.register(expandableStrategyBigDecimalKey, new SingletonStrategy(bigDecimalStrategy));

                            // to LocalDateTime strategies
                            IKey localDateTimeKey = Keys.getOrAdd(LocalDateTime.class.getCanonicalName() + "convert");
                            IKey expandableStrategyLocalDateTimeKey = Keys.getOrAdd("expandable_strategy#" + LocalDateTime.class.getCanonicalName());
                            IResolveDependencyStrategy localDateTimeStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IAdditionDependencyStrategy) localDateTimeStrategy).register(
                                    LocalDateTime.class,
                                    new ClassToClassResolveDependencyStrategy()
                            );
                            ((IAdditionDependencyStrategy) localDateTimeStrategy).register(
                                    String.class,
                                    new StringToDateResolveDependencyStrategy()
                            );
                            IOC.register(localDateTimeKey, localDateTimeStrategy);
                            IOC.register(expandableStrategyLocalDateTimeKey, new SingletonStrategy(localDateTimeStrategy));

                            // to list strategies
                            IKey listKey = Keys.getOrAdd(List.class.getCanonicalName() + "convert");
                            IKey expandableStrategyListKey = Keys.getOrAdd("expandable_strategy#" + List.class.getCanonicalName());
                            IResolveDependencyStrategy listStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IAdditionDependencyStrategy) listStrategy).register(boolean[].class, new BooleanArrayToListResolveDependencyStrategy());
                            ((IAdditionDependencyStrategy) listStrategy).register(byte[].class, new ByteArrayToListResolveDependencyStrategy());
                            ((IAdditionDependencyStrategy) listStrategy).register(char[].class, new CharArrayToListResolveDependencyStrategy());
                            ((IAdditionDependencyStrategy) listStrategy).register(double[].class, new DoubleArrayToListResolveDependencyStrategy());
                            ((IAdditionDependencyStrategy) listStrategy).register(float[].class, new FloatArrayToListResolveDependencyStrategy());
                            ((IAdditionDependencyStrategy) listStrategy).register(int[].class, new IntArrayToListResolveDependencyStrategy());
                            ((IAdditionDependencyStrategy) listStrategy).register(long[].class, new LongArrayToListResolveDependencyStrategy());
                            ((IAdditionDependencyStrategy) listStrategy).register(Object[].class, new ObjectArrayToListResolveDependencyStrategy());
                            ((IAdditionDependencyStrategy) listStrategy).register(short[].class, new ShortArrayToListResolveDependencyStrategy());
                            IOC.register(listKey, listStrategy);
                            IOC.register(expandableStrategyListKey, new SingletonStrategy(listStrategy));

                            //to int strategies
                            IKey intKey = Keys.getOrAdd(int.class.getCanonicalName() + "convert");
                            IKey expandableStrategyIntKey = Keys.getOrAdd("expandable_strategy#" + int.class.getCanonicalName());
                            IResolveDependencyStrategy intStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IAdditionDependencyStrategy) intStrategy).register(
                                    String.class,
                                    new StringToIntResolveDependencyStrategy()
                            );
                            ((IAdditionDependencyStrategy) intStrategy).register(
                                    Double.class,
                                    new DoubleToIntResolveDependencyStrategy()
                            );
                            IOC.register(intKey, intStrategy);
                            IOC.register(expandableStrategyIntKey, new SingletonStrategy(intStrategy));
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("ResolveStandardTypesStrategies plugin can't load: can't get ResolveStandardTypesStrategies key", e);
                        } catch (RegistrationException | AdditionDependencyStrategyException | InvalidArgumentException e) {
                            throw new ActionExecuteException("ResolveStandardTypesStrategies plugin can't load: can't register new strategy", e);
                        }
                    })
                    .revertProcess(() -> {
                        String itemName = "ResolveStandardTypesStrategiesPlugin";
                        String keyName = "";

                        try {
                            keyName = "expandable_strategy#" + int.class.getCanonicalName();
                            IOC.remove(Keys.getOrAdd(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = int.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.getOrAdd(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + List.class.getCanonicalName();
                            IOC.remove(Keys.getOrAdd(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = List.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.getOrAdd(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + LocalDateTime.class.getCanonicalName();
                            IOC.remove(Keys.getOrAdd(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = LocalDateTime.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.getOrAdd(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + BigDecimal.class.getCanonicalName();
                            IOC.remove(Keys.getOrAdd(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = BigDecimal.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.getOrAdd(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + Integer.class.getCanonicalName();
                            IOC.remove(Keys.getOrAdd(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = Integer.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.getOrAdd(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + boolean.class.getCanonicalName();
                            IOC.remove(Keys.getOrAdd(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = boolean.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.getOrAdd(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + Character.class.getCanonicalName();
                            IOC.remove(Keys.getOrAdd(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = Character.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.getOrAdd(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + String.class.getCanonicalName();
                            IOC.remove(Keys.getOrAdd(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = String.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.getOrAdd(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }
                    });

            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load ResolveStandardTypesStrategies plugin", e);
        }
    }
}
