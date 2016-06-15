package info.smart_tools.smartactors.core.sql_commons;

import info.smart_tools.smartactors.core.sql_commons.exception.CompiledQueryResolverException;

@FunctionalInterface
public interface CompiledQueryResolver {

    QueryStatement compile() throws CompiledQueryResolverException;
}
