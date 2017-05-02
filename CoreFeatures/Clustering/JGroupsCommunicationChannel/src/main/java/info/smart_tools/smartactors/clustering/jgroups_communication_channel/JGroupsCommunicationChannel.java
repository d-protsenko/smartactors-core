package info.smart_tools.smartactors.clustering.jgroups_communication_channel;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.exception.invalid_state_exception.InvalidStateException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.IBiAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.clustering_interfaces.communication_channel_interfaces.ICommunicationChannel;
import info.smart_tools.smartactors.clustering_interfaces.communication_channel_interfaces.INodeId;
import info.smart_tools.smartactors.clustering_interfaces.communication_channel_interfaces.exceptions.ClusterMessageSendException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.SerializeException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementation of {@link ICommunicationChannel} that uses JGroups library as backend.
 */
public class JGroupsCommunicationChannel implements ICommunicationChannel {
    private final Channel channel;
    private List<INodeId> nodeList;
    private final INodeId thisNodeId;
    private final List<IAction<ICommunicationChannel>> topologyChangeCallbacks;
    private final IQueue<ITask> taskQueue;
    private final Map<Object, IBiAction<INodeId, IObject>> messageTypeListeners;
    private final IFieldName messageTypeFieldName;

    /**
     * Implementation of {@link Receiver}.
     */
    private class JGroupsChannelReceiver implements Receiver {

        @Override
        public void viewAccepted(final View newView) {
            List<INodeId> newNodeList = new ArrayList<>(newView.size());

            try {
                for (Address address : newView) {
                    INodeId nodeId = IOC.resolve(Keys.getOrAdd("node id from jgroups address"), address);
                    newNodeList.add(nodeId);
                }
            } catch (ResolutionException e) {
                throw new RuntimeException(e);
            }

            nodeList = newNodeList;

            exec(() -> {
                try {
                    for (IAction<ICommunicationChannel> cb : topologyChangeCallbacks) {
                        cb.execute(JGroupsCommunicationChannel.this);
                    }
                } catch (ActionExecuteException | InvalidArgumentException e) {
                    throw new TaskExecutionException(e);
                }
            });
        }

        @Override
        public void suspect(final Address suspectedMbr) {

        }

        @Override
        public void block() {
            // TODO:: Check it it's really necessary to implement this method
        }

        @Override
        public void unblock() {

        }

        @Override
        public void receive(final Message msg) {
            exec(() -> {
                try {
                    INodeId srcNode = IOC.resolve(Keys.getOrAdd("node id from jgroups address"), msg.getSrc());
                    byte[] buf = msg.getBuffer();
                    IObject content = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), new String(buf));
                    Object type = content.getValue(messageTypeFieldName);
                    IBiAction<INodeId, IObject> callback = messageTypeListeners.get(type);

                    if (null == callback) {
                        throw new TaskExecutionException(String.format("Unknown message type '%s'", type));
                    }

                    callback.execute(srcNode, content);
                } catch (ResolutionException | ReadValueException | InvalidArgumentException | ActionExecuteException e) {
                    throw new TaskExecutionException(e);
                }
            });
        }

        @Override
        public void getState(final OutputStream output) throws Exception {

        }

        @Override
        public void setState(final InputStream input) throws Exception {

        }
    }

    /**
     * The constructor.
     *
     * @throws ResolutionException if error occurs resolving any dependency
     * @throws Exception if error occurs
     */
    public JGroupsCommunicationChannel()
            throws Exception {
        this.topologyChangeCallbacks = new CopyOnWriteArrayList<>();
        this.messageTypeListeners = new ConcurrentHashMap<>();
        this.nodeList = Collections.emptyList();
        this.messageTypeFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "__TYPE_");
        this.taskQueue = IOC.resolve(Keys.getOrAdd("task_queue"));

        channel = new JChannel();
        channel.setDiscardOwnMessages(true);
        channel.setReceiver(new JGroupsChannelReceiver());

        // TODO:: Move to argument (?)
        channel.connect(IOC.resolve(Keys.getOrAdd("cluster name")));

        thisNodeId = IOC.resolve(Keys.getOrAdd("node id from jgroups address"), channel.getAddress());
    }

    @Override
    public INodeId getThisNodeId() {
        return thisNodeId;
    }

    @Override
    public Collection<INodeId> getNodeList() {
        return nodeList;
    }

    @Override
    public void setMessageListener(final Object messageType, final IBiAction<INodeId, IObject> listener)
            throws InvalidArgumentException, InvalidStateException {
        if (null == listener) {
            throw new InvalidArgumentException("Listener is null");
        }

        if (null != messageTypeListeners.putIfAbsent(messageType, listener)) {
            throw new InvalidStateException(String.format("There already is a listener for message type '%s'", messageType));
        }
    }

    @Override
    public void sendUnicast(final INodeId dst, final Object type, final IObject message)
            throws ClusterMessageSendException, InvalidArgumentException {
        Address dstAddress;

        try {
            dstAddress = IOC.resolve(Keys.getOrAdd("jgroups address from node id"), dst);
        } catch (ResolutionException e) {
            throw new InvalidArgumentException("Invalid node id object.", e);
        }

        send0(dstAddress, type, message);
    }

    @Override
    public void sendBroadcast(final Object type, final IObject message) throws ClusterMessageSendException {
        send0(null, type, message);
    }

    @Override
    public void addTopologyChangeListener(final IAction<ICommunicationChannel> listener) throws InvalidArgumentException {
        if (null == listener) {
            throw new InvalidArgumentException("Listener is null.");
        }

        topologyChangeCallbacks.add(listener);

        if (null != nodeList) {
            exec(() -> {
                try {
                    listener.execute(this);
                } catch (ActionExecuteException | InvalidArgumentException e) {
                    throw new TaskExecutionException(e);
                }
            });
        }
    }

    private void send0(final Address dstAddress, final Object type, final IObject message) throws ClusterMessageSendException {
        try {
            channel.send(dstAddress, messageBufferFromTypeAndContent(type, message));
        } catch (SerializeException | ResolutionException | ChangeValueException | InvalidArgumentException e) {
            throw new ClusterMessageSendException("Error occurred creating the message.", e);
        } catch (Exception e) {
            throw new ClusterMessageSendException("Error occurred sending the message.", e);
        }
    }

    private byte[] messageBufferFromTypeAndContent(final Object type, final IObject content)
            throws SerializeException, ResolutionException, ChangeValueException, InvalidArgumentException {
        content.setValue(messageTypeFieldName, type);
        String asString = content.serialize();
        return asString.getBytes();
    }

    private void exec(final ITask task) {
        try {
            taskQueue.put(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
