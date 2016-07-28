package info.smart_tools.smartactors.core.postgres_schema.search;

///**
// * {@see SearchQueryWriter} {@link ISearchQueryWriter}.
// * General writer an ORDER clause for psql db.
// */
//public class GeneralSQLOrderWriter implements ISearchQueryWriter {
//    private static final IFieldName ORDER_FIELD_ORDER_BY_ITEM_FN;
//    private static final IFieldName ORDER_DIRECTION_ORDER_BY_ITEM_FN;
//
//    static {
//        try {
//            ORDER_FIELD_ORDER_BY_ITEM_FN =
//                    IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "field");
//            ORDER_DIRECTION_ORDER_BY_ITEM_FN =
//                    IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "order");
//        } catch (ResolutionException e) {
//            throw new RuntimeException("Static block initialize error: " + e.getMessage(), e);
//        }
//    }
//
//    private GeneralSQLOrderWriter() {}
//
//    /**
//     * Factory method for creation a new instance of <pre>GeneralSQLOrderWriter</pre>.
//     *
//     * @return new instance of <pre>GeneralSQLOrderWriter</pre>.
//     */
//    public static GeneralSQLOrderWriter create() {
//        return new GeneralSQLOrderWriter();
//    }
//
//    /**
//     * Writes an ORDER clause into the query statement.
//     *
//     * @param queryStatement - a compiled statement of query.
//     * @param queryMessage - message with parameters for query.
//     * @param setters - list of query parameters setter.
//     *                Any setter sets some parameter into query.
//     *
//     * @throws QueryBuildException when:
//     *              1. Writing body of query error;
//     *              2. IOC resolution error object;
//     *              3. Reading field from <pre>queryMessage</pre> error.
//     */
//    @Override
//    public void write(
//            @Nonnull final QueryStatement queryStatement,
//            @Nonnull final ISearchQuery queryMessage,
//            @Nonnull final List<SQLQueryParameterSetter> setters
//    ) throws QueryBuildException {
//        if (queryMessage.countOrderBy() == 0) {
//            return;
//        }
//
//        try {
//            queryStatement.getBodyWriter().write("ORDER BY");
//
//            for (int i = 0; i < queryMessage.countOrderBy(); ++i) {
//                IObject orderItem = queryMessage.getOrderBy(i);
//
//                FieldPath fieldPath = IOC.resolve(
//                        Keys.getOrAdd(PostgresFieldPath.class.toString()),
//                        orderItem.getValue(ORDER_FIELD_ORDER_BY_ITEM_FN));
//
//                String sortDirection = IOC.resolve(
//                        Keys.getOrAdd(String.class.toString()),
//                        orderItem.getValue(ORDER_DIRECTION_ORDER_BY_ITEM_FN));
//
//                sortDirection = ("DESC".equalsIgnoreCase(String.valueOf(sortDirection))) ? "DESC" : "ASC";
//                queryStatement
//                        .getBodyWriter()
//                        .write(String.format("(%s)%s,", fieldPath.toSQL(), sortDirection));
//            }
//
//            queryStatement.getBodyWriter().write("(1)");
//        } catch (IOException | ResolutionException | ReadValueException e) {
//            throw new QueryBuildException("Error while writing ORDER BY clause of search query SQL.", e);
//        } catch (InvalidArgumentException e) { //TODO added by AKutalev, reason: now IObject can throw InvalidArgumentException
//            throw new QueryBuildException("Invalid argument exception" , e);
//        }
//    }
//}
