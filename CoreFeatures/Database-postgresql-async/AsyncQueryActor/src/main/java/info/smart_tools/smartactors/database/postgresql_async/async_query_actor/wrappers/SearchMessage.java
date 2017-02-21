package info.smart_tools.smartactors.database.postgresql_async.async_query_actor.wrappers;

import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;

/**
 *
 */
public interface SearchMessage {
    IMessageProcessor getProcessor() throws ReadValueException;

    Object getQuery() throws ReadValueException;

    String getCollectionName() throws ReadValueException;

    void setResult(Object result) throws ChangeValueException;
}
