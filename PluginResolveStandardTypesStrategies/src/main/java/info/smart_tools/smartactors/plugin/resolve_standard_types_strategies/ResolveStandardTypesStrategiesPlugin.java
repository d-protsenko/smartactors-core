package info.smart_tools.smartactors.plugin.resolve_standard_types_strategies;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.resolve_by_type_strategy.ResolveByTypeStrategy;
import info.smart_tools.smartactors.core.resolve_standard_types_strategies.to_bigdecimal.DoubleToBigDecimalResolveDependencyStrategy;
import info.smart_tools.smartactors.core.resolve_standard_types_strategies.to_bigdecimal.StringToBigDecimalResolveDependencyStrategy;
import info.smart_tools.smartactors.core.resolve_standard_types_strategies.to_date_strategies.StringToDateResolveDependencyStrategy;
import info.smart_tools.smartactors.core.resolve_standard_types_strategies.to_integer_strategies.DoubleToIntResolveDependencyStrategy;
import info.smart_tools.smartactors.core.resolve_standard_types_strategies.to_integer_strategies.StringToIntResolveDependencyStrategy;
import info.smart_tools.smartactors.core.resolve_standard_types_strategies.to_string_strategies.*;
import info.smart_tools.smartactors.core.wrapper_generator.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

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
                            // to String strategies
                            IKey stringKey = Keys.getOrAdd(String.class.getCanonicalName() + "convert");
                            ResolveByTypeStrategy stringStrategy = new ResolveByTypeStrategy();

                            stringStrategy.register(Keys.getOrAdd(Object.class.getCanonicalName()),
                                    new ObjectToStringResolveDependencyStrategy());
                            stringStrategy.register(Keys.getOrAdd(int.class.getCanonicalName()),
                                    new IntToStringResolveDependencyStrategy());
                            stringStrategy.register(Keys.getOrAdd(long.class.getCanonicalName()),
                                    new LongToStringResolveDependencyStrategy());
                            stringStrategy.register(Keys.getOrAdd(float.class.getCanonicalName()),
                                    new FloatToStringResolveDependencyStrategy());
                            stringStrategy.register(Keys.getOrAdd(double.class.getCanonicalName()),
                                    new DoubleToStringResolveDependencyStrategy());
                            stringStrategy.register(Keys.getOrAdd(boolean.class.getCanonicalName()),
                                    new BooleanToStringResolveDependencyStrategy());
                            stringStrategy.register(Keys.getOrAdd(byte.class.getCanonicalName()),
                                    new ByteToStringResolveDependencyStrategy());
                            stringStrategy.register(Keys.getOrAdd(short.class.getCanonicalName()),
                                    new ShortToStringResolveDependencyStrategy());
                            stringStrategy.register(Keys.getOrAdd(char.class.getCanonicalName()),
                                    new CharToStringResolveDependencyStrategy());

                            IOC.register(stringKey, stringStrategy);

                            // to Integer strategies
                            IKey integerKey = Keys.getOrAdd(Integer.class.getCanonicalName() + "convert");
                            ResolveByTypeStrategy integerStrategy = new ResolveByTypeStrategy();

                            integerStrategy.register(Keys.getOrAdd(String.class.getCanonicalName()),
                                    new StringToIntResolveDependencyStrategy());
                            integerStrategy.register(Keys.getOrAdd(Double.class.getCanonicalName()),
                                    new DoubleToIntResolveDependencyStrategy());

                            IOC.register(integerKey, integerStrategy);

                            // to BigDecimal strategies
                            IKey bigDecimalKey = Keys.getOrAdd(BigDecimal.class.getCanonicalName() + "convert");
                            ResolveByTypeStrategy bigDecimalStrategy = new ResolveByTypeStrategy();

                            bigDecimalStrategy.register(Keys.getOrAdd(String.class.getCanonicalName()),
                                    new StringToBigDecimalResolveDependencyStrategy());
                            bigDecimalStrategy.register(Keys.getOrAdd(Double.class.getCanonicalName()),
                                    new DoubleToBigDecimalResolveDependencyStrategy());

                            IOC.register(bigDecimalKey, bigDecimalStrategy);

                            // to LocalDateTyme strategies
                            IKey localDateTimeKey = Keys.getOrAdd(LocalDateTime.class.getCanonicalName() + "convert");
                            ResolveByTypeStrategy localDateTimeStrategy = new ResolveByTypeStrategy();

                            localDateTimeStrategy.register(Keys.getOrAdd(String.class.getCanonicalName()),
                                    new StringToDateResolveDependencyStrategy());

                            IOC.register(localDateTimeKey, localDateTimeStrategy);
                        } catch (RegistrationException | ResolutionException e) {
                            throw new RuntimeException(e);
                        }
                    });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load ResolveStandardTypesStrategies plugin", e);
        }
    }
}
