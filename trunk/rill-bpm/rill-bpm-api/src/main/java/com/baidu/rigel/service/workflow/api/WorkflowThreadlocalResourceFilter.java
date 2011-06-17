package com.baidu.rigel.service.workflow.api;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;


/**
 * 
 * 公司名：百度 <br>
 * 系统名：盘古<br>
 * 子系统名: 支持子系统<br>
 * 模块名： 支持派单管理模块<br>
 * 文件名：WorkflowThreadlocalResourceFilter.java<br>
 * 功能说明：清理工作流thread资源<br>
 * 
 * @author mengran
 * @version 1.0
 * @date 上午10:57:19 2011-2-15
 */
public class WorkflowThreadlocalResourceFilter implements Filter {

	public void destroy() {
		
	}

	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain arg2) throws IOException, ServletException {
		
		// Clear work-flow thread-binding resource
		ThreadLocalResourceHolder.getThreadMap().clear();
		
		// Do next
		arg2.doFilter(arg0, arg1);
	}

	public void init(FilterConfig arg0) throws ServletException {
		
	}

}
