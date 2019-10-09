package info.smart_tools.smartactors.base.interfaces.transformation;

import info.smart_tools.smartactors.base.interfaces.transformation.exception.TransformationException;

public interface ITransformable<T, R> {

    R transformTo(T obj) throws TransformationException;

    T transformFrom(R obj) throws TransformationException;
}
