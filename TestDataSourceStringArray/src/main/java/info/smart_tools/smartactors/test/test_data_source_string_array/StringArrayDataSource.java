package info.smart_tools.smartactors.test.test_data_source_string_array;

import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.test.isource.ISource;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Implementation of {@link ISource}.
 * This implementation gets test data from array of string
 */
public class StringArrayDataSource implements ISource<String[], IObject> {

    private BlockingDeque<IObject> queue;

    /**
     * Default constructor
     */
    public StringArrayDataSource() {
        this.queue = new LinkedBlockingDeque<>();
    }

    @Override
    public void setSource(final String ... strings) {
        for (String item : strings) {
            try {
                this.queue.put(new DSObject(item));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (InvalidArgumentException e) {
                // skip action on broken element
            }
        }
    }

    @Override
    public BlockingDeque<IObject> getQueue() {
        return this.queue;
    }
}
