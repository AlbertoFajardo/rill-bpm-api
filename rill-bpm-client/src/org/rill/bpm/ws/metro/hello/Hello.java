package org.rill.bpm.ws.metro.hello;

import javax.jws.WebService;

import com.sun.xml.ws.api.tx.at.Transactional;

/**
 * Applu Metro WS-AT feature.
 * @author mengran
 *
 */
@WebService
public class Hello {

	@Transactional
	public void helloWS() {
		
		
	}
	
}
