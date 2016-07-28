package info.smart_tools.smartactors.core.postgres_schema.search;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PSQLFieldPathTest {

    @Test
    public void should_ResolvesPSQLFieldPath() throws QueryBuildException {
        String path = "testValidPath";
        FieldPath fieldPath = PostgresFieldPath.fromString(path);
        assertEquals(fieldPath.getSQLRepresentation(), "document#>'{testValidPath}'");
    }

    @Test(expected = QueryBuildException.class)
    public void should_ThrowsException() throws QueryBuildException {
        String path = "tes!t#%Incorrect_Path@#%$";
        PostgresFieldPath.fromString(path);
    }
}
