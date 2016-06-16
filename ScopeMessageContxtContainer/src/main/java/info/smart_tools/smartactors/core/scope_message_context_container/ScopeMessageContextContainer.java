package info.smart_tools.smartactors.core.scope_message_context_container;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iscope.exception.ScopeException;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.message_context.IMessageContextContainer;
import info.smart_tools.smartactors.core.message_context.exceptions.MessageContextAccessException;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;

/**
 * Implementation of {@link IMessageContextContainer} that stores message context in in current scope.
 */
public class ScopeMessageContextContainer implements IMessageContextContainer {
    private static final Object MESSAGE_CONTEXT_SCOPE_KEY = new Object();

    @Override
    public IObject getCurrentContext() throws MessageContextAccessException {
        try {
            return (IObject) ScopeProvider.getCurrentScope().getValue(MESSAGE_CONTEXT_SCOPE_KEY);
        } catch (ScopeProviderException e) {
            throw new MessageContextAccessException("Could not access current scope", e);
        } catch (ScopeException e) {
            throw new MessageContextAccessException("Could not access message context in current scope.", e);
        } catch (ClassCastException e) {
            throw new MessageContextAccessException("Invalid object stored as current context.", e);
        }
    }

    @Override
    public void setCurrentContext(final IObject context) throws MessageContextAccessException {
        try {
            ScopeProvider.getCurrentScope().setValue(MESSAGE_CONTEXT_SCOPE_KEY, context);
        } catch (ScopeProviderException e) {
            throw new MessageContextAccessException("Could not access current scope.", e);
        } catch (ScopeException e) {
            throw new MessageContextAccessException("Could not access message context in current scope.", e);
        }
    }
}
