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
        
        // Put operations
        hz1Instance.getMap("hz1LRUMap").put("1", new Integer(1));
        hz1Instance.getMap("hz1LRUMap").put("2", new Integer(2));
        hz2Instance.getMap("hz2LRUMap").put("a", "a");
        hz2Instance.getMap("hz2LRUMap").put("b", "b");
        
        // Cache Assert
        Assert.assertTrue(hz1Instance.getMap("hz1LRUMap").get("1").equals(new Integer(1)));
        Assert.assertTrue(hz1Instance.getMap("hz1LRUMap").get("a").equals("a"));
        Assert.assertTrue(hz2Instance.getMap("hz2LRUMap").get("2").equals(new Integer(2)));
        Assert.assertTrue(hz2Instance.getMap("hz2LRUMap").get("b").equals("b"));
    }
}
