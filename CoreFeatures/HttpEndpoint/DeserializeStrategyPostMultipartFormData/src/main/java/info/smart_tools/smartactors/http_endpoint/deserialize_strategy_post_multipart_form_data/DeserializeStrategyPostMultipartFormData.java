package info.smart_tools.smartactors.http_endpoint.deserialize_strategy_post_multipart_form_data;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.endpoint.interfaces.ideserialize_strategy.IDeserializeStrategy;
import info.smart_tools.smartactors.endpoint.interfaces.ideserialize_strategy.exceptions.DeserializationException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Strategy for deserialization a message from a post request with multipart/form-data content-type.
 * Default strategy.
 */
public class DeserializeStrategyPostMultipartFormData implements IDeserializeStrategy<FullHttpRequest> {

    private static final String IOBJECT_STRATEGY_NAME = "info.smart_tools.smartactors.iobject.iobject.IObject";
    private static final String IFIELD_NAME_STRATEGY_NAME = "info.smart_tools.smartactors.iobject.ifield_name.IFieldName";

    private static final String ENDPOINT_CONFIG_NAME_POSTFIX = "_endpoint-config";

    private static final String FILENAME_FIELD_NAME = "fileName";
    private static final String MESSAGE_FIELD_NAME_KEY = "message";
    private static final String DATA_FIELD_NAME_KEY = "data";
    private static final String UPLOAD_DIRECTORY_FIELD_NAME_KEY = "uploadDirectory";

    private static final String USER_DIR = System.getProperty("user.dir");
    private static final String DEFAULT_UPLOAD_DIR = "uploaded_files_via_http";

    private IFieldName messageFN;
    private IFieldName dataFN;
    private IFieldName uploadDirectoryFN;

    private String endpointName;
    private Map<HttpDataType, IAction<IObject>> actions = new HashMap<>();

    /**
     * Constructor
     * @param endpointName the endpoint name
     */
    public DeserializeStrategyPostMultipartFormData(final String endpointName) {
        try {
            messageFN = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyByNameStrategy(), IFIELD_NAME_STRATEGY_NAME),
                    MESSAGE_FIELD_NAME_KEY
            );
            dataFN = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyByNameStrategy(), IFIELD_NAME_STRATEGY_NAME),
                    DATA_FIELD_NAME_KEY
            );
            uploadDirectoryFN = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyByNameStrategy(), IFIELD_NAME_STRATEGY_NAME),
                    UPLOAD_DIRECTORY_FIELD_NAME_KEY
            );
            this.endpointName = endpointName;
            initializeActions();
        } catch (ResolutionException e) {
            throw new RuntimeException("Could not create instance of DeserializeStrategyPostMultipartFormData", e);
        }
    }

    /**
     * The method that deserializes content of the 'multipart/form-data' request
     * @param request Http request that should be deserialized
     * @return {@link IObject} deserialized json
     * @throws DeserializationException if any error occurs on request body deserialization
     */
    @Override
    @SuppressWarnings("unchecked")
    public IObject deserialize(final FullHttpRequest request) throws DeserializationException {
        try {
            String uploadDirectory = getUploadDirectory();
            IObject message = IOC.resolve(
                    Keys.getKeyByName(IOBJECT_STRATEGY_NAME)
            );
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(
                    new DefaultHttpDataFactory(true), request);
            decoder.getBodyHttpDatas().forEach(data -> {
                try {
                    IAction<IObject> action = actions.get(data.getHttpDataType());
                    IObject environment = IOC.resolve(
                            Keys.getKeyByName(IOBJECT_STRATEGY_NAME)
                    );
                    environment.setValue(messageFN, message);
                    environment.setValue(dataFN, data);
                    environment.setValue(uploadDirectoryFN, uploadDirectory);
                    action.execute(environment);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to deserialize request", e);
                }
            });

            return message;
        } catch (Exception e) {
            throw new DeserializationException("Failed to deserialize request", e);
        }
    }

    private void initializeActions() {
        this.actions.put(
                HttpDataType.Attribute, (IObject env) -> {
                    try {
                        IObject message = (IObject) env.getValue(messageFN);
                        InterfaceHttpData data = (InterfaceHttpData) env.getValue(dataFN);
                        IFieldName fieldName = IOC.resolve(
                                Keys.getKeyByName(IFIELD_NAME_STRATEGY_NAME), data.getName()
                        );
                        message.setValue(fieldName, ((Attribute) data).getValue());
                    } catch (ResolutionException e) {
                        throw new RuntimeException(
                                "Could not resolve field name by key name - 'info.smart_tools.smartactors.iobject.ifield_name.IFieldName'",
                                e
                        );
                    } catch (IOException e) {
                        throw new RuntimeException(
                                "Could not read value from HTTP request by given attribute name",
                                e
                        );
                    } catch (ChangeValueException | InvalidArgumentException e) {
                        throw new RuntimeException(
                                "Could not set attribute value to the message",
                                e
                        );
                    } catch (ReadValueException e) {
                        throw new RuntimeException(
                                "Could not read value from environment.",
                                e
                        );
                    }
                }
        );
        this.actions.put(
            HttpDataType.FileUpload, (IObject env) -> {
                try {
                    IFieldName fieldName = IOC.resolve(
                            Keys.getKeyByName(IFIELD_NAME_STRATEGY_NAME),
                            FILENAME_FIELD_NAME
                    );
                    IObject message = (IObject) env.getValue(messageFN);
                    FileUpload uploadedFile = (FileUpload) env.getValue(dataFN);
                    String uploadDir = (String) env.getValue(uploadDirectoryFN);
                    final File file = new File(uploadDir + File.separator + uploadedFile.getName());
                    Files.createDirectories(Paths.get(uploadDir));
                    try (OutputStream os = new FileOutputStream(file)) {
                        Files.copy(uploadedFile.getFile().toPath(), os);
                        os.flush();
                    }
                    message.setValue(fieldName,  file.getAbsolutePath());
                } catch (ResolutionException e) {
                    throw new RuntimeException(
                            "Could not resolve field name by key name - 'info.smart_tools.smartactors.iobject.ifield_name.IFieldName'",
                            e
                    );
                } catch (ChangeValueException | InvalidArgumentException e) {
                    throw new RuntimeException(
                            "Could not set attribute value to the message",
                            e
                    );
                } catch (IOException e) {
                    throw new RuntimeException(
                            "Issues with IO has occurred.",
                            e
                    );
                } catch (ReadValueException e) {
                    throw new RuntimeException(
                            "Could not read value from environment.",
                            e
                    );
                }
            }
        );
    }

    private String getUploadDirectory() {
        try {
            IObject endpointConfiguration = IOC.resolve(
                    Keys.getKeyByName(this.endpointName + ENDPOINT_CONFIG_NAME_POSTFIX)
            );
            String uploadDirectory = (String) endpointConfiguration.getValue(uploadDirectoryFN);

            return null != uploadDirectory ? uploadDirectory : USER_DIR + File.separator + DEFAULT_UPLOAD_DIR;
        } catch (ResolutionException | ReadValueException | InvalidArgumentException  e) {
            throw new RuntimeException(
                    "Could not initialize upload directory for multipart/form-data deserialization strategy.", e
            );
        }
    }
}
