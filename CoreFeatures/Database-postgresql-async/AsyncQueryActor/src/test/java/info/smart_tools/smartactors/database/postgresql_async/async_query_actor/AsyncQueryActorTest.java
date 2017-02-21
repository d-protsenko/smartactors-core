package info.smart_tools.smartactors.database.postgresql_async.async_query_actor;

import com.github.pgasync.Db;
import com.github.pgasync.ResultSet;
import com.github.pgasync.Row;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.database.postgresql_async.async_query_actor.wrappers.ModificationMessage;
import info.smart_tools.smartactors.database.postgresql_async.async_query_actor.wrappers.SearchMessage;
import info.smart_tools.smartactors.helpers.plugins_loading_test_base.PluginsLoadingTestBase;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import rx.Observable;
import rx.subjects.PublishSubject;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test for {@link AsyncQueryActor}.
 */
public class AsyncQueryActorTest extends PluginsLoadingTestBase {
    private Db dbMock;
    private IMessageProcessor mpMock;
    private ModificationMessage mmMock;
    private SearchMessage smMock;
    private IQueue tqMock;

    private ResultSet testRsMock;
    private Row testRow;

    private ResultSet rsMock;
    private Observable<ResultSet> obs;

    private IObject doc;

    private IResolveDependencyStrategy idResolutionStrategyMock;

    @Override
    protected void loadPlugins() throws Exception {
        load(ScopedIOCPlugin.class);
        load(PluginScopeProvider.class);
        load(PluginIOCKeys.class);
        load(PluginDSObject.class);
        load(IFieldNamePlugin.class);
    }

    void expectEnqueuedTask()
            throws Exception {
        ArgumentCaptor<ITask> tc = ArgumentCaptor.forClass(ITask.class);
        verify(tqMock).put(tc.capture());
        tc.getValue().execute();
        reset(tqMock);
    }

    @Override
    protected void registerMocks() throws Exception {
        dbMock = mock(Db.class);
        mpMock = mock(IMessageProcessor.class);
        mmMock = mock(ModificationMessage.class);
        smMock = mock(SearchMessage.class);
        rsMock = mock(ResultSet.class);
        tqMock = mock(IQueue.class);

        when(mmMock.getProcessor()).thenReturn(mpMock);
        when(mmMock.getCollectionName()).thenReturn("collection1");
        when(mmMock.getDocument()).thenAnswer(invocation -> this.doc);

        idResolutionStrategyMock = mock(IResolveDependencyStrategy.class);
        when(idResolutionStrategyMock.resolve()).thenReturn("new-id-!").thenThrow(ResolveDependencyStrategyException.class);
        IOC.register(Keys.getOrAdd("db.collection.nextid"), idResolutionStrategyMock);

        IOC.register(Keys.getOrAdd("task_queue"), new SingletonStrategy(tqMock));

        testRsMock = mock(ResultSet.class);
        testRow = mock(Row.class);
        when(testRsMock.row(0)).thenReturn(testRow);
        when(testRow.getInt(0)).thenReturn(42);
        when(dbMock.querySet("select 42")).thenReturn(PublishSubject.just(testRsMock));
    }

    @Test(expected = Exception.class)
    public void Should_testConnectionPoolAndThrowWhenTestQueryFails()
            throws Exception {
        when(testRow.getInt(0)).thenReturn(13);

        new AsyncQueryActor(dbMock);
    }

    @Test
    public void Should_insertDocuments()
            throws Exception {
        doc = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

        obs = PublishSubject.just(rsMock);
        when(dbMock.querySet(any(), any())).thenReturn(obs);

        AsyncQueryActor actor = new AsyncQueryActor(dbMock);

        actor.insert(mmMock);

        verify(mpMock).pauseProcess();
        verify(mpMock).continueProcess(null);

        assertEquals("new-id-!", doc.getValue(IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "collection1ID")));
    }


}
