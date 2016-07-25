package info.smart_tools.smartactors.core.resolve_object_from_list_strategy;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.mock;

public class ResolveObjectFromListDependencyStrategyTest {

    private ResolveObjectFromListDependencyStrategy strategy;

    @Before
    public void setUp() throws Exception {

        strategy = new ResolveObjectFromListDependencyStrategy();
    }

    @Test
    public void ShouldCorrectExtractObjectFromList() throws ResolveDependencyStrategyException {


        List<Object> list = new ArrayList<>();
        Object object = mock(Object.class);
        list.add(object);

        Object result = strategy.resolve(list, 0);
        assertEquals(result, object);
    }

    @Test(expected = ResolveDependencyStrategyException.class)
    public void ShouldThrowException_When_ClassCastIsImpossible() throws Exception {

        strategy.resolve(new HashMap<>(), 0);
        fail();
    }

    @Test(expected = ResolveDependencyStrategyException.class)
    public void ShouldThrowException_When_IndexArgumentIsNotPassed() throws Exception {

        List<Object> list = new ArrayList<>();
        Object object = mock(Object.class);
        list.add(object);
        strategy.resolve(list);
        fail();
    }

    @Test(expected = ResolveDependencyStrategyException.class)
    public void ShouldThrowException_When_NullIsPassed() throws Exception {

        strategy.resolve(null);
        fail();
    }



}
