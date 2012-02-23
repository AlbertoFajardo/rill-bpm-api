package org.rill.bpm.webclient.hello.service;

import java.util.List;

public interface HelloService {

	void sayHello(String name);
	
	void batchSayHello(String[] names);
	
	List<String> whoSaid();
}
