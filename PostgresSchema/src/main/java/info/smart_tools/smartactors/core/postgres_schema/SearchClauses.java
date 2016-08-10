package info.smart_tools.smartactors.core.postgres_schema;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_connection.QueryStatement;
import info.smart_tools.smartactors.core.postgres_schema.search.OrderWriter;
import info.smart_tools.smartactors.core.postgres_schema.search.PagingWriter;
import info.smart_tools.smartactors.core.postgres_schema.search.PostgresQueryWriterResolver;

import java.io.Writer;
import java.util.List;

/**
 * A set of methods to write parts of search SQL query.
 */
class SearchClauses {

    /**
     * Private constructor to avoid instantiation.
     */
    private SearchClauses() {
    }

    static void writeSearchWhere(QueryStatement statement, IObject criteria) throws Exception {
        IKey fieldNameKey = Keys.getOrAdd(IFieldName.class.getCanonicalName());
        Writer body = statement.getBodyWriter();
        try {
            IFieldName filterField = IOC.resolve(fieldNameKey, "filter");
            IObject filter = (IObject) criteria.getValue(filterField);
            if (filter == null) {
                return; // no filter in the criteria, ignoring
            }
            body.write(" WHERE ");
            PostgresQueryWriterResolver resolver = new PostgresQueryWriterResolver();
            resolver.resolve(null).write(statement, resolver, null, filter);
        } catch (ReadValueException e) {
            // no filter in the criteria, ignoring
        }
    }

    static void writeSearchOrder(QueryStatement statement, IObject criteria) throws Exception {
        IKey fieldNameKey = Keys.getOrAdd(IFieldName.class.getCanonicalName());
        Writer body = statement.getBodyWriter();
        try {
            IFieldName sortField = IOC.resolve(fieldNameKey, "sort");
            List<IObject> sortItems = (List<IObject>) criteria.getValue(sortField);
            if (sortItems == null || sortItems.isEmpty()) {
                return; // no sort in the criteria, ignoring
            }
            body.write(" ");
            OrderWriter order = new OrderWriter();
            order.write(statement, sortItems);
        } catch (ReadValueException e) {
            // no sort in the criteria, ignoring
        }
    }

    static void writeSearchPaging(QueryStatement statement, IObject criteria) throws Exception {
        IKey fieldNameKey = Keys.getOrAdd(IFieldName.class.getCanonicalName());
        Writer body = statement.getBodyWriter();
        try {
            IFieldName pageField = IOC.resolve(fieldNameKey, "page");
            IObject page = (IObject) criteria.getValue(pageField);
            if (page == null) {
                return; // no page in the criteria, ignoring
            }
            body.write(" ");
            Integer size;
            Integer number;
            try {
                IFieldName sizeField = IOC.resolve(fieldNameKey, "size");
                size = (Integer) page.getValue(sizeField);
                IFieldName numberField = IOC.resolve(fieldNameKey, "number");
                number = (Integer) page.getValue(numberField);
            } catch (Exception e) {
                throw new QueryBuildException("wrong page format: " + page.serialize(), e);
            }
            PagingWriter paging = new PagingWriter();
            paging.write(statement, number, size);
        } catch (ReadValueException e) {
            // no page in the criteria, ignoring
        }
    }

}
