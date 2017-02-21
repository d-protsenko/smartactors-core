package info.smart_tools.smartactors.database.postgresql_async.async_query_actor.wrappers;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;

/**
 *
 */
public interface ModificationMessage {
    IMessageProcessor getProcessor() throws ReadValueException;

    String getCollectionName() throws ReadValueException;

    IObject getDocument() throws ReadValueException;
}
