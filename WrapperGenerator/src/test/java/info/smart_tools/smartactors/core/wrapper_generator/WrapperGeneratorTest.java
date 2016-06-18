package info.smart_tools.smartactors.core.wrapper_generator;


import info.smart_tools.smartactors.core.iwrapper_generator.IWrapperGenerator;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests for {@link WrapperGenerator}
 */
public class WrapperGeneratorTest {

    @Test
    public void check()
            throws Exception {
        IWrapperGenerator wg = new WrapperGenerator(null);
        Map<String, String> binding = new HashMap<>();
        binding.put("getIntValue", "message.IntValue");
        binding.put("setIntValue", "response.IntValue");
        binding.put("getStringValue", "message.StringValue");
        binding.put("setStringValue", "response.StringValue");
        binding.put("getTestClassValue", "message.TestClassValue");
        binding.put("setTestClassValue", "response.TestClassValue");
        binding.put("getListOfInt", "message.ListOfInt");
        binding.put("setListOfInt", "response.ListOfInt");
        binding.put("getListOfString", "message.ListOfString");
        binding.put("setListOfString", "response.ListOfString");
        binding.put("getListOfTestClasses", "message.ListOfTestClasses");
        binding.put("setListOfTestClasses", "response.ListOfTestClasses");
        binding.put("isBoolValue", "message.BoolValue");
        binding.put("setBoolValue", "response.BoolValue");
        binding.put("hasValue", "message.Value");
        binding.put("getWrappedIObject", "message.WrappedIObject");
        binding.put("setWrappedIObject", "response.WrappedIObject");
        binding.put("getIObject", "message.IObject");
        binding.put("setIObject", "response.IObject");
        binding.put("countCValue", "message.CValue");
        wg.generate(IWrapper.class, binding);
    }
}

