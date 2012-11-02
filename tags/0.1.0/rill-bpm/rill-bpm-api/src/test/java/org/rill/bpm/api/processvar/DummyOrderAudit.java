/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rill.bpm.api.processvar;

import java.io.Serializable;

/**
 *
 * @author mengran
 */
public class DummyOrderAudit implements Serializable {

    public static final Integer AGREE = 0;
    public static final Integer REJECT = 1;

    /**
     * Set default action is AGREE
     */
    private Integer auditAction = AGREE;
    private String auditorName;

    public String getAuditorName() {
        return auditorName;
    }

    public void setAuditorName(String auditorName) {
        this.auditorName = auditorName;
    }

    public Integer getAuditAction() {
        return auditAction;
    }

    public void setAuditAction(Integer auditAction) {
        this.auditAction = auditAction;
    }

    @Override
    public String toString() {
        return "OrderAudit::auditAction:" + auditAction;
    }

}
