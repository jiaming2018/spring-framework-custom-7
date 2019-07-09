package com.kkb.springframework.beans.config;

import java.io.InputStream;

public interface Resource {
	
	boolean isCanRead(String location);

	InputStream getInputStream();
}
