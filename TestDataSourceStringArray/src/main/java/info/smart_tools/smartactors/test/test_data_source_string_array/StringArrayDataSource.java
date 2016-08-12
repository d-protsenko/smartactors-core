package info.smart_tools.smartactors.test.test_data_source_string_array;

import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.test.isource.ISource;
import info.smart_tools.smartactors.test.isource.exception.SourceExtractionException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of {@link ISource}.
 * This implementation gets test data from array of string
 */
public class StringArrayDataSource implements ISource<String[], IObject> {

    private List<IObject> dataSource;

    @Override
    public void setSource(final String ... strings)
            throws SourceExtractionException {
        try {
            this.dataSource = new ArrayList<IObject>();
            for (String item : strings) {
                this.dataSource.add(new DSObject(item));
            }
        } catch (InvalidArgumentException e) {
            throw new SourceExtractionException("Could not extract data from given string array.", e);
        }
    }

    @Override
    public Iterator<IObject> iterator() {
        return dataSource.iterator();
    }
}
