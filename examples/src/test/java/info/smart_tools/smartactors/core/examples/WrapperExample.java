package info.smart_tools.smartactors.core.examples;

import info.smart_tools.smartactors.core.bootstrap.Bootstrap;
import info.smart_tools.smartactors.core.ibootstrap.exception.ProcessExecutionException;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.wds_object.WDSObject;
import info.smart_tools.smartactors.plugin.dsobject.PluginDSObject;
import info.smart_tools.smartactors.plugin.ifield.IFieldPlugin;
import info.smart_tools.smartactors.plugin.ifieldname.IFieldNamePlugin;
import info.smart_tools.smartactors.plugin.ioc_keys.PluginIOCKeys;
import info.smart_tools.smartactors.plugin.ioc_simple_container.PluginIOCSimpleContainer;
import info.smart_tools.smartactors.plugin.resolve_standard_types_strategies.ResolveStandardTypesStrategiesPlugin;
import info.smart_tools.smartactors.plugin.wds_object.PluginWDSObject;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Some examples to use Wrapper.
 */
public class WrapperExample {

    private static IKey iObjectKey;
    private static IKey iFieldKey;

    @BeforeClass
    public static void loadPlugins() throws PluginException, ProcessExecutionException, ResolutionException {
        Bootstrap bootstrap = new Bootstrap();
        new PluginIOCSimpleContainer(bootstrap).load();
        new PluginIOCKeys(bootstrap).load();
        new IFieldNamePlugin(bootstrap).load();
        new IFieldPlugin(bootstrap).load();
        new PluginDSObject(bootstrap).load();
        new ResolveStandardTypesStrategiesPlugin(bootstrap).load();
        new PluginWDSObject(bootstrap).load();
        bootstrap.start();

        iObjectKey = Keys.getOrAdd(IObject.class.getCanonicalName());
        iFieldKey = Keys.getOrAdd(IField.class.getCanonicalName());
    }

    @Test
    public void testWDSObject() throws ResolutionException, ReadValueException, ChangeValueException, InvalidArgumentException {
        IObject config = IOC.resolve(iObjectKey,
                "{" +
                "\"in_getName\": \"message/personName\"," +
                "\"out_setGreeting\": \"response/greeting\"" +
                "}");
        WDSObject wrapper = IOC.resolve(Keys.getOrAdd(WDSObject.class.getCanonicalName()), config);

        IObject environment = IOC.resolve(iObjectKey,
                "{" +
                "\"message\": { \"personName\": \"Ivan\" }," +
                "\"response\": {}" +
                "}");
        wrapper.init(environment);

        IField getNameField = IOC.resolve(iFieldKey, "in_getName");
        assertEquals("Ivan", getNameField.in(wrapper));

        IField setGreetingField = IOC.resolve(iFieldKey, "out_setGreeting");
        setGreetingField.out(wrapper, "Hello");

        IField responseField = IOC.resolve(iFieldKey, "response");
        IObject response = responseField.in(environment);
        IField greetingField = IOC.resolve(iFieldKey, "greeting");
        assertEquals("Hello", greetingField.in(response));
    }


}
