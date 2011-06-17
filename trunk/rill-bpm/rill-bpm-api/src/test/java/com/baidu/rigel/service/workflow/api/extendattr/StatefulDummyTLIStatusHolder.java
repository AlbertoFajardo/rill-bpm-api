/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.baidu.rigel.service.workflow.api.extendattr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.Assert;

/**
 *
 * @author mengran
 */
public class StatefulDummyTLIStatusHolder {

    public enum TLI_METHOD {
        pre, post, init, after, onError
    }

    private static final Map<String, List<TLI_METHOD>> statusHolder = new HashMap<String, List<TLI_METHOD>>();

    public synchronized static void holdTLIMethodCallStatus(String taskInstance, TLI_METHOD tliMethod) {

        Assert.notNull(taskInstance);
        Assert.notNull(tliMethod);
        if (!statusHolder.containsKey(taskInstance)) {
            List<TLI_METHOD> tliMethods = new ArrayList<TLI_METHOD>();
            statusHolder.put(taskInstance, tliMethods);
        }

        // Record method call
        statusHolder.get(taskInstance).add(tliMethod);
    }

    public synchronized static Map<String, List<TLI_METHOD>> getStatusHolder() {

        return statusHolder;
    }
}
