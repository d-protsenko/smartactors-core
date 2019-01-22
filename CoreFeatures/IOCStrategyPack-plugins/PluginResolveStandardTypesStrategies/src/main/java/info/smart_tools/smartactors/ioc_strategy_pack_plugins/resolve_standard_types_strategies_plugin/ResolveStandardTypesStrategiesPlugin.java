package info.smart_tools.smartactors.ioc_strategy_pack_plugins.resolve_standard_types_strategies_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.istrategy_registration.IStrategyRegistration;
import info.smart_tools.smartactors.base.interfaces.istrategy_registration.exception.StrategyRegistrationException;
import info.smart_tools.smartactors.base.interfaces.iaction.IFunctionTwoArgs;
import info.smart_tools.smartactors.base.interfaces.iaction.IFunction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecutionException;
import info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy;
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
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_bigdecimal.*;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_bigdecimal.FloatToBigDecimalStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_bigdecimal.IntegerToBigDecimalStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_boolean_strategies.BooleanToPrimitiveStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_character_strategies.StringToCharacterStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_date_strategies.StringToDateStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_integer_strategies.DoubleToIntStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_integer_strategies.StringToIntStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_list_strategies.*;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_self.ClassToClassStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_string_strategies.*;

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
                            IFunctionTwoArgs findValueByArgument = (map, arg) -> {
                                IStrategy strategy = null;
                                for (Map.Entry<Class, IStrategy> entry : ((Map<Class, IStrategy>) map).entrySet()) {
                                    if (entry.getKey().isInstance(arg)) {
                                        strategy = entry.getValue();

                                        break;
                                    }
                                }
                                return strategy;
                            };

                            // to String strategies
                            IKey stringKey = Keys.resolveByName(String.class.getCanonicalName() + "convert");
                            IKey expandableStrategyStringKey = Keys.resolveByName("expandable_strategy#" + String.class.getCanonicalName());
                            IStrategy stringStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IStrategyRegistration)stringStrategy).register(
                                    String.class,
                                    new ClassToClassStrategy()
                            );
                            ((IStrategyRegistration)stringStrategy).register(
                                    Object.class,
                                    new ObjectToStringStrategy()
                            );
                            ((IStrategyRegistration)stringStrategy).register(
                                    int.class,
                                    new IntToStringStrategy()
                            );
                            ((IStrategyRegistration)stringStrategy).register(
                                    long.class,
                                    new LongToStringStrategy()
                            );
                            ((IStrategyRegistration)stringStrategy).register(
                                    float.class,
                                    new FloatToStringStrategy()
                            );
                            ((IStrategyRegistration)stringStrategy).register(
                                    double.class,
                                    new DoubleToStringStrategy()
                            );
                            ((IStrategyRegistration)stringStrategy).register(
                                    boolean.class,
                                    new BooleanToStringStrategy()
                            );
                            ((IStrategyRegistration)stringStrategy).register(
                                    byte.class,
                                    new ByteToStringStrategy()
                            );
                            ((IStrategyRegistration)stringStrategy).register(
                                    short.class,
                                    new ShortToStringStrategy()
                            );
                            ((IStrategyRegistration)stringStrategy).register(
                                    char.class,
                                    new CharToStringStrategy()
                            );
                            IOC.register(stringKey, stringStrategy);
                            IOC.register(expandableStrategyStringKey, new SingletonStrategy(stringStrategy));

                            // to Character strategies
                            IKey characterKey = Keys.resolveByName(Character.class.getCanonicalName() + "convert");
                            IKey expandableStrategyCharacterKey = Keys.resolveByName("expandable_strategy#" + Character.class.getCanonicalName());
                            IStrategy characterStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IStrategyRegistration) characterStrategy).register(
                                    Character.class,
                                    new ClassToClassStrategy()
                            );
                            ((IStrategyRegistration) characterStrategy).register(
                                    String.class,
                                    new StringToCharacterStrategy()
                            );
                            IOC.register(characterKey, characterStrategy);
                            IOC.register(expandableStrategyCharacterKey, new SingletonStrategy(characterStrategy));

                            // to boolean strategies
                            IKey booleanKey = Keys.resolveByName(boolean.class.getCanonicalName() + "convert");
                            IKey expandableStrategyBooleanKey = Keys.resolveByName("expandable_strategy#" + boolean.class.getCanonicalName());
                            IStrategy booleanStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IStrategyRegistration) booleanStrategy).register(
                                    boolean.class,
                                    new ClassToClassStrategy()
                            );
                            ((IStrategyRegistration) booleanStrategy).register(
                                    Boolean.class,
                                    new BooleanToPrimitiveStrategy()
                            );
                            IOC.register(booleanKey, booleanStrategy);
                            IOC.register(expandableStrategyBooleanKey, new SingletonStrategy(booleanStrategy));

                            // to Integer strategies
                            IKey integerKey = Keys.resolveByName(Integer.class.getCanonicalName() + "convert");
                            IKey expandableStrategyIntegerKey = Keys.resolveByName("expandable_strategy#" + Integer.class.getCanonicalName());
                            IStrategy integerStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IStrategyRegistration) integerStrategy).register(
                                    Integer.class,
                                    new ClassToClassStrategy()
                            );
                            ((IStrategyRegistration) integerStrategy).register(
                                    String.class,
                                    new StringToIntStrategy()
                            );
                            ((IStrategyRegistration) integerStrategy).register(
                                    Double.class,
                                    new DoubleToIntStrategy()
                            );
                            IOC.register(integerKey, integerStrategy);
                            IOC.register(expandableStrategyIntegerKey, new SingletonStrategy(integerStrategy));

                            // to BigDecimal strategies
                            IKey bigDecimalKey = Keys.resolveByName(BigDecimal.class.getCanonicalName() + "convert");
                            IKey expandableStrategyBigDecimalKey = Keys.resolveByName("expandable_strategy#" + BigDecimal.class.getCanonicalName());
                            IStrategy bigDecimalStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IStrategyRegistration) bigDecimalStrategy).register(
                                    BigDecimal.class,
                                    new ClassToClassStrategy()
                            );
                            ((IStrategyRegistration) bigDecimalStrategy).register(
                                    String.class,
                                    new StringToBigDecimalStrategy()
                            );
                            ((IStrategyRegistration) bigDecimalStrategy).register(
                                    Double.class,
                                    new DoubleToBigDecimalStrategy()
                            );
                            ((IStrategyRegistration) bigDecimalStrategy).register(
                                    Float.class,
                                    new FloatToBigDecimalStrategy()
                            );
                            ((IStrategyRegistration) bigDecimalStrategy).register(
                                    Integer.class,
                                    new IntegerToBigDecimalStrategy()
                            );
                            IOC.register(bigDecimalKey, bigDecimalStrategy);
                            IOC.register(expandableStrategyBigDecimalKey, new SingletonStrategy(bigDecimalStrategy));

                            // to LocalDateTime strategies
                            IKey localDateTimeKey = Keys.resolveByName(LocalDateTime.class.getCanonicalName() + "convert");
                            IKey expandableStrategyLocalDateTimeKey = Keys.resolveByName("expandable_strategy#" + LocalDateTime.class.getCanonicalName());
                            IStrategy localDateTimeStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IStrategyRegistration) localDateTimeStrategy).register(
                                    LocalDateTime.class,
                                    new ClassToClassStrategy()
                            );
                            ((IStrategyRegistration) localDateTimeStrategy).register(
                                    String.class,
                                    new StringToDateStrategy()
                            );
                            IOC.register(localDateTimeKey, localDateTimeStrategy);
                            IOC.register(expandableStrategyLocalDateTimeKey, new SingletonStrategy(localDateTimeStrategy));

                            // to list strategies
                            IKey listKey = Keys.resolveByName(List.class.getCanonicalName() + "convert");
                            IKey expandableStrategyListKey = Keys.resolveByName("expandable_strategy#" + List.class.getCanonicalName());
                            IStrategy listStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IStrategyRegistration) listStrategy).register(boolean[].class, new BooleanArrayToListStrategy());
                            ((IStrategyRegistration) listStrategy).register(byte[].class, new ByteArrayToListStrategy());
                            ((IStrategyRegistration) listStrategy).register(char[].class, new CharArrayToListStrategy());
                            ((IStrategyRegistration) listStrategy).register(double[].class, new DoubleArrayToListStrategy());
                            ((IStrategyRegistration) listStrategy).register(float[].class, new FloatArrayToListStrategy());
                            ((IStrategyRegistration) listStrategy).register(int[].class, new IntArrayToListStrategy());
                            ((IStrategyRegistration) listStrategy).register(long[].class, new LongArrayToListStrategy());
                            ((IStrategyRegistration) listStrategy).register(Object[].class, new ObjectArrayToListStrategy());
                            ((IStrategyRegistration) listStrategy).register(short[].class, new ShortArrayToListStrategy());
                            IOC.register(listKey, listStrategy);
                            IOC.register(expandableStrategyListKey, new SingletonStrategy(listStrategy));

                            //to int strategies
                            IKey intKey = Keys.resolveByName(int.class.getCanonicalName() + "convert");
                            IKey expandableStrategyIntKey = Keys.resolveByName("expandable_strategy#" + int.class.getCanonicalName());
                            IStrategy intStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
                            ((IStrategyRegistration) intStrategy).register(
                                    String.class,
                                    new StringToIntStrategy()
                            );
                            ((IStrategyRegistration) intStrategy).register(
                                    Double.class,
                                    new DoubleToIntStrategy()
                            );
                            IOC.register(intKey, intStrategy);
                            IOC.register(expandableStrategyIntKey, new SingletonStrategy(intStrategy));
                        } catch (ResolutionException e) {
                            throw new ActionExecutionException("ResolveStandardTypesStrategies plugin can't load: can't get ResolveStandardTypesStrategies key", e);
                        } catch (RegistrationException | StrategyRegistrationException | InvalidArgumentException e) {
                            throw new ActionExecutionException("ResolveStandardTypesStrategies plugin can't load: can't register new strategy", e);
                        }
                    })
                    .revertProcess(() -> {
                        String itemName = "ResolveStandardTypesStrategiesPlugin";
                        String keyName = "";

                        try {
                            keyName = "expandable_strategy#" + int.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = int.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + List.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = List.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + LocalDateTime.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = LocalDateTime.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + BigDecimal.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = BigDecimal.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + Integer.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = Integer.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + boolean.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = boolean.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + Character.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = Character.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "expandable_strategy#" + String.class.getCanonicalName();
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = String.class.getCanonicalName() + "convert";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }
                    });

            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load ResolveStandardTypesStrategies plugin", e);
        }
    }
}
