package info.smart_tools.smartactors.core.db_tasks.psql.search;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.ISQLQueryParameterSetter;
import info.smart_tools.smartactors.core.sql_commons.ParamContainer;

import java.sql.ResultSet;
import java.util.List;

public class SearchCompiledQuery implements ICompiledQuery {
    private ICompiledQuery compiledQuery;
    private List<ParamContainer> parametersOrder;

    private SearchCompiledQuery(final ICompiledQuery compiledQuery, final List<ParamContainer> parametersOrder) {
        this.compiledQuery = compiledQuery;
        this.parametersOrder = parametersOrder;
    }

    public static SearchCompiledQuery create(final ICompiledQuery compiledQuery,
                                             final List<ParamContainer> parametersOrder
    ) {
        return new SearchCompiledQuery(compiledQuery, parametersOrder);
    }

    @Override
    public boolean execute() throws QueryExecutionException {
        return compiledQuery.execute();
    }

    @Override
    public ResultSet executeQuery() throws QueryExecutionException {
        return compiledQuery.executeQuery();
    }

    @Override
    public int executeUpdate() throws QueryExecutionException {
        return compiledQuery.executeUpdate();
    }

    @Override
    public void setParameters(final ISQLQueryParameterSetter parameterSetter) throws QueryBuildException {
        compiledQuery.setParameters(parameterSetter);
    }

    public List<ParamContainer> getParametersOrder() {
        return parametersOrder;
    }
}
