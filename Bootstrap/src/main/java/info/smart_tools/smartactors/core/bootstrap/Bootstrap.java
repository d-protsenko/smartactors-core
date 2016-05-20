package info.smart_tools.smartactors.core.bootstrap;

import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap.exception.ProcessExecutionException;
import info.smart_tools.smartactors.core.ibootstrap.exception.RevertProcessExecutionException;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sevenbits on 5/20/16.
 */
public class Bootstrap implements IBootstrap<IBootstrapItem> {


    private List<IBootstrapItem> itemStorage = new ArrayList<>();

    @Override
    public void add(final IBootstrapItem bootstrapItem) {
        itemStorage.add(bootstrapItem);
    }

    @Override
    public void start()
            throws ProcessExecutionException {
        try {
            for (IBootstrapItem item : itemStorage) {
                item.executeProcess();
            }
        } catch (Throwable e) {
            throw  new ProcessExecutionException("Could not execute plugin process.", e);
        }
    }

    @Override
    public void revert()
            throws RevertProcessExecutionException {

    }
}
