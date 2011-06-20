/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.cache;

import com.hazelcast.core.HazelcastInstance;
import javax.annotation.Resource;
import junit.framework.Assert;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 *
 * @author Administrator
 */
@ContextConfiguration(locations="applicationContext-hazelcastCache.xml")
public class HazelcastSpringTest extends AbstractJUnit4SpringContextTests {
    
    @Resource
    private HazelcastInstance hz1Instance;
    @Resource
    private HazelcastInstance hz2Instance;

    public HazelcastInstance getHz1Instance() {
        return hz1Instance;
    }

    public void setHz1Instance(HazelcastInstance hz1Instance) {
        this.hz1Instance = hz1Instance;
    }

    public HazelcastInstance getHz2Instance() {
        return hz2Instance;
    }

    public void setHz2Instance(HazelcastInstance hz2Instance) {
        this.hz2Instance = hz2Instance;
    }
    
    @Test
    public void testTwoInstance() {
        
        Assert.assertNotNull(hz1Instance);
        Assert.assertNotNull(hz2Instance);
    }
}
