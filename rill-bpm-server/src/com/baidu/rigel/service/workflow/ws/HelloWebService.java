/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.ws;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 *
 * @author mengran
 */
@WebService(serviceName = "HelloWebService")
public class HelloWebService {

    /** This is a sample web service operation */
    @WebMethod(operationName = "hello")
    public String hello(@WebParam(name = "name") String txt) {
        return "Hello " + txt + " !";
    }
}
