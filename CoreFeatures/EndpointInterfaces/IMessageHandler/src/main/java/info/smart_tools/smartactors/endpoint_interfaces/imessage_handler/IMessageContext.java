package info.smart_tools.smartactors.endpoint_interfaces.imessage_handler;

public interface IMessageContext {
    <T extends IMessageContext> T cast();
}
