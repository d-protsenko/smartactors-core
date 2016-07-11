package info.smart_tools.smartactors.core.db_tasks.psql.search;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ConditionsWriterResolverTest {

    @Test
    public void resolve_QueryCriteria() throws InvalidArgumentException, ChangeValueException, QueryBuildException {
//        QueryConditionWriterResolver conditionsWriterResolver  = ConditionsWriterResolver.create();
//
//        IObject criteria = new DSObject();
//
//        List<IObject> or = new ArrayList<>();
//
//        IObject fBlock = new DSObject();
//        IObject sBlock = new DSObject();
//
//        IObject a = new DSObject();
//        IObject b = new DSObject();
//        IObject c = new DSObject();
//
//        String lt = "2";
//        String lte = "3";
//        List<String> in = new ArrayList<>();
//        in.add("testInName");
//        in.add("4");
//
//        IFieldName orFN = new FieldName("$or");
//        IFieldName aFN = new FieldName("a");
//        IFieldName bFN = new FieldName("b");
//        IFieldName cFN = new FieldName("c");
//        IFieldName ltFN = new FieldName("$lt");
//        IFieldName lteFN = new FieldName("$lte");
//        IFieldName inFN = new FieldName("$in");
//
//        criteria.setValue(orFN, or);
//
//        or.add(fBlock);
//        or.add(sBlock);
//
//        fBlock.setValue(aFN, a);
//
//        sBlock.setValue(bFN, b);
//        sBlock.setValue(cFN, c);
//
//        a.setValue(ltFN, lt);
//        b.setValue(lteFN, lte);
//        c.setValue(inFN, in);
//
//        List<ParamContainer> paramContainers = new ArrayList<>();
//        QueryStatement queryStatement = new QueryStatement();
//        conditionsWriterResolver
//                .resolve(null)
//                .write(queryStatement, conditionsWriterResolver, null, criteria, paramContainers);

        List<Object> l = new ArrayList<>();
        String s = "";
        String NULL = null;

        int a = 0;
    }
}
