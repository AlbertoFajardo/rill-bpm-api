package org.rill.bpm.ws;

import java.util.Random;

import javax.transaction.xa.Xid;

import org.junit.Assert;
import org.junit.Test;

import com.sun.xml.ws.tx.at.internal.XidImpl;
import com.sun.xml.ws.tx.at.runtime.TransactionIdHelper;
import com.sun.xml.ws.tx.at.tube.WSATClientHelper;

public class GlobalTxIdPrefixTest {

	@Test
	public void testRandom() {
		
		for (int i = 0; i < 1000; i++) {
			
			Random a = new Random();
			Random b = new Random();
			
			int inta = a.nextInt(1000);
			int intb = b.nextInt(1000);
			System.out.println(i + "= a " + inta + ", " + "b " + intb);
			Assert.assertTrue(inta != intb);
		}
		
	}
	
	@Test
	public void globaltxidPrefix() {
		
		int cnt = -1;
		while (true) {
			byte[] globalTxId = new WSATClientHelper().globalTransactionId();
			Xid xid = new XidImpl(1234, globalTxId, new byte[]{});
	        String txId = TransactionIdHelper.getInstance().xid2wsatid(xid);
	        System.out.println(txId);
	        System.out.println(txId.getBytes().length);
	        
	        cnt++;
	        if (cnt == 1000) {
	        	System.out.println("exit at " + cnt);
	        	break;
	        }
	        
	        Assert.assertTrue(txId.getBytes().length < 64);
		}
		
	}

}
