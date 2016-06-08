package info.smart_tools.smartactors.core.wrapper_generator;

import com.fasterxml.jackson.core.type.TypeReference;

import java.lang.reflect.ParameterizedType;

public abstract class TypeDef<T> extends TypeReference<T> {

    public Class<T> getTypeAsClass() {
        try {
            return (Class<T>) this._type;
        } catch (ClassCastException e) {
            return (Class<T>) ((ParameterizedType) this._type).getRawType();
        }
    }
}
