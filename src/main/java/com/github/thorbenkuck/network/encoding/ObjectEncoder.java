package com.github.thorbenkuck.network.encoding;

import com.github.thorbenkuck.network.exceptions.FailedEncodingException;

public interface ObjectEncoder {

	byte[] apply(Object object) throws FailedEncodingException;

}
