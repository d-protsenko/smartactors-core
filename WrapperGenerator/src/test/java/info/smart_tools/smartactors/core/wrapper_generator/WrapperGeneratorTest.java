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
        IObject binding = new DSObject();

        IObject getIntValue = new DSObject("{\n" +
                "\t\"ValueName\":    \"IntValue\",\n" +
                "\t\"MethodType\":   \"get\",\n" +
                "\t\"Resource\":     \"message\",\n" +
                "\t\"UseStrategy\":  \"\",\n" +
                "\t\"CheckWrapper\": false\n" +
                "}");
        IObject setIntValue = new DSObject("{\n" +
                "\t\"ValueName\":    \"IntValue\",\n" +
                "\t\"MethodType\":   \"set\",\n" +
                "\t\"Resource\":     \"response\",\n" +
                "\t\"UseStrategy\":  \"\",\n" +
                "\t\"CheckWrapper\": false\n" +
                "}");
        IObject getStringValue = new DSObject("{\n" +
                "\t\"ValueName\":    \"StringValue\",\n" +
                "\t\"MethodType\":   \"get\",\n" +
                "\t\"Resource\":     \"message\",\n" +
                "\t\"UseStrategy\":  \"\",\n" +
                "\t\"CheckWrapper\": false\n" +
                "}");
        IObject setStringValue = new DSObject("{\n" +
                "\t\"ValueName\":    \"StringValue\",\n" +
                "\t\"MethodType\":   \"set\",\n" +
                "\t\"Resource\":     \"response\",\n" +
                "\t\"UseStrategy\":  \"\",\n" +
                "\t\"CheckWrapper\": false\n" +
                "}");
        IObject getTestClassValue = new DSObject("{\n" +
                "\t\"ValueName\":    \"TestClassValue\",\n" +
                "\t\"MethodType\":   \"get\",\n" +
                "\t\"Resource\":     \"message\",\n" +
                "\t\"UseStrategy\":  \"ToTestClass\",\n" +
                "\t\"CheckWrapper\": false\n" +
                "}");
        IObject setTestClassValue = new DSObject("{\n" +
                "\t\"ValueName\":    \"TestClassValue\",\n" +
                "\t\"MethodType\":   \"set\",\n" +
                "\t\"Resource\":     \"response\",\n" +
                "\t\"UseStrategy\":  \"\",\n" +
                "\t\"CheckWrapper\": false\n" +
                "}");
        IObject getListOfInt = new DSObject("{\n" +
                "\t\"ValueName\":    \"ListOfInt\",\n" +
                "\t\"MethodType\":   \"get\",\n" +
                "\t\"Resource\":     \"message\",\n" +
                "\t\"UseStrategy\":  \"ToListOfInt\",\n" +
                "\t\"CheckWrapper\": false\n" +
                "}");
        IObject setListOfInt = new DSObject("{\n" +
                "\t\"ValueName\":    \"ListOfInt\",\n" +
                "\t\"MethodType\":   \"set\",\n" +
                "\t\"Resource\":     \"response\",\n" +
                "\t\"UseStrategy\":  \"\",\n" +
                "\t\"CheckWrapper\": false\n" +
                "}");
        IObject getListOfString = new DSObject("{\n" +
                "\t\"ValueName\":    \"ListOfString\",\n" +
                "\t\"MethodType\":   \"get\",\n" +
                "\t\"Resource\":     \"message\",\n" +
                "\t\"UseStrategy\":  \"\",\n" +
                "\t\"CheckWrapper\": false\n" +
                "}");
        IObject setListOfString = new DSObject("{\n" +
                "\t\"ValueName\":    \"ListOfString\",\n" +
                "\t\"MethodType\":   \"set\",\n" +
                "\t\"Resource\":     \"response\",\n" +
                "\t\"UseStrategy\":  \"\",\n" +
                "\t\"CheckWrapper\": false\n" +
                "}");
        IObject getListOfTestClasses = new DSObject("{\n" +
                "\t\"ValueName\":    \"ListOfTestClasses\",\n" +
                "\t\"MethodType\":   \"get\",\n" +
                "\t\"Resource\":     \"message\",\n" +
                "\t\"UseStrategy\":  \"ToListOfTestClasses\",\n" +
                "\t\"CheckWrapper\": false\n" +
                "}");
        IObject setListOfTestClasses = new DSObject("{\n" +
                "\t\"ValueName\":    \"ListOfTestClasses\",\n" +
                "\t\"MethodType\":   \"set\",\n" +
                "\t\"Resource\":     \"response\",\n" +
                "\t\"UseStrategy\":  \"\",\n" +
                "\t\"CheckWrapper\": false\n" +
                "}");
        IObject getBoolValue = new DSObject("{\n" +
                "\t\"ValueName\":    \"BoolValue\",\n" +
                "\t\"MethodType\":   \"get\",\n" +
                "\t\"Resource\":     \"message\",\n" +
                "\t\"UseStrategy\":  \"\",\n" +
                "\t\"CheckWrapper\": false\n" +
                "}");
        IObject setBoolValue = new DSObject("{\n" +
                "\t\"ValueName\":    \"BoolValue\",\n" +
                "\t\"MethodType\":   \"set\",\n" +
                "\t\"Resource\":     \"response\",\n" +
                "\t\"UseStrategy\":  \"\",\n" +
                "\t\"CheckWrapper\": false\n" +
                "}");
        IObject getWrappedIObject = new DSObject("{\n" +
                "\t\"ValueName\":    \"WrappedIObject\",\n" +
                "\t\"MethodType\":   \"get\",\n" +
                "\t\"Resource\":     \"message\",\n" +
                "\t\"UseStrategy\":  \"\",\n" +
                "\t\"CheckWrapper\": false\n" +
                "}");
        IObject setWrappedIObject = new DSObject("{\n" +
                "\t\"ValueName\":    \"WrappedIObject\",\n" +
                "\t\"MethodType\":   \"set\",\n" +
                "\t\"Resource\":     \"response\",\n" +
                "\t\"UseStrategy\":  \"\",\n" +
                "\t\"CheckWrapper\": false\n" +
                "}");
        IObject getIObject = new DSObject("{\n" +
                "\t\"ValueName\":    \"IObject\",\n" +
                "\t\"MethodType\":   \"get\",\n" +
                "\t\"Resource\":     \"message\",\n" +
                "\t\"UseStrategy\":  \"\",\n" +
                "\t\"CheckWrapper\": false\n" +
                "}");
        IObject setIObject = new DSObject("{\n" +
                "\t\"ValueName\":    \"IObject\",\n" +
                "\t\"MethodType\":   \"set\",\n" +
                "\t\"Resource\":     \"response\",\n" +
                "\t\"UseStrategy\":  \"\",\n" +
                "\t\"CheckWrapper\": false\n" +
                "}");
        IObject getStringIInnerMap = new DSObject("{\n" +
                "\t\"ValueName\":    \"StringIInnerMap\",\n" +
                "\t\"MethodType\":   \"get\",\n" +
                "\t\"Resource\":     \"context\",\n" +
                "\t\"UseStrategy\":  \"\",\n" +
                "\t\"CheckWrapper\": false\n" +
                "}");
        IObject setStringIInnerMap = new DSObject("{\n" +
                "\t\"ValueName\":    \"StringIInnerMap\",\n" +
                "\t\"MethodType\":   \"set\",\n" +
                "\t\"Resource\":     \"context\",\n" +
                "\t\"UseStrategy\":  \"\",\n" +
                "\t\"CheckWrapper\": false\n" +
                "}");
        binding.setValue(new FieldName("getIntValue"), getIntValue);
        binding.setValue(new FieldName("setIntValue"), setIntValue);
        binding.setValue(new FieldName("getStringValue"), getStringValue);
        binding.setValue(new FieldName("setStringValue"), setStringValue);
        binding.setValue(new FieldName("getTestClassValue"), getTestClassValue);
        binding.setValue(new FieldName("setTestClassValue"), setTestClassValue);
        binding.setValue(new FieldName("getListOfInt"), getListOfInt);
        binding.setValue(new FieldName("setListOfInt"), setListOfInt);
        binding.setValue(new FieldName("getListOfString"), getListOfString);
        binding.setValue(new FieldName("setListOfString"), setListOfString);
        binding.setValue(new FieldName("getListOfTestClasses"), getListOfTestClasses);
        binding.setValue(new FieldName("setListOfTestClasses"), setListOfTestClasses);
        binding.setValue(new FieldName("getBoolValue"), getBoolValue);
        binding.setValue(new FieldName("setBoolValue"), setBoolValue);
        binding.setValue(new FieldName("getWrappedIObject"), getWrappedIObject);
        binding.setValue(new FieldName("setWrappedIObject"), setWrappedIObject);
        binding.setValue(new FieldName("getIObject"), getIObject);
        binding.setValue(new FieldName("setIObject"), setIObject);
        binding.setValue(new FieldName("getStringIInnerMap"), getStringIInnerMap);
        binding.setValue(new FieldName("setStringIInnerMap"), setStringIInnerMap);

        IWrapper inst = wg.generate(IWrapper.class, binding);

        IObject message = mock(IObject.class);
        IObject context = mock(IObject.class);
        IObject response = mock(IObject.class);

        //List<Integer> list = new ArrayList<Integer>(){{add(1);add(3);}};

        ((IObjectWrapper)inst).init(message, context, response);
        inst.getListOfInt();


    }
}

