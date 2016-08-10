package info.smart_tools.smartactors.core.postgres_schema.indexes;

import info.smart_tools.smartactors.core.iobject.IObject;

/**
 * Functional interface to resolve IndexWriter.
 */
interface IndexWriterResolver {

    IndexWriter resolve(IObject indexDefinition) throws Exception;

}
