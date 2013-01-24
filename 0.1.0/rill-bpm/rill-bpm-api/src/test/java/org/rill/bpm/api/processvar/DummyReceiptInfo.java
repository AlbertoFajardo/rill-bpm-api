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
public class DummyReceiptInfo implements Serializable {

    public static final Integer PRE_INVOICE = 0;
    public static final Integer POST_INVOICE = 2;
    public static final Integer PRE_RECEIPT = 1;

    private Integer receiptType;

    public Integer getReceiptType() {
        return receiptType;
    }

    public void setReceiptType(Integer receiptType) {
        this.receiptType = receiptType;
    }

    @Override
    public String toString() {
        return "ReceiptInfo::receiptType:" + receiptType;
    }



}
