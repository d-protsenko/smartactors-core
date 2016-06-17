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
        wg.generate(IWrapper.class, binding);
    }
}
