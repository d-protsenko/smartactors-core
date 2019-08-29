package info.smart_tools.smartactors.http_endpoint.deserialize_strategy_post_multipart_form_data;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.endpoint.interfaces.ideserialize_strategy.IDeserializeStrategy;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.ioc.string_ioc_key.Key;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeserializeStrategyPostMultipartFormDataTest {

    private static final String IFIELD_NAME_STRATEGY_NAME = "info.smart_tools.smartactors.iobject.ifield_name.IFieldName";

    private static final String ENDPOINT_CONFIG_NAME_POSTFIX = "_endpoint-config";
    private static final String UPLOAD_DIRECTORY_FIELD_NAME_KEY = "uploadDirectory";

    private static final String BOUNDARY = "aaa-000-xxx";

    private static final String ARG1 = "arg1";
    private static final String ARG2 = "arg2";
    private static final String ARG3 = "arg3";
    private static final String FILENAME_FIELD = "fileName";

    private static final String VALUE1 = "value1";
    private static final String VALUE2 = "value2";
    private static final String VALUE3 = "value3";

    private static final String ENDPOINT_NAME_1 = "testEndpoint1";
    private static final String ENDPOINT_NAME_2 = "testEndpoint2";
    private static final String DEFAULT_UPLOAD_DIR = "uploaded_files_via_http";
    private static final String UPLOADED_FILENAME = "test.txt";
    private static final String SECOND_ENDPOINT_UPLOAD_DIRECTORY = "secondDirectory";

    private static final String content = "123";

    private IStrategyContainer container = new StrategyContainer();
    private String requestBodyWOFile = "" +
            "--" + BOUNDARY +"\n" +
            "Content-Disposition: form-data; name=\""+ UPLOADED_FILENAME + "\"; filename=\"example.txt\"\n\n" +
            content + "\n" +
            "--" + BOUNDARY +"\n" +
            "Content-Disposition: form-data; name=\"" + ARG1 + "\"\n\n" +
            VALUE1 + "\n" +
            "--" + BOUNDARY +"\n" +
            "Content-Disposition: form-data; name=\"" + ARG2 + "\"\n\n" +
            VALUE2 + "\n" +
            "--" + BOUNDARY +"\n" +
            "Content-Disposition: form-data; name=\"" + ARG3 + "\"\n\n" +
            VALUE3 + "\n" +
            "--" + BOUNDARY + "--";

    private IFieldName arg1_FN;
    private IFieldName arg2_FN;
    private IFieldName arg3_FN;
    private IFieldName fileName_FN;
    private IFieldName uploadDirectory_FN;

    @Before
    public void init()
            throws Exception {
        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(keyOfMainScope);
        scope.setValue(IOC.getIocKey(), this.container);
        ScopeProvider.setCurrentScope(scope);

        IOC.register(
                IOC.getKeyForKeyByNameStrategy(),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return new Key((String) a[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );

        IOC.register(
                IOC.resolve(
                        IOC.getKeyForKeyByNameStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                        new ApplyFunctionToArgumentsStrategy(
                                (args) -> {
                                    try {
                                        return new FieldName((String) args[0]);
                                    } catch (Exception e) {
                                        throw new RuntimeException("exception", e);
                                    }
                                }
                        )
        );
        IOC.register(
                Keys.getKeyByName("info.smart_tools.smartactors.iobject.iobject.IObject"),
                new ApplyFunctionToArgumentsStrategy(args -> {
                    if (args.length == 0) {
                        return new DSObject();
                    } else if (args.length == 1 && args[0] instanceof String) {
                        try {
                            return new DSObject((String) args[0]);
                        } catch (InvalidArgumentException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        throw new RuntimeException("Invalid arguments for IObject creation.");
                    }
                }));

        IObject config1 = mock(IObject.class);
        IObject config2 = mock(IObject.class);
        IOC.register(
                Keys.getKeyByName(ENDPOINT_NAME_1 + ENDPOINT_CONFIG_NAME_POSTFIX),
                new SingletonStrategy(config1)
        );
        IOC.register(
                Keys.getKeyByName(ENDPOINT_NAME_2 + ENDPOINT_CONFIG_NAME_POSTFIX),
                new SingletonStrategy(config2)
        );
        when(config2.getValue(any())).thenReturn(SECOND_ENDPOINT_UPLOAD_DIRECTORY);
        arg1_FN = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyByNameStrategy(), IFIELD_NAME_STRATEGY_NAME),
                ARG1
        );
        arg2_FN = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyByNameStrategy(), IFIELD_NAME_STRATEGY_NAME),
                ARG2
        );
        arg3_FN = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyByNameStrategy(), IFIELD_NAME_STRATEGY_NAME),
                ARG3
        );
        fileName_FN = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyByNameStrategy(), IFIELD_NAME_STRATEGY_NAME),
                FILENAME_FIELD
        );
        uploadDirectory_FN = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyByNameStrategy(), IFIELD_NAME_STRATEGY_NAME),
                UPLOAD_DIRECTORY_FIELD_NAME_KEY
        );
    }

    @Test
    public void check_deserialization_with_default_file_path()
            throws Exception {

        IDeserializeStrategy<FullHttpRequest> strategy = new DeserializeStrategyPostMultipartFormData(
                "testEndpoint1"
        );
        FullHttpRequest request = createMultipartRequest();

        IObject message = strategy.deserialize(request);
        assertEquals(message.getValue(arg1_FN), VALUE1);
        assertEquals(message.getValue(arg2_FN), VALUE2);
        assertEquals(message.getValue(arg3_FN), VALUE3);

        File uploadedFile = new File(DEFAULT_UPLOAD_DIR + File.separator + UPLOADED_FILENAME);
        assertTrue(uploadedFile.exists());
        assertEquals(message.getValue(fileName_FN), uploadedFile.getAbsolutePath());
        String fileContent = new String(Files.readAllBytes(Paths.get(uploadedFile.getPath())));
        assertEquals(content, fileContent);
        uploadedFile.delete();
        Files.delete(Paths.get(DEFAULT_UPLOAD_DIR));
    }

    @Test
    public void check_different_upload_directories_for_endpoints_differently_configured()
            throws Exception {

        IDeserializeStrategy<FullHttpRequest> strategy1 = new DeserializeStrategyPostMultipartFormData(
                "testEndpoint1"
        );
        IDeserializeStrategy<FullHttpRequest> strategy2 = new DeserializeStrategyPostMultipartFormData(
                "testEndpoint2"
        );
        FullHttpRequest request = createMultipartRequest();

        strategy1.deserialize(request);
        strategy2.deserialize(request);

        File uploadedFile1 = new File(DEFAULT_UPLOAD_DIR + File.separator + UPLOADED_FILENAME);
        assertTrue(uploadedFile1.exists());
        File uploadedFile2 = new File(SECOND_ENDPOINT_UPLOAD_DIRECTORY + File.separator + UPLOADED_FILENAME);
        assertTrue(uploadedFile2.exists());
        uploadedFile1.delete();
        uploadedFile2.delete();
        Files.delete(Paths.get(DEFAULT_UPLOAD_DIR));
        Files.delete(Paths.get(SECOND_ENDPOINT_UPLOAD_DIRECTORY));
    }

    private FullHttpRequest createMultipartRequest() {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_0,
                HttpMethod.POST,
                "http://localhost"
        );
        request.setDecoderResult(DecoderResult.SUCCESS);
        request.headers().add(HttpHeaderNames.CONTENT_TYPE, "multipart/form-data; boundary=" + BOUNDARY);
        request.headers().add(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        request.content().writeBytes(requestBodyWOFile.getBytes(CharsetUtil.UTF_8));

        return request;
    }
}
