package info.smart_tools.smartactors.core.db_tasks.commons.queries;

import info.smart_tools.smartactors.core.sql_commons.DeclaredParam;

import javax.annotation.Nonnull;
import java.util.List;

/**
 *
 */
public interface IComplexQueryStatementBuilder extends IQueryStatementBuilder {
    /**
     *
     * @param declaredParams
     * @return
     */
    IComplexQueryStatementBuilder withDeclaredParams(@Nonnull final List<DeclaredParam> declaredParams);
}
