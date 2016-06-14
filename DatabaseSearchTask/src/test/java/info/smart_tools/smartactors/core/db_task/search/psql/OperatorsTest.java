package info.smart_tools.smartactors.core.db_task.search.psql;

import info.smart_tools.smartactors.core.sql_commons.ConditionsResolverBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Operators.class)
public class OperatorsTest {
    private int operatorsNumber;

    @Before
    public void setUp() {
        operatorsNumber = 12;
    }

    @Test
    public void should_AddsAllOperators() throws Exception {
        ConditionsResolverBase conditionsResolverBase = mock(ConditionsResolverBase.class);
        Operators.addAll(conditionsResolverBase);

        verify(conditionsResolverBase, times(operatorsNumber)).addOperator(anyString(), anyObject());
        verifyPrivate(Operators.class, times(10)).invoke("formattedCheckWriter", anyString());
    }
}
