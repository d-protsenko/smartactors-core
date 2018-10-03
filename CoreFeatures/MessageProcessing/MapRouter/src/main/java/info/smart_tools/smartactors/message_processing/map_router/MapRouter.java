package info.smart_tools.smartactors.message_processing.map_router;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.class_management.version_manager.VersionManager;
import info.smart_tools.smartactors.message_processing_interfaces.irouter.IRouter;
import info.smart_tools.smartactors.message_processing_interfaces.irouter.exceptions.RouteNotFoundException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link IRouter} that uses a {@link Map} instance.
 */
public class MapRouter implements IRouter {
    private final Map<Object, Map<Object, IMessageReceiver>> map;

    /**
     * The constructor.
     *
     * @param map    {@link Map} instance to use as routing table
     * @throws InvalidArgumentException if map is {@code null}
     */
    public MapRouter(final Map<Object, Map<Object, IMessageReceiver>> map)
            throws InvalidArgumentException {
        if (null == map) {
            throw new InvalidArgumentException("Map should not be null.");
        }

        this.map = map;
    }

    @Override
    public IMessageReceiver route(final Object targetId) throws RouteNotFoundException {
        IMessageReceiver receiver = null;
        Map<Object, IMessageReceiver> versions = map.get(targetId);

        if (versions != null) {
            receiver = VersionManager.getFromMap(versions);
        }

        if (null == receiver) {
            throw new RouteNotFoundException(MessageFormat.format("Route to {0} not found.", targetId));
        }

        return receiver;
    }

    @Override
    public void register(final Object targetId, final IMessageReceiver receiver) {
        Map<Object, IMessageReceiver> versions = map.get(targetId);

        if (versions == null) {
            versions = new ConcurrentHashMap<>();
            map.put(targetId, versions);
        }

        IMessageReceiver oldReceiver = versions.put(VersionManager.getCurrentModule(), receiver);

        if (null != oldReceiver) {
            System.out.println(MessageFormat.format("Warning: replacing receiver ({0}) registered as ''{1}'' by {2}",
                    oldReceiver.toString(), targetId.toString(), receiver.toString()));
        }
    }

    @Override
    public void unregister(final Object targetId) {
        IMessageReceiver receiver = null;
        Map<Object, IMessageReceiver> versions = map.get(targetId);

        if (versions != null) {
            receiver = versions.remove(VersionManager.getCurrentModule());
            if (versions.size() == 0) {
                map.remove(targetId);
            }
        }

        if (null != receiver) {
            receiver.dispose();
        } else {
            System.out.println(MessageFormat.format("Warning: ''{0}'' has no receivers, nothing to delete",
                    targetId.toString()));
        }
    }

    @Override
    public List<Object> enumerate() {
        return new ArrayList<>(map.keySet());
    }
}
