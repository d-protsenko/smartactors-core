package info.smart_tools.smartactors.core.db_tasks.commons.queries;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.ISQLQueryParameterSetter;
import info.smart_tools.smartactors.core.sql_commons.DeclaredParam;

import java.sql.ResultSet;
import java.util.List;

public class ComplexCompiledQuery implements IComplexCompiledQuery {
    private ICompiledQuery compiledQuery;
    private List<DeclaredParam> declaredParameters;

    private ComplexCompiledQuery(final ICompiledQuery compiledQuery, final List<DeclaredParam> declaredParameters) {
        this.compiledQuery = compiledQuery;
        this.declaredParameters = declaredParameters;
    }

    public static ComplexCompiledQuery create(final ICompiledQuery compiledQuery,
                                              final List<DeclaredParam> declaredParameters
    ) {
        return new ComplexCompiledQuery(compiledQuery, declaredParameters);
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

    public List<DeclaredParam> getDeclaredParams() {
        return declaredParameters;
    }
}
