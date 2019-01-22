package info.smart_tools.smartactors.transformation_rules.get_first_not_null;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;
import org.junit.Test;

import static org.junit.Assert.*;

public class GetFirstNotNullRuleTest {

    @Test
    public void MustCorrectReturnFirstNotNull() throws ResolutionStrategyException {
        Object[] args = new Object[10];

        Object targetObject = new Object();
        args[9] = targetObject;

        assertTrue(targetObject == new GetFirstNotNullRule().resolve(args));

        targetObject = new Object();
        args[4] = targetObject;

        assertTrue(targetObject == new GetFirstNotNullRule().resolve(args));

        targetObject = new Object();
        args[1] = targetObject;

        assertTrue(targetObject == new GetFirstNotNullRule().resolve(args));
    }

    @Test(expected = ResolutionStrategyException.class)
    public void MustInCorrectReturnFirstNotNullWhenAllNull() throws ResolutionStrategyException {
        Object[] args = new Object[10];

        new GetFirstNotNullRule().resolve(args);
    }
}