package info.smart_tools.smartactors.core.create_new_instance_strategy;

import info.smart_tools.smartactors.core.class_container.ClassStorageContainer;
import info.smart_tools.smartactors.core.class_storage.ClassStorage;
import info.smart_tools.smartactors.core.class_storage.IClassStorageContainer;
import info.smart_tools.smartactors.core.iobject.FieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.function.Function;

/**
 * Created by sevenbits on 4/18/16.
 */
public class CreateNewInstanceStrategyTest {

    private static final String CLASS_ID_KEY = "class_id";
    private static final String ARGS_KEY = "value";

    @Test
    public void checkCreationNewInstance()
            throws Exception {

        // region Initialize ClassStorage service locator">
        IClassStorageContainer classContainer = new ClassStorageContainer();
        Field field = ClassStorage.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, classContainer);
        field.setAccessible(false);

        ClassStorage.addClass(Integer.TYPE.getName(), Integer.TYPE);
        // endregion


        IObject args = mock(IObject.class);
        when(args.getValue(new FieldName(CLASS_ID_KEY))).thenReturn(Integer.TYPE.getName());
        when(args.getValue(new FieldName(ARGS_KEY))).thenReturn(1);

        CreateNewInstanceStrategy strategy = new CreateNewInstanceStrategy();
        Function<IObject, Object> f = (IObject param) -> {
            Integer value = new Integer(0);
            try {
                value = (Integer)args.getValue(new FieldName(ARGS_KEY));
            } catch (Exception e) {

            }
            return new Integer(value);
        };
        strategy.register(Integer.TYPE, f);
        int p = (Integer)strategy.resolve(args);
        assertEquals(p, 1);
    }
}
