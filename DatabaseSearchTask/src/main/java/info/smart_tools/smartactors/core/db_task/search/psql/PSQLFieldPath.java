package info.smart_tools.smartactors.core.db_task.search.psql;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.sql_commons.FieldPath;

import javax.annotation.Nonnull;

final class PSQLFieldPath implements FieldPath {
    private String path;

    private PSQLFieldPath(final String[] parts) {
        this.path = String.format("%s#>\'{%s}\'",
                "document",
                String.join(",", parts));
    }

    private PSQLFieldPath(final String column, final String castFunction) {
        this.path = String.format("%s(%s)", castFunction, column);
    }

    public String getSQLRepresentation() {
        return this.path;
    }

    public static PSQLFieldPath fromString(@Nonnull final String path)
            throws QueryBuildException {
        if(!FieldPath.isValid(path)) {
            throw new QueryBuildException("Invalid field path: " + path);
        }

        if (path.equals("id")) {
            return new PSQLFieldPath("id", "bigint_to_jsonb_immutable");
        }

        return new PSQLFieldPath(FieldPath.splitParts(path));
    }
}
