package info.smart_tools.smartactors.core.sql_commons;

import info.smart_tools.smartactors.core.iobject.IFieldName;

public class ParamContainer {
    private IFieldName name;
    private int count;

    private ParamContainer(final IFieldName name, final int count) {
        this.name = name;
        this.count = count;
    }

    public static ParamContainer create(final IFieldName name, final int count) {
        return new ParamContainer(name, count);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ParamContainer && name.equals(((ParamContainer) obj).name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public IFieldName getName() {
        return name;
    }

    public int getCount() {
        return count;
    }
}
