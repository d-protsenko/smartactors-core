package info.smart_tools.smartactors.plugin.datetime_formatter_strategy;

import info.smart_tools.smartactors.feature_loading_system.bootstrap.Bootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.exception.ProcessExecutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.ioc_plugins.ioc_simple_container_plugin.PluginIOCSimpleContainer;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;

public class PluginDateTimeFormatterTest {

    @BeforeClass
    public static void prepareIOC() throws PluginException, ProcessExecutionException, InvalidArgumentException {

        Bootstrap bootstrap = new Bootstrap();
        new PluginIOCSimpleContainer(bootstrap).load();
        new PluginIOCKeys(bootstrap).load();
        new PluginDateTimeFormatter(bootstrap).load();
        bootstrap.start();
    }

    @Test
    public void ShouldReturnFormatter() throws Exception {

        LocalDateTime now = LocalDateTime.of(2016, 8, 18, 0, 0);
        DateTimeFormatter formatter = IOC.resolve(Keys.getOrAdd("datetime_formatter"));
        assertEquals(formatter.format(now), "08-18-2016 00:00:00");
    }
}
