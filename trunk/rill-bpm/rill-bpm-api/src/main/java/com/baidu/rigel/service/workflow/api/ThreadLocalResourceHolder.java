package com.baidu.rigel.service.workflow.api;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.util.ObjectUtils;

public abstract class ThreadLocalResourceHolder {

    private static final Logger log = Logger.getLogger(ThreadLocalResourceHolder.class.getName());
    private static final ThreadLocal<Map<Object, Object>> threadLocalResources = new ThreadLocal<Map<Object, Object>>();

    //method-----------------------------------
    public static Map<Object, Object> getThreadMap() {

        //Get Current Thread Map
        Map<Object, Object> threadMap = threadLocalResources.get();

        if (threadMap == null) {
            threadMap = new HashMap<Object, Object>();
            threadLocalResources.set(threadMap);
        }

        return threadMap;
    }

    /**
     * Return thread-binding object<br>
     * and the giving key
     * @param key key of object
     * @return thread-binding object
     */
    public static Object getProperty(Object key) {

        if (key == null) {
            throw new IllegalArgumentException("Parameter must not be null");
        }

        Map<Object, Object> queryMap = getThreadMap();

        log.fine("Retrieve Object [" + queryMap.get(key) + "] from thread [" + Thread.currentThread().getName() + "] using key[" + key + "].");
        return queryMap.get(key);
    }

    /**
     * Bind the object to thread. Store in to <code>Map</code> object.
     * @param key key of object
     * @param target bind target
     */
    public static void bindProperty(Object key, Object target) {

        if (key == null) {
            throw new IllegalArgumentException("Parameter must not be null");
        }

        if (getProperty(key) != null) {
            log.log(Level.WARNING, "Already bind [" + key + "] to thread [" + Thread.currentThread().getName() + "], old value:" + getProperty(key) + ", new value:" + (target == null ? "null" : target));
        }

        Map<Object, Object> propertiesMap = getThreadMap();
        //Set value
        propertiesMap.put(key, target);

        log.fine("Bound Object [" + target + "] to thread [" + Thread.currentThread().getName() + "]");
    }

    /**
     * Remove the object from thread.
     * @param key key of object
     */
    public static void unbindProperty(Object key) {

        if (key == null) {
            throw new IllegalArgumentException("Parameter must not be null");
        }

        //Get Current Thread Map
        Map<Object, Object> propertiesMap = getThreadMap();
        if (!propertiesMap.containsKey(key)) {
            log.fine("Removed value [" + key + "] from thread [" + Thread.currentThread().getName() + "]");
        }

        propertiesMap.remove(key);

        log.fine("Removed value [" + key + "] from thread [" + Thread.currentThread().getName() + "]");
    }

    public static String printAll() {

        return ObjectUtils.getDisplayString(getThreadMap());
    }
}
