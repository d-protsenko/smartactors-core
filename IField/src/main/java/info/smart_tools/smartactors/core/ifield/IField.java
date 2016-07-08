package info.smart_tools.smartactors.core.ifield;

import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Support internal interface for wrappers
 *
 * @param <T> the return type for {@code in} method and type of inserting parameters for {@code out} method
 */
public interface IField<T> {

    /**
     * Apply rules and return result
     * @param env instance of {@link IObject} with data
     * @return instance of {@link T}
     * @throws ReadValueException if any errors occurred when iobject had been reading
     * @throws InvalidArgumentException if incoming arguments are incorrect
     * @throws ClassCastException when returning type doesn't match required type
     */
    T in(final IObject env)
            throws ReadValueException, InvalidArgumentException;

    /**
     * Apply rules to given argument without return result
     * @param env instance of {@link IObject} with data
     * @param in given argument
     * @throws ChangeValueException if any errors occurred when iObject had been changing
     * @throws InvalidArgumentException if incoming arguments are incorrect
     */
    void out(final IObject env, final T in)
            throws ChangeValueException, InvalidArgumentException;
}
