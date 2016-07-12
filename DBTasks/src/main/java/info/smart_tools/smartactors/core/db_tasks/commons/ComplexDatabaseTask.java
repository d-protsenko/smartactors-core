package info.smart_tools.smartactors.core.db_tasks.commons;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.sql_commons.ParamContainer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 */
abstract class ComplexDatabaseTask extends CachedDatabaseTask {
    /**
     *
     */
    protected ComplexDatabaseTask() {
        super();
    }

    protected List<Object> sortParameters(@Nonnull final IObject parameters,
                                          @Nonnull final List<ParamContainer> order
    ) throws QueryBuildException {
        try {
            List<Object> sortedParams = new ArrayList<>();
            for (ParamContainer container : order) {
                Object parameter = parameters.getValue(container.getName());
                if (!List.class.isAssignableFrom(parameter.getClass())) {
                    List<Object> inParameters = (List<Object>) parameter;
                    if (inParameters.size() > container.getCount()) {
                        throw new QueryBuildException("Invalid parameters: too many arguments for \"$in\"!");
                    }
                    sortedParams.addAll(inParameters);
                    int variableSize = container.getCount() - inParameters.size();
                    if (variableSize > 0) {
                        for (int i = 0; i < variableSize; ++i) {
                            sortedParams.add(inParameters.get(inParameters.size() - 1));
                        }
                    }
                } else {
                    sortedParams.add(parameter);
                }
            }

            return  sortedParams;
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }
}
