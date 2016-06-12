package info.smart_tools.smartactors.core.db_task.insert.psql;

import info.smart_tools.smartactors.core.db_storage.exceptions.StorageException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.PreparedQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_task.insert.psql.wrapper.InsertMessage;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;
import org.junit.Before;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.StringWriter;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest()
public class DBInsertTaskTest {

    private QueryStatement preparedQuery;
    private CompiledQuery compiledQuery;
    private IFieldName iIdFieldName;
    private String collectionName = "collectionName";
    private DBInsertTask task = new DBInsertTask();

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(IOC.class);
        PowerMockito.mockStatic(Keys.class);

        IKey FNKey = Keys.getOrAdd(IFieldName.class.toString());
        IKey QSKey = Keys.getOrAdd(QueryStatement.class.toString());

        when(Keys.getOrAdd(eq("interface info.smart_tools.smartactors.core.sql_commons.QueryStatement"))).thenReturn(QSKey);
        when(Keys.getOrAdd(eq("interface info.smart_tools.smartactors.core.iobject.IFieldName"))).thenReturn(FNKey);
        when(IOC.resolve(eq(FNKey), eq(collectionName + "Id"))).thenReturn(iIdFieldName);
        when(IOC.resolve(eq(QSKey))).thenReturn(preparedQuery);
    }

    @Test(expected = TaskPrepareException.class)
    public void ShouldThrowTaskPrepareException_WhenIdIsNotNull() throws ResolutionException, ReadValueException, ChangeValueException, StorageException, TaskPrepareException, TaskSetConnectionException {
        IObject insertQuery = mock(IObject.class);
        InsertMessage message = mock(InsertMessage.class);
        IKey IMKey = mock(IKey.class);

        IObject mockObject = mock(IObject.class);
        when(insertQuery.getValue(iIdFieldName)).thenReturn(mockObject);

        when(Keys.getOrAdd(eq("interface info.smart_tools.smartactors.core.db_task.insert.psql.wrapper.InsertMessage"))).thenReturn(IMKey);
        when(IOC.resolve(eq(IMKey), eq(insertQuery))).thenReturn(message);
        when(message.getCollectionName()).thenReturn(collectionName);

        IKey stringKey = mock(IKey.class);
        when(Keys.getOrAdd(eq(String.class.toString()))).thenReturn(stringKey);
        when(IOC.resolve(eq(stringKey), eq(mockObject))).thenReturn("111");

        StorageConnection connection = mock(StorageConnection.class);
        when(connection.compileQuery(any(PreparedQuery.class))).thenReturn(compiledQuery);

        task.setConnection(connection);
        task.prepare(insertQuery);
    }

}