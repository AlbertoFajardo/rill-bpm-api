package org.rill.bpm.webclient.wsat;

import java.util.UUID;

import javax.transaction.xa.Xid;

import org.junit.Test;

import com.sun.xml.ws.tx.at.runtime.TransactionIdHelper;

public class XidWsatIdTests {

	@Test
	public void wsatId2Xid() {
		
		String wsatId = "4D2-313332343534333231383035362D30";
		Xid xid = TransactionIdHelper.getInstance().wsatid2xid(wsatId);
		
		System.out.println(new String(xid.getGlobalTransactionId()));
		
		System.out.println(UUID.randomUUID().toString().replaceAll("-", ""));
	}

}
