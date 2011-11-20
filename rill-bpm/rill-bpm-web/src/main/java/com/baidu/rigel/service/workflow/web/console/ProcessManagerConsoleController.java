package com.baidu.rigel.service.workflow.web.console;

import java.util.zip.ZipInputStream;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.repository.DeploymentBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.baidu.rigel.service.workflow.api.WorkflowOperations;
import com.baidu.rigel.service.workflow.api.activiti.ActivitiAccessor;

@Controller
@RequestMapping("/console")
public class ProcessManagerConsoleController {

	@Resource
	private WorkflowOperations workflowAccessor;
	private ActivitiAccessor activitiAccessor;

	public final WorkflowOperations getWorkflowAccessor() {
		return workflowAccessor;
	}

	public final void setWorkflowAccessor(WorkflowOperations workflowAccessor) {
		this.workflowAccessor = workflowAccessor;
		activitiAccessor = ActivitiAccessor.retrieveActivitiAccessorImpl(
				workflowAccessor, ActivitiAccessor.class);
	}

	@RequestMapping(value = { "/" }, method = RequestMethod.GET)
	public ModelAndView console() {

		return new ModelAndView("/console");
	}

	@RequestMapping(value = { "/deploy" }, method = RequestMethod.POST)
	public void deploy(HttpServletRequest request,
			HttpServletResponse response, @RequestParam("deployFile") MultipartFile deployFile, 
			@RequestParam("afterDeploy") String afterDeploy) throws Exception {
		
		// FIXME Cross browser issue. IE8 work OK.
		response.setContentType("application/octet-stream; charset=UTF-8");
		try {
			if (deployFile.isEmpty()) {
				throw new IllegalArgumentException("Deployed file is empty." + deployFile.getOriginalFilename());
			}
			DeploymentBuilder deploymentBuilder = activitiAccessor
					.getRepositoryService().createDeployment().name(deployFile.getOriginalFilename());
			if(deployFile.getOriginalFilename().toUpperCase().endsWith("ZIP")) {
				deploymentBuilder.addZipInputStream(new ZipInputStream(deployFile.getInputStream()));
			} else if (deployFile.getOriginalFilename().toUpperCase().endsWith("BPMN20\\.XML")) {
				deploymentBuilder.addInputStream(deployFile.getOriginalFilename(), deployFile.getInputStream());
			} else {
				throw new IllegalArgumentException("Support zip/bpmn20.xml file only. " + deployFile.getOriginalFilename());
			}
			String deploymentId = deploymentBuilder.deploy().getId();
			
			// Return and call method
			response.getWriter().println("<script>parent." + afterDeploy + "('" + deploymentId  
	                + "', '')</script>"); 
			response.getWriter().flush();
		} catch (Exception e) {
			// Return and call method
			response.getWriter().println("<script type='text/javascript'>parent." + afterDeploy + "('" + -1  
	                + "','" + e.getMessage() + "');</script>"); 
			response.getWriter().flush();
		}
		
	}
}
