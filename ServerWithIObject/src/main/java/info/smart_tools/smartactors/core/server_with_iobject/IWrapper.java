package info.smart_tools.smartactors.core.server_with_iobject;

import info.smart_tools.smartactors.core.iobject.IObject;

import java.util.List;

/**
 * Demonstration interface for generation wrapper
 */
public interface IWrapper {

    /**
     * Getter for int value
     * @return instance of {@link Integer}
     */
    Integer getIntValue();

    /**
     * Setter for int value
     * @param a instance of {@link Integer}
     */
    void setIntValue(int a);

    /**
     * Getter for string value
     * @return instance of {@link String}
     */
    String getStringValue();

    /**
     * Setter for string value
     * @param s instance of {@link String}
     */
    void setStringValue(String s);

    /**
     * Getter for list of int
     * @return instance of {@link List<Integer>}
     */
    List<Integer> getListOfInt();

    /**
     * Setter for list of integer
     * @param list instance of {@link List<Integer>}
     */
    void setListOfInt(List<Integer> list);

    /**
     * Getter for list of string
     * @return instance of {@link List<String>}
     */
    List<String> getListOfString();

    /**
     * Stter for list string
     * @param list instance of {@link List<String>}
     */
    void setListOfString(List<String> list);

    /**
     * Getter for bool value
     * @return instance of {@link Boolean}
     */
    Boolean getBoolValue();

    /**
     * Setter for bool value
     * @param value instance of {@link Boolean}
     */
    void setBoolValue(boolean value);

    /**
     * Getter for IObject instance
     * @return instance of {@link IObject}
     */
    IObject getIObject();

    /**
     * Setter for IObject instance
     * @param iObject instance of {@link IObject}
     */
    void setIObject(IObject iObject);
}
