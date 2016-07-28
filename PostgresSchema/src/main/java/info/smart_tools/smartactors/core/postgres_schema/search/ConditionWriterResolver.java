package info.smart_tools.smartactors.core.postgres_schema.search;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;

/**
 * Resolves a {@link ConditionWriter} for specific condition object key.
 */
public interface ConditionWriterResolver {

    /**
     * Returns the writer to write the specific query condition identified by the key
     * @param conditionKey key in key-value pair of condition object. May be an operator name ('$and', '$eq', '$lt', etc..),
     *                     field path ('parent.child', for example) or null for default operator
     * @return ConditionWriter found for this condition key
     * @throws QueryBuildException if something goes wrong
     */
    ConditionWriter resolve(String conditionKey) throws QueryBuildException;

}
