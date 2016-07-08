package info.smart_tools.smartactors.core.db_tasks.psql.search.utils;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ISQLQueryParameterSetter;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.FieldPath;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * {@see SearchQueryWriter} {@link ISearchQueryStatementWriter}.
 * General writer an ORDER clause for psql db.
 */
public class GeneralSQLOrderWriter implements ISearchQueryStatementWriter<List<IObject>> {
    private static final IFieldName ORDER_FIELD_ORDER_BY_ITEM_FN;
    private static final IFieldName ORDER_DIRECTION_ORDER_BY_ITEM_FN;

    static {
        try {
            ORDER_FIELD_ORDER_BY_ITEM_FN =
                    IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "field");
            ORDER_DIRECTION_ORDER_BY_ITEM_FN =
                    IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "order");
        } catch (ResolutionException e) {
            throw new RuntimeException("Static block initialize error: " + e.getMessage(), e);
        }
    }

    private GeneralSQLOrderWriter() {}

    /**
     * Factory method for creation a new instance of <pre>GeneralSQLOrderWriter</pre>.
     *
     * @return new instance of <pre>GeneralSQLOrderWriter</pre>.
     */
    public static GeneralSQLOrderWriter create() {
        return new GeneralSQLOrderWriter();
    }

    /**
     * Writes an ORDER clause into the query statement.
     *
     * @param queryStatement - a compiled statement of query.
     * @param orderByItems -
     * @param setters - list of query parameters setter.
     *                Any setter sets some parameter into query.
     *
     * @throws QueryBuildException when:
     *              1. Writing body of query error;
     *              2. IOC resolution error object;
     *              3. Reading field from <pre>queryMessage</pre> error.
     */
    @Override
    public void write(
            @Nonnull final QueryStatement queryStatement,
            @Nonnull final List<IObject> orderByItems,
            @Nonnull final List<ISQLQueryParameterSetter> setters
    ) throws QueryBuildException {
        if (orderByItems.size() == 0) {
            return;
        }

        try {
            queryStatement.getBodyWriter().write("ORDER BY");

            for (IObject orderByItem : orderByItems) {
                String sortDirection = orderByItem.getValue(ORDER_DIRECTION_ORDER_BY_ITEM_FN).toString();
                FieldPath fieldPath =
                        PSQLFieldPath.fromString(orderByItem.getValue(ORDER_FIELD_ORDER_BY_ITEM_FN).toString());

                sortDirection = ("DESC".equalsIgnoreCase(String.valueOf(sortDirection))) ? "DESC" : "ASC";
                queryStatement
                        .getBodyWriter()
                        .write(String.format("(%s)%s,", fieldPath.getSQLRepresentation(), sortDirection));
            }

            queryStatement.getBodyWriter().write("(1)");
        } catch (IOException | ReadValueException e) {
            throw new QueryBuildException("Error while writing ORDER BY clause of search query SQL.", e);
        }
    }
}
