package info.smart_tools.smartactors.core.ichain_storage.exceptions;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for {@link ChainNotFoundException}.
 */
public class ChainNotFoundExceptionTest {
    @Test(expected = ChainNotFoundException.class)
    public void checkConstruction()
            throws ChainNotFoundException {
        ChainNotFoundException exception = new ChainNotFoundException("test");
        assertEquals(exception.getMessage(), "Chain 'test' not found.");
        throw exception;
    }
}
