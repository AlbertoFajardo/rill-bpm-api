/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rill.bpm.api;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.rill.bpm.api.exception.ProcessException;
import org.springframework.util.ObjectUtils;
/**
 * Thread-local resource holder class, for pass data on the thead.
 * @author mengran
 */
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

        log.log(Level.FINE, "Retrieve Object [{0}] from thread [{1}] using key[{2}].", new Object[]{queryMap.get(key), Thread.currentThread().getName(), key});
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
            log.log(Level.SEVERE, "Already bind [{0}] to thread [{1}], old value:{2}, new value:{3}", new Object[]{key, Thread.currentThread().getName(), getProperty(key), target == null ? "null" : target});
            throw new ProcessException("Already bind [" + key + "] to thread [" + Thread.currentThread().getName() + "], old value:{" + getProperty(key) + "}, new value:{" + target == null ? "null" : target + "}");
        }

        Map<Object, Object> propertiesMap = getThreadMap();
        //Set value
        propertiesMap.put(key, target);

        log.log(Level.FINE, "Bound Object [{0}] to thread [{1}]", new Object[]{target, Thread.currentThread().getName()});
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
            log.log(Level.FINE, "Removed value [{0}] from thread [{1}]", new Object[]{key, Thread.currentThread().getName()});
        }

        propertiesMap.remove(key);

        log.log(Level.FINE, "Removed value [{0}] from thread [{1}]", new Object[]{key, Thread.currentThread().getName()});
    }

    public static String printAll() {

        return ObjectUtils.getDisplayString(getThreadMap());
    }
}
