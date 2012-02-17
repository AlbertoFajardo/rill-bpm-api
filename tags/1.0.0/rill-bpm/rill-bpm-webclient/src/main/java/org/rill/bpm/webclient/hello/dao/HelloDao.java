package org.rill.bpm.webclient.hello.dao;

import java.util.List;

public interface HelloDao {

	/**
	 * Persist who say hello.
	 * 
	 * @param name who say hello.
	 * @throws IllegalArgumentException when insert failed.
	 */
	void createHello(String name);
	
	List<String> whoSaid();
}
