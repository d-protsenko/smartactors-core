package info.smart_tools.smartactors.core.security.encoding.codecs;

import info.smart_tools.smartactors.core.security.encoding.decoders.ICharSequenceDecoder;
import info.smart_tools.smartactors.core.security.encoding.encoders.ICharSequenceEncoder;

/**
 * Marker-interface for charset codec
 */
public interface ICharSequenceCodec extends ICharSequenceEncoder, ICharSequenceDecoder { }
