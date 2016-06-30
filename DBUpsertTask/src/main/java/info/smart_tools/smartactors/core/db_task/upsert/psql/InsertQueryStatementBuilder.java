package info.smart_tools.smartactors.core.db_task.upsert.psql;

public class InsertQueryStatementBuilder extends QueryStatementBuilder {
    private static final String[] templateParts = { "INSERT ",
            "AS tab SET document = docs.document FROM VALUES(?::jsonb) RETURNING id;" };

    private static final int TEMPLATE_SIZE = templateParts[0].length() + templateParts[1].length();

    private InsertQueryStatementBuilder() {}

    public static InsertQueryStatementBuilder create() {
        return new InsertQueryStatementBuilder();
    }

    @Override
    protected String[] getTemplateParts() {
        return templateParts;
    }

    @Override
    protected int getTemplateSize() {
        return TEMPLATE_SIZE;
    }
}
