package info.smart_tools.smartactors.core.db_tasks.psql.search;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.IStorageConnection;
import info.smart_tools.smartactors.core.db_tasks.commons.DBQueryFields;
import info.smart_tools.smartactors.core.db_tasks.commons.DBSearchTask;
import info.smart_tools.smartactors.core.db_tasks.wrappers.search.ISearchMessage;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.sql_commons.IQueryStatementFactory;
import info.smart_tools.smartactors.core.sql_commons.ParamContainer;
import info.smart_tools.smartactors.core.sql_commons.exception.QueryStatementFactoryException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Task for searching documents in database.
 */
public class PSQLSearchTask extends DBSearchTask {

    private PSQLSearchTask() {}

    /**
     * Factory method for creation new instance of {@link PSQLSearchTask}.
     *
     * @return
     */
    public static PSQLSearchTask create() {
       return new PSQLSearchTask();
    }

    @Override
    protected boolean requiresNonExecutable(@Nonnull final IObject queryMessage) throws InvalidArgumentException {
        return false;
    }

    @Nonnull
    @Override
    protected ICompiledQuery takeCompiledQuery(@Nonnull final IStorageConnection connection,
                                               @Nonnull final IObject message
    ) throws QueryBuildException {

//        try {
//            String collection = DBQueryFields.COLLECTION.in(message);
//            IKey queryKey = QueryKey.create(
//                    connection.getId(),
//                    PSQLSearchByIdTask.class.toString(),
//                    collection);
//
//            return takeCompiledQuery(
//                    queryKey,
//                    connection,
//                    getQueryStatementFactory(collection));
//        } catch (ReadValueException | InvalidArgumentException e) {
//           throw new QueryBuildException(e.getMessage(), e);
//        }

//        try {
//
//        } catch (ReadValueException | ResolutionException e) {
//            throw new QueryBuildException(e.getMessage(), e);
//        }
        return null;
    }

    @Nonnull
    @Override
    protected ICompiledQuery setParameters(@Nonnull final ICompiledQuery query,
                                           @Nonnull final IObject message
    ) throws QueryBuildException {
        try {
            SearchCompiledQuery searchCompiledQuery = (SearchCompiledQuery) query;
            IObject parameters = DBQueryFields.PARAMETERS.in(message);
            List<ParamContainer> parametersOrder = searchCompiledQuery.getParametersOrder();

            List<Object> orderedParameters = new ArrayList<>();
            for (ParamContainer container : parametersOrder) {
                Object parameter = parameters.getValue(container.getName());
                if (!List.class.isAssignableFrom(parameter.getClass())) {
                    List<Object> inParameters = (List<Object>) parameter;
                    if (inParameters.size() > container.getCount()) {
                        throw new QueryBuildException();
                    }
                    orderedParameters.addAll(inParameters);
                    int variableSize = container.getCount() - inParameters.size();
                    if (variableSize > 0) {
                        for (int i = 0; i < variableSize; ++i) {
                            orderedParameters.add(inParameters.get(inParameters.size() - 1));
                        }
                    }
                } else {
                    orderedParameters.add(parameter);
                }
            }

            searchCompiledQuery.setParameters(statement -> {
                int parametersSize = orderedParameters.size();
                for (int i = 0; i < parametersSize; ++i) {
                    statement.setObject(i, orderedParameters.get(i));
                }
            });
        } catch (NullPointerException e) {

        } catch (ClassCastException e) {

        } catch (ReadValueException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }

        return query;
    }

    @Override
    protected void execute(@Nonnull final ICompiledQuery query,
                           @Nonnull final IObject message
    ) throws TaskExecutionException {
        try {
            DBQueryFields.SEARCH_RESULT.out(message, super.execute(query));
        } catch (ChangeValueException | InvalidArgumentException e) {
            throw new TaskExecutionException(e.getMessage(), e);
        }
    }

    private IQueryStatementFactory getQueryStatementFactory(final ISearchMessage message,
                                                            final List<ParamContainer> order
    ) {
        return () -> {
            try {
                return SearchQueryStatementBuilder
                        .create()
                        .withCollection(message.getCollection().toString())
                        .withCriteria(message.getCriteria())
                        .withOrderByItems(message.getOrderBy())
                        .build(order);
            } catch (QueryBuildException | ReadValueException e) {
                throw new QueryStatementFactoryException("Error while initialize a search by id query.", e);
            }
        };
    }
}

