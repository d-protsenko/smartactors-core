package info.smart_tools.smartactors.core.icontainer_implementation;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IContainer;
import info.smart_tools.smartactors.core.ioc.exception.RegistrationException;
import info.smart_tools.smartactors.core.ioc.exception.ResolutionException;

/**
 * Implementation of {@link info.smart_tools.smartactors.core.ioc.IContainer}
 * <pre>
 * Implementation features:
 * - support scopes
 * </pre>
 */
public class Container implements IContainer {
    public Object resolve(IObject obj) throws ResolutionException {

        return null;
    }

    public void register(IObject obj) throws RegistrationException {

    }
}
