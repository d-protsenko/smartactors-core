package info.smart_tools.smartactors.core.async_operation_collection;

import info.smart_tools.smartactors.core.async_operation_collection.task.GetAsyncOperationTask;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IOC.class, Keys.class})
public class AsyncOperationCollectionTest {

    @Before
    public void prepare () throws ResolutionException, ReadValueException, ChangeValueException, InvalidArgumentException {
        mockStatic(IOC.class);
        mockStatic(Keys.class);
    }

}