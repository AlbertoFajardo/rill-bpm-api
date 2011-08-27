/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.api;

import java.util.Collection;

/**
 *
 * @author mengran
 */
public interface TLIGenerator<T> {
    
    Collection<T> generate(String[] source);
    
    boolean supportGeneratePattern(String[] source);
}
