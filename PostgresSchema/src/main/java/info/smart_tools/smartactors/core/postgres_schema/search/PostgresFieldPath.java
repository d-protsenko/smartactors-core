package info.smart_tools.smartactors.core.postgres_schema.search;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;

/**
 * {@see FiledPath} {@link FieldPath}
 * Valid field path for psql database.
 */
public class PostgresFieldPath implements FieldPath {
    private String path;

    private PostgresFieldPath(final String[] parts) {
        this.path = String.format("%s#>\'{%s}\'",
                "document",
                String.join(",", parts));
    }

    private PostgresFieldPath(final String column, final String castFunction) {
        this.path = String.format("%s(%s)", castFunction, column);
    }

    /**
     * {@see FieldPath#getSQLRepresentation()} {@link FieldPath#getSQLRepresentation()}
     * @return valid representation of field path.
     */
    public String getSQLRepresentation() {
        return this.path;
    }

    public static PostgresFieldPath fromString(final String path)
            throws QueryBuildException {
        if (!FieldPath.isValid(path)) {
            throw new QueryBuildException("Invalid field path: " + path);
        }

//        if (path.equals("id")) {
//            return new PostgresFieldPath(PostgresSchema.ID_COLUMN, Schema.ID_TO_JSONB_CAST_FUNCTION);
//        }

        return new PostgresFieldPath(FieldPath.splitParts(path));
    }
}
