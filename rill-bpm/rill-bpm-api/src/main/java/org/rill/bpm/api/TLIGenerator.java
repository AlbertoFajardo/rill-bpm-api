/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rill.bpm.api;

import java.util.Collection;

/**
 *
 * @author mengran
 */
public interface TLIGenerator<T> {
    
    Collection<T> generate(String[] source);
    
    boolean supportGeneratePattern(String[] source);
}
