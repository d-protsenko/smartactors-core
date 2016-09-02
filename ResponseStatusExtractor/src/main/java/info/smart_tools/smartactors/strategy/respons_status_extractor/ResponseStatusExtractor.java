package info.smart_tools.smartactors.strategy.respons_status_extractor;

import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iresponse_status_extractor.IResponseStatusExtractor;

/**
 * Simple implementation of {@link IResponseStatusExtractor}.
 */
public class ResponseStatusExtractor implements IResponseStatusExtractor {

    private static final int DEFAULT_STATUS_CODE = 500;

    private IFieldName httpResponseStatusCodeFieldName;
    private IFieldName contextFieldName;

    public ResponseStatusExtractor() throws ResolutionException {
        this.httpResponseStatusCodeFieldName = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "httpResponseStatusCode"
        );
        this.contextFieldName = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()), "context"
        );
    }

    @Override
    public Integer extract(final IObject environment) {
        Integer status = null;
        try {
            IObject context = (IObject) environment.getValue(this.contextFieldName);
            status = (Integer) context.getValue(this.httpResponseStatusCodeFieldName);
        } catch (Throwable e) {
            // do nothing
        }
        if (null == status) {
            status = DEFAULT_STATUS_CODE;
        }

        return status;
    }
}
