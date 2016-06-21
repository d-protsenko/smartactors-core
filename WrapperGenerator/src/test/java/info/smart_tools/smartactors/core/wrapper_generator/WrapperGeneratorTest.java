package info.smart_tools.smartactors.core.wrapper_generator;


import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.ds_object.FieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iwrapper_generator.IWrapperGenerator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        binding.put("getBoolValue", "message.BoolValue");
        binding.put("setBoolValue", "response.BoolValue");
        binding.put("getWrappedIObject", "message.WrappedIObject");
        binding.put("setWrappedIObject", "response.WrappedIObject");
        binding.put("getIObject", "message.IObject");
        binding.put("setIObject", "response.IObject");
        binding.put("getStringIInnerMap", "context.StringIInnerMap");
        binding.put("setStringIInnerMap", "context.StringIInnerMap");
        IWrapper inst = wg.generate(IWrapper.class, binding);

        IObject message = mock(IObject.class);
        IObject context = mock(IObject.class);
        IObject response = mock(IObject.class);

        //List<Integer> list = new ArrayList<Integer>(){{add(1);add(3);}};

        ((IObjectWrapper)inst).init(message, context, response);
        inst.getListOfInt();


    }
}

