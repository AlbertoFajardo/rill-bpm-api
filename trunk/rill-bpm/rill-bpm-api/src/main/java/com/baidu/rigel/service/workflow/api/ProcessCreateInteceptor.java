package com.baidu.rigel.service.workflow.api;

import java.util.Map;

import com.baidu.rigel.service.workflow.api.exception.ProcessException;

/**
 * 公司名：百度 <br>
 * 系统名：Rigel直销系统<br>
 * 子系统名: <br>
 * 模块名：HT-SUPPORT <br>
 * 文件名：ProcessCreateInteceptor.java<br>
 * 功能说明: 创建流程拦截器，允许其他类参与到流程操作的过程中，并有机会阻止当前操作。<br>
 * @author mengran
 * @version 1.0.0
 * @date 2010-5-14下午04:38:39
**/
public interface ProcessCreateInteceptor {
	
	Object preOperation(Object modelInfo, Object processStarterInfo, Long businessObjectId, Map<String, Object> startParams) throws ProcessException;
	
	void postOperation(Object engineProcessInstance, Long businessObjectId, Object processStarterInfo) throws ProcessException;
	
}
