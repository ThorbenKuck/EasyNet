package com.github.thorbenkuck.network;

import com.github.thorbenkuck.network.exceptions.FailedEncodingException;

public interface ObjectEncoder {

	byte[] apply(Object object) throws FailedEncodingException;

}
