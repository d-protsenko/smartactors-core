package info.smart_tools.smartactors.clustering.jgroups_communication_channel;

import info.smart_tools.smartactors.clustering_interfaces.communication_channel_interfaces.INodeId;
import org.jgroups.Address;

/**
 * Implementation of node identifier for JGroups backend.
 */
public class JGroupsNodeId implements INodeId {
    private final Address address;

    /**
     * The constructor.
     *
     * @param address    the JGroups address
     */
    public JGroupsNodeId(final Address address) {
        this.address = address;
    }

    @Override
    public int compareTo(final INodeId that) {
        return this.address.compareTo(((JGroupsNodeId) that).address);
    }

    @Override
    public boolean equals(final Object that) {
        return that instanceof JGroupsNodeId && this.address.equals(((JGroupsNodeId) that).address);
    }

    @Override
    public int hashCode() {
        return address.hashCode() + 31;
    }
}
