package info.smart_tools.smartactors.core.wrapper_generator;

import info.smart_tools.smartactors.core.iobject.IObject;

import java.util.List;
import java.util.Map;

import info.smart_tools.smartactors.core.wrapper_generator.IInnerWrapper;
import info.smart_tools.smartactors.core.wrapper_generator.TestClass;

/**
 * Test interface for generation wrapper
 */
public interface IWrapper {

    List<Map<String,List<TestClass>>> getIntValue();

    void setIntValue(int a);

    String getStringValue();

    void setStringValue(String s);

    TestClass getTestClass();

    void setTestClass(TestClass t);

    List<Integer> getListOfInt();

    void setListOfInt(List<Integer> list);

    List<String> getListOfString();

    void setListOfString(List<String> list);

    List<TestClass> getListOfTestClasses();

    void setListOfTestClasses(List<TestClass> list);

    Boolean isBoolValue();

    void setBoolValue(boolean value);

    Boolean hasValue();

    IInnerWrapper getWrappedIObject();

    void setWrappedIObject(IInnerWrapper wrappedIObject);

    IObject getIObject();

    void setIObject(IObject iObject);

    Integer countCValue();
}
