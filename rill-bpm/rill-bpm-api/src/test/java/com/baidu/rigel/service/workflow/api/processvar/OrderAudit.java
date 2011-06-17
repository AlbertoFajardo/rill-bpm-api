/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.baidu.rigel.service.workflow.api.processvar;

import java.io.Serializable;

/**
 *
 * @author mengran
 */
public class OrderAudit implements Serializable {

    public static final Integer AGREE = 0;
    public static final Integer REJECT = 1;

    /**
     * Set default action is AGREE
     */
    private Integer auditAction = AGREE;

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
