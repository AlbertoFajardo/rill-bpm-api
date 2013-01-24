package org.rill.bpm.api.infinispan_d;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.rill.bpm.api.WorkflowCache;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;


/**
 * Usage: Keep only one test method that start with "getOrSetUserInfo" available.
 * 
 * @author mengran
 *
 */
@ContextConfiguration(value="classpath:org/rill/bpm/api/infinispan-d/infinispan.d.activiti.cfg.xml")
public class WorkflowCacheImplClusterTests extends AbstractJUnit4SpringContextTests {

	@Resource
	private WorkflowCache workflowCache;
	
	private String pickHost() throws RuntimeException {
        String currentAddress = null;
        try {
            final String localAddress = System.getProperty("hazelcast.local.localAddress");
            if (localAddress != null) {
                currentAddress = InetAddress.getByName(localAddress.trim()).getHostAddress();
            }
            if (currentAddress == null) {
	            final Enumeration<NetworkInterface> enums = NetworkInterface.getNetworkInterfaces();
	            interfaces:
	            while (enums.hasMoreElements()) {
	                final NetworkInterface ni = enums.nextElement();
	                final Enumeration<InetAddress> e = ni.getInetAddresses();
	                while (e.hasMoreElements()) {
	                    final InetAddress inetAddress = e.nextElement();
	                    if (inetAddress instanceof Inet4Address) {
	                        if (!inetAddress.isLoopbackAddress()) {
	                            currentAddress = inetAddress.getHostAddress();
	                            break interfaces;
	                        }
	                    }
	                }
	            }
            }
            if (currentAddress == null) {
                currentAddress = "127.0.0.1";
            }
            final InetAddress inetAddress = InetAddress.getByName(currentAddress);
            logger.info("Pick host " + inetAddress.getHostAddress());
            return inetAddress.getHostAddress();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
	
//	@Test
	public void getOrSetUserInfoDefaultConfiguration() {
		
		String rillmeng = workflowCache.getOrSetUserInfo("what's your name", "getOrSetUserInfoDefaultConfiguration");
		Assert.assertEquals("getOrSetUserInfoDefaultConfiguration", rillmeng);
		
		String kingOfPop = workflowCache.getOrSetUserInfo("King of pop", "Michael Jackson");
		Assert.assertEquals("Michael Jackson", kingOfPop);
		
		kingOfPop = workflowCache.getOrSetUserInfo("King of pop", "Rill Meng");
		Assert.assertEquals("Michael Jackson", kingOfPop);
		
	}
	
//	@Test
	public void getOrSetUserInfoOnlyInitialHostsConfiguration() {
		
		logger.info(System.getProperty("jgroups.tcpping.initial_hosts"));
		if (System.getProperty("jgroups.tcpping.initial_hosts") == null) {
			String host = pickHost();
			System.setProperty("jgroups.tcpping.initial_hosts", host + "[7800]," + host + "[7801]");
			logger.info(System.getProperty("jgroups.tcpping.initial_hosts"));
		}
		
		String rillmeng = workflowCache.getOrSetUserInfo("what's your name", "getOrSetUserInfoOnlyInitialHostsConfiguration");
		Assert.assertEquals("getOrSetUserInfoOnlyInitialHostsConfiguration", rillmeng);
		
		String kingOfPop = workflowCache.getOrSetUserInfo("King of pop", "Michael Jackson");
		Assert.assertEquals("Michael Jackson", kingOfPop);
		
		kingOfPop = workflowCache.getOrSetUserInfo("King of pop", "Rill Meng");
		Assert.assertEquals("Michael Jackson", kingOfPop);
		
	}
	
	@Test
	public void getOrSetUserInfoInitialHostsAndBindAddrConfiguration() {
		
		logger.info(System.getProperty("jgroups.tcpping.initial_hosts"));
		if (System.getProperty("jgroups.tcpping.initial_hosts") == null) {
			String host = pickHost();
			System.setProperty("jgroups.tcpping.initial_hosts", host + "[7800]," + host + "[7801]");
			logger.info(System.getProperty("jgroups.tcpping.initial_hosts"));
			System.setProperty("jgroups.bind_addr", host);
			logger.info(System.getProperty("jgroups.bind_addr"));
		}
		
		String rillmeng = workflowCache.getOrSetUserInfo("what's your name", "getOrSetUserInfoInitialHostsAndBindAddrConfiguration");
		Assert.assertEquals("getOrSetUserInfoInitialHostsAndBindAddrConfiguration", rillmeng);
		
		String kingOfPop = workflowCache.getOrSetUserInfo("King of pop", "Michael Jackson");
		Assert.assertEquals("Michael Jackson", kingOfPop);
		
		kingOfPop = workflowCache.getOrSetUserInfo("King of pop", "Rill Meng");
		Assert.assertEquals("Michael Jackson", kingOfPop);
		
	}
}
