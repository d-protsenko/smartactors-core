package info.smart_tools.smartactors.core.db_storage.interfaces;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *  Sets some of {@link PreparedStatement}'s parameters starting from {@code firstIndex}.
 *  Returns index of next parameter to be set.
 */
@FunctionalInterface
public interface SQLQueryParameterSetter {
    int setParameters(PreparedStatement statement, int firstIndex) throws SQLException, QueryBuildException, ReadValueException;
}
