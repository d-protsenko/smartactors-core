package info.smart_tools.smartactors.core.wrapper_generator;

import info.smart_tools.smartactors.core.iobject.IObject;

import java.util.List;
import java.util.Map;

/**
 * Test interface for generation wrapper
 */
public interface IWrapper {

    Integer getIntValue();

    void setIntValue(int a);

    String getStringValue();

    void setStringValue(String s);

    TestClass getTestClassValue();

    void setTestClassValue(TestClass t);

    List<Integer> getListOfInt();

    void setListOfInt(List<Integer> list);

    List<String> getListOfString();

    void setListOfString(List<String> list);

    List<TestClass> getListOfTestClasses();

    void setListOfTestClasses(List<TestClass> list);

    Boolean getBoolValue();

    void setBoolValue(boolean value);

    IInnerWrapper getWrappedIObject();

    void setWrappedIObject(IInnerWrapper wrappedIObject);

    IObject getIObject();

    void setIObject(IObject iObject);

    Map<String, IInnerWrapper> getStringIInnerMap();

    void setStringIInnerMap(Map<String, IInnerWrapper> map);
}
