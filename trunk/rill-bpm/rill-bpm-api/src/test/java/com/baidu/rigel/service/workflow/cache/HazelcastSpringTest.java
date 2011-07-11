/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.cache;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import java.util.concurrent.BlockingQueue;
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
        
        // Map test ------------------------------------------------------------
        // Entry listener configuration
        IMap<String, Integer> hz1LRUMap = hz1Instance.getMap("hz1LRUMap");
        hz1LRUMap.addEntryListener(new EntryListener<String, Integer>() {
            
            public void entryAdded(EntryEvent<String, Integer> event) {
                logger.info(event.toString());
            }

            public void entryRemoved(EntryEvent<String, Integer> event) {
                logger.info(event.toString());
            }

            public void entryUpdated(EntryEvent<String, Integer> event) {
                logger.info(event.toString());
            }

            public void entryEvicted(EntryEvent<String, Integer> event) {
                logger.info(event.toString());
            }
            
        }, true);
        
        IMap<String, String> hz2LRUMap = hz2Instance.getMap("hz2LRUMap");
        hz2LRUMap.addEntryListener(new EntryListener<String, String>() {
            
            public void entryAdded(EntryEvent<String, String> event) {
                logger.info(event.toString());
            }

            public void entryRemoved(EntryEvent<String, String> event) {
                logger.info(event.toString());
            }

            public void entryUpdated(EntryEvent<String, String> event) {
                logger.info(event.toString());
            }

            public void entryEvicted(EntryEvent<String, String> event) {
                logger.info(event.toString());
            }
            
        }, true);
        
        // Put operations
        hz1LRUMap.put("1", new Integer(1));
        hz1LRUMap.put("2", new Integer(2));
        hz2LRUMap.put("a", "a");
        hz2LRUMap.put("b", "b");
        
        // Cache Assert
        Assert.assertTrue(hz1Instance.getMap("hz1LRUMap").get("1").equals(new Integer(1)));
        Assert.assertTrue(hz2Instance.getMap("hz1LRUMap").get("2").equals(new Integer(2)));
        Assert.assertTrue(hz2Instance.getMap("hz2LRUMap").get("a").equals("a"));
        Assert.assertTrue(hz1Instance.getMap("hz2LRUMap").get("b").equals("b"));
        
        // Queue test ----------------------------------------------------------
        BlockingQueue<String> hz1Queue = hz1Instance.getQueue("hz1Queue");
        try {
            hz1Queue.put("hz1Queue-1");
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        BlockingQueue<String> hz2Queue = hz2Instance.getQueue("hz1Queue");
        try {
            hz2Queue.put("hz1Queue-2");
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        
        // Assert queue
        try {
            Assert.assertEquals("hz1Queue-1", hz1Queue.take());
            Assert.assertEquals("hz1Queue-2", hz1Queue.take());
            Assert.assertEquals(true, hz1Queue.isEmpty());
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }
}
