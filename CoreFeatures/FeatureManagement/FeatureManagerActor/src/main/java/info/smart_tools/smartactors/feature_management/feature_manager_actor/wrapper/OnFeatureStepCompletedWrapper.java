package info.smart_tools.smartactors.feature_management.feature_manager_actor.wrapper;

import info.smart_tools.smartactors.feature_management.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;

import java.util.Map;

/**
 * Created by sevenbits on 12/5/16.
 */
public interface OnFeatureStepCompletedWrapper {

    IFeature getFeature()
            throws ReadValueException;

    IMessageProcessor getMessageProcessor()
            throws ReadValueException;

    Map<IMessageProcessor, IFeature> getFeatureProcess()
            throws ReadValueException;

}
