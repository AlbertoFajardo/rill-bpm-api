package nu.com.rill.analysis.report.cluster;

import junit.framework.Assert;

import org.jgroups.stack.IpAddress;
import org.junit.Test;

public class SessionStickyFilter2Tests {

	@Test
	public void ims() throws Throwable {
		
		String ims1 = "10.38.100.27:8800";
		IpAddress imsIpAdress = new IpAddress(ims1.split(":")[0], new Integer(ims1.split(":")[1]));
		System.out.println(ims1 + "                    " + imsIpAdress.hashCode());
		
		String ims2 = "10.50.133.26:8800";
		imsIpAdress = new IpAddress(ims2.split(":")[0], new Integer(ims2.split(":")[1]));
		System.out.println(ims2 + "                    " + imsIpAdress.hashCode());
	}
	
	@Test
	public void compass() throws Throwable {
		
		String ims1 = "10.38.100.27:7800";
		IpAddress imsIpAdress = new IpAddress(ims1.split(":")[0], new Integer(ims1.split(":")[1]));
		System.out.println(ims1 + "                    " + imsIpAdress.hashCode());
		
		String ims2 = "10.50.133.26:7800";
		imsIpAdress = new IpAddress(ims2.split(":")[0], new Integer(ims2.split(":")[1]));
		System.out.println(ims2 + "                    " + imsIpAdress.hashCode());
		
	}
	
	@Test
	public void compassQa() throws Throwable {
		
		String ims1 = "127.0.0.1:7800";
		IpAddress imsIpAdress = new IpAddress(ims1.split(":")[0], new Integer(ims1.split(":")[1]));
		System.out.println(ims1 + "                    " + imsIpAdress.hashCode());
		Assert.assertEquals(2130714233, imsIpAdress.hashCode());
		
		String ims2 = "127.0.0.1:7801";
		imsIpAdress = new IpAddress(ims2.split(":")[0], new Integer(ims2.split(":")[1]));
		System.out.println(ims2 + "                    " + imsIpAdress.hashCode());
		Assert.assertEquals(2130714234, imsIpAdress.hashCode());
		
	}
	
	@Test
	public void rigeldashboard() throws Throwable {
		
		String ims1 = "10.26.212.12:7801";
		IpAddress imsIpAdress = new IpAddress(ims1.split(":")[0], new Integer(ims1.split(":")[1]));
		System.out.println(ims1 + "                    " + imsIpAdress.hashCode());
		
		Assert.assertEquals(169538181, imsIpAdress.hashCode());
		
	}

}
