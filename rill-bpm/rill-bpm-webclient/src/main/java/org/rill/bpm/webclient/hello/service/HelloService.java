package org.rill.bpm.webclient.hello.service;

import java.util.List;

public interface HelloService {

	void sayHello(String name);
	
	List<String> whoSaid();
}
