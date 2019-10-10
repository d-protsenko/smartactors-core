package info.smart_tools.smartactors.ioc_strategy_pack_plugins.datetime_formatter_strategy_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap.Bootstrap;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.exception.ProcessExecutionException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.exception.RevertProcessExecutionException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.helpers.IOCInitializer.IOCInitializer;
import info.smart_tools.smartactors.ioc.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PluginDateTimeFormatterTest extends IOCInitializer {
    static Bootstrap bootstrap;

    @Override
    protected void registry(String... strategyNames) throws Exception {
        registryStrategies("ifieldname strategy", "iobject strategy");
    }

    @Before
    public void prepareIOC()
            throws Exception {

        IOC.register(
                Keys.getKeyByName("bootstrap item"),
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> new BootstrapItem((String) args[0])
                )
        );
        bootstrap = new Bootstrap();
        bootstrap.add(new BootstrapItem("IOC").process(()->{}));
        new PluginDateTimeFormatter(bootstrap).load();
        bootstrap.start();
    }

    @Test
    public void ShouldReturnFormatter() throws Exception {

        LocalDateTime now = LocalDateTime.of(2016, 8, 18, 0, 0);
        DateTimeFormatter formatter = IOC.resolve(Keys.getKeyByName("datetime_formatter"));
        assertEquals(formatter.format(now), "08-18-2016 00:00:00");
    }

    @Test
    public void ShouldRevertPlugin() throws Exception {

        bootstrap.revert();

        LocalDateTime now = LocalDateTime.of(2016, 8, 18, 0, 0);
        try {
            IOC.resolve(Keys.getKeyByName("datetime_formatter"));
            fail();
        } catch(ResolutionException e) { }

        bootstrap.start();
    }
}
