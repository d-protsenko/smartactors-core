package info.smart_tools.smartactors.core.pool_guard;

public class PoolGuard implements AutoCloseable {
    /**
     * Local storage for instance of {@link IScope}
     */
    private Pool pool;

    /**
     * Locally save and substitute current instance of {@link IScope} by
     * other
     * @param key unique identifier for find {@link IScope}
     * @throws  ScopeGuardException if any errors occurred
     */
    public Object getObject() {return null;}


    @Override
    public void close() throws Exception {

    }
}
