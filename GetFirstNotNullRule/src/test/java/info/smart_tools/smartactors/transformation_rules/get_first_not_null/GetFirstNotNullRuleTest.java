package info.smart_tools.smartactors.transformation_rules.get_first_not_null;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import org.junit.Test;

import static org.junit.Assert.*;

public class GetFirstNotNullRuleTest {

    @Test
    public void MustCorrectReturnFirstNotNull() throws ResolveDependencyStrategyException {
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

    @Test(expected = ResolveDependencyStrategyException.class)
    public void MustInCorrectReturnFirstNotNullWhenAllNull() throws ResolveDependencyStrategyException {
        Object[] args = new Object[10];

        new GetFirstNotNullRule().resolve(args);
    }
}