package info.smart_tools.smartactors.core.db_task.search.utils.sql;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_task.search.psql.PSQLFieldPath;
import info.smart_tools.smartactors.core.db_task.search.utils.SearchQueryWriter;
import info.smart_tools.smartactors.core.db_task.search.wrappers.SearchQuery;
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

/**
 *
 */
public class GeneralSQLOrderWriter implements SearchQueryWriter {
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
     *
     * @return
     */
    public static GeneralSQLOrderWriter create() {
        return new GeneralSQLOrderWriter();
    }

    /**
     *
     * @param queryStatement
     * @param queryMessage
     * @throws QueryBuildException
     */
    @Override
    public void write(
            @Nonnull final QueryStatement queryStatement,
            @Nonnull final SearchQuery queryMessage
    ) throws QueryBuildException {
        if (queryMessage.countOrderBy() == 0) {
            return;
        }

        try {
            queryStatement.getBodyWriter().write("ORDER BY");

            for (int i = 0; i < queryMessage.countOrderBy(); ++i) {
                IObject orderItem = queryMessage.getOrderBy(i);

                FieldPath fieldPath = IOC.resolve(
                        Keys.getOrAdd(PSQLFieldPath.class.toString()),
                        orderItem.getValue(ORDER_FIELD_ORDER_BY_ITEM_FN));

                String sortDirection = IOC.resolve(
                        Keys.getOrAdd(PSQLFieldPath.class.toString()),
                        orderItem.getValue(ORDER_DIRECTION_ORDER_BY_ITEM_FN));

                sortDirection = ("DESC".equalsIgnoreCase(String.valueOf(sortDirection))) ? "DESC" : "ASC";
                queryStatement
                        .getBodyWriter()
                        .write(String.format("(%s)%s,", fieldPath.getSQLRepresentation(), sortDirection));
            }

            queryStatement.getBodyWriter().write("(1)");
        } catch (IOException | ResolutionException | ReadValueException e) {
            throw new QueryBuildException("Error while writing ORDER BY clause of search query SQL.", e);
        }
    }
}
