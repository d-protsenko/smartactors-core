package info.smart_tools.smartactors.core.endpoint_generator;

import info.smart_tools.smartactors.core.iasync_service.IAsyncService;
import info.smart_tools.smartactors.core.iendpoint_creator.IEndpointCreator;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.util.HashMap;
import java.util.function.Function;

/**
 * Generator of endpoints by protocol.
 */
public final class EndpointGenerator {

    private static HashMap<String, IEndpointCreator> endpoints = new HashMap<>();

    private static IFieldName typeField;
    private static String defaultEndpointType = "http";

    private EndpointGenerator() {
    }

    static {
        try {
            typeField = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "type");
        } catch (ResolutionException e) {
            e.printStackTrace();
        }
        //put endpoint creators to endpoints map
    }

    /**
     * Generates {@link IEndpointCreator} by protocol.
     *
     * @param endpointParams the parameters of endpoint
     * @return function to create an endpoint.
     */
    public static Function<IMessageReceiver, IAsyncService> getEndpointCreator(final IObject endpointParams) {
        IEndpointCreator creator = null;
        try {
            creator = endpoints.get(endpointParams.getValue(typeField));
        } catch (ReadValueException e) {
            //TODO handle
        } catch (InvalidArgumentException e) {
            //TODO handle
        }
        if (creator == null) {
            creator = endpoints.get(defaultEndpointType);
        }
        final IEndpointCreator finalCreator = creator;
        return (receiver) -> finalCreator.create(endpointParams);
    }
}