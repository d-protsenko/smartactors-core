package info.smart_tools.smartactors.core.imatcher;

import info.smart_tools.smartactors.core.exception.PatternMatchingException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Interface for matching objects.
 * Implementation should has a property - pattern object and overridden method - match
 */
public interface IMatcher {

    Boolean match(IObject obj) throws ReadValueException, InvalidArgumentException, PatternMatchingException;
}
