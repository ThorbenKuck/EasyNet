package com.github.thorbenkuck.network.encoding;

import com.github.thorbenkuck.network.exceptions.FailedDecodingException;

public interface ObjectDecoder {

	Object apply(byte[] bytes) throws FailedDecodingException;

}
