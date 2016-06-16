package info.smart_tools.smartactors.core.db_task.search.psql;

import info.smart_tools.smartactors.core.db_storage.interfaces.SQLQueryParameterSetter;
import info.smart_tools.smartactors.core.sql_commons.ConditionsResolverBase;
import info.smart_tools.smartactors.core.sql_commons.FieldPath;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;
import info.smart_tools.smartactors.core.sql_commons.psql.Schema;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
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

    @Test
    public void writeFieldCheckConditionTest() throws Exception {
        final int OPERATORS_NUMBER = 10;
        List<String> formats = new ArrayList<>();
        List<FieldPath> fieldsPaths = new ArrayList<>();
        List<QueryStatement> queryStatements = new ArrayList<>();
        List<SQLQueryParameterSetter> setters = new ArrayList<>();
        List<String> result = new ArrayList<>();
        String queryParam = "testQueryParam";

        formats.addAll(
                Arrays.asList(
                        "((%s)=to_json(?)::jsonb)", "((%s)!=to_json(?)::jsonb)", "((%s)<to_json(?)::jsonb)",
                        "((%s)>to_json(?)::jsonb)", "((%s)<=to_json(?)::jsonb)", "((%s)>=to_json(?)::jsonb)",
                        "(parse_timestamp_immutable(%s)>=(?)::timestamp)", "(parse_timestamp_immutable(%s)<=(?)::timestamp)",
                        "((%s)??(?))", String.format("(to_tsvector('%s',(%%s)::text))@@(to_tsquery(%s,?))",
                                Schema.FTS_DICTIONARY, Schema.FTS_DICTIONARY)));
        fieldsPaths.addAll(
                Arrays.asList(
                        PSQLFieldPath.fromString("eq"), PSQLFieldPath.fromString("ne"),
                        PSQLFieldPath.fromString("lt"), PSQLFieldPath.fromString("gt"),
                        PSQLFieldPath.fromString("lte"), PSQLFieldPath.fromString("gte"),
                        PSQLFieldPath.fromString("date-from"), PSQLFieldPath.fromString("date-to"),
                        PSQLFieldPath.fromString("hasTag"), PSQLFieldPath.fromString("fulltext")));

        for (int i = 0; i < OPERATORS_NUMBER; ++i)
            queryStatements.add(new QueryStatement());

        result.addAll(
                Arrays.asList(
                        "((document#>'{eq}')=to_json(?)::jsonb)", "((document#>'{ne}')!=to_json(?)::jsonb)",
                        "((document#>'{lt}')<to_json(?)::jsonb)", "((document#>'{gt}')>to_json(?)::jsonb)",
                        "((document#>'{lte}')<=to_json(?)::jsonb)", "((document#>'{gte}')>=to_json(?)::jsonb)",
                        "(parse_timestamp_immutable(document#>'{date-from}')>=(?)::timestamp)",
                        "(parse_timestamp_immutable(document#>'{date-to}')<=(?)::timestamp)",
                        "((document#>'{hasTag}')??(?))",
                        "(to_tsvector('russian',(document#>'{fulltext}')::text))@@(to_tsquery(russian,?))"));

        Method writeFieldCheckCondition = Operators.class.getDeclaredMethod(
                "writeFieldCheckCondition",
                String.class,
                QueryStatement.class,
                FieldPath.class,
                Object.class,
                List.class);
        writeFieldCheckCondition.setAccessible(true);

        for (int i = 0; i < OPERATORS_NUMBER; ++i) {
            writeFieldCheckCondition.invoke(null, formats.get(i),
                    queryStatements.get(i), fieldsPaths.get(i), queryParam, setters);
            assertEquals(queryStatements.get(i).getBodyWriter().toString().trim(), result.get(i));
        }
    }
}
