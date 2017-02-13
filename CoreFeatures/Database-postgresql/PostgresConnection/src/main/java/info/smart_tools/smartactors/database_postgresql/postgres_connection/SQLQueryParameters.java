package info.smart_tools.smartactors.database_postgresql.postgres_connection;

import java.sql.SQLException;

/**
 *
 */
public interface SQLQueryParameters {
    void setString(int index, String val) throws SQLException;
    void setObject(int index, Object val) throws SQLException;
    void setInt(int index, int val) throws SQLException;
}
