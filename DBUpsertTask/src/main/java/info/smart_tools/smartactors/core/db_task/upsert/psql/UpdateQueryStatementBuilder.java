package info.smart_tools.smartactors.core.db_task.upsert.psql;

public class UpdateQueryStatementBuilder extends QueryStatementBuilder {
    private static final String[] templateParts = {"UPDATE ",
            " AS tab SET document = docs.document FROM VALUES(?,?::jsonb)) " +
                    "AS docs (id, document) WHERE tab.id = docs.id;" };

    private static final int TEMPLATE_SIZE = templateParts[0].length() + templateParts[1].length();

    private UpdateQueryStatementBuilder() {}

    public static UpdateQueryStatementBuilder create() {
        return new UpdateQueryStatementBuilder();
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
