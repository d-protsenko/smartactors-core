package info.smart_tools.smartactors.test.test_http_endpoint;

import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.util.Map;

/**
 * Created by sevenbits on 8/15/16.
 */
public class TestFullHttpRequest implements FullHttpRequest {

    private IFieldName protocolVersionTextFieldName;
    private IFieldName keepAliveFieldName;
    private IFieldName headersFieldName;

    private IObject request;

    public TestFullHttpRequest(final IObject testRequest) throws InvalidArgumentException {
        try {
            this.protocolVersionTextFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "protocolVersion");
            this.keepAliveFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "keepAlive");
            this.headersFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "headers");

            this.request = testRequest;
        } catch (Exception e) {
            throw new InvalidArgumentException("Could not initialize new instance of TestFullHttpRequest");
        }
    }

    @Override
    public FullHttpRequest copy() {
        return null;
    }

    @Override
    public FullHttpRequest duplicate() {
        return null;
    }

    @Override
    public FullHttpRequest retainedDuplicate() {
        return null;
    }

    @Override
    public FullHttpRequest replace(final ByteBuf content) {
        return null;
    }

    @Override
    public FullHttpRequest retain(final int increment) {
        return null;
    }

    @Override
    public FullHttpRequest retain() {
        return null;
    }

    @Override
    public FullHttpRequest touch() {
        return null;
    }

    @Override
    public FullHttpRequest touch(final Object hint) {
        return null;
    }

    @Override
    public FullHttpRequest setProtocolVersion(final HttpVersion version) {
        return null;
    }

    @Override
    public FullHttpRequest setMethod(final HttpMethod method) {
        return null;
    }

    @Override
    public FullHttpRequest setUri(final String uri) {
        return null;
    }

    @Override
    public ByteBuf content() {
        return null;
    }

    @Override
    public HttpMethod getMethod() {
        return null;
    }

    @Override
    public HttpMethod method() {
        return null;
    }

    @Override
    public String getUri() {
        return null;
    }

    @Override
    public String uri() {
        return null;
    }

    @Override
    public HttpVersion getProtocolVersion() {
        return this.protocolVersion();
    }

    @Override
    public HttpVersion protocolVersion() {
        try {
            return new HttpVersion((String) this.request.getValue(this.protocolVersionTextFieldName), (boolean) this.request.getValue(this.keepAliveFieldName));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public HttpHeaders headers() {
        try {
            HttpHeaders parsedHeaders = new DefaultHttpHeaders();
            Map<IFieldName, String> testHeaders = (Map<IFieldName, String>) this.request.getValue(this.headersFieldName);
            for (Map.Entry<IFieldName, String> header : testHeaders.entrySet()) {
                parsedHeaders.add(header.getKey().toString(), header.getValue());
            }
            return parsedHeaders;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public HttpHeaders trailingHeaders() {
        return null;
    }

    @Override
    public DecoderResult getDecoderResult() {
        return this.decoderResult();
    }

    @Override
    public DecoderResult decoderResult() {
        return null;
    }

    @Override
    public void setDecoderResult(final DecoderResult result) {

    }

    @Override
    public int refCnt() {
        return 0;
    }

    @Override
    public boolean release() {
        return false;
    }

    @Override
    public boolean release(final int decrement) {
        return false;
    }
}
