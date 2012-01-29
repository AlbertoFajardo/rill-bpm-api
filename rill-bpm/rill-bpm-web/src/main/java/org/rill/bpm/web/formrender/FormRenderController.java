package org.rill.bpm.web.formrender;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.activiti.ActivitiAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/formrender")
public class FormRenderController {

	protected final Log logger = LogFactory.getLog(getClass().getName());
	
	@Resource
	private WorkflowOperations workflowAccessor;
	private ActivitiAccessor activitiAccessor;
	
	public final WorkflowOperations getWorkflowAccessor() {
		return workflowAccessor;
	}

	public final void setWorkflowAccessor(WorkflowOperations workflowAccessor) {
		this.workflowAccessor = workflowAccessor;
		activitiAccessor = ActivitiAccessor.retrieveActivitiAccessorImpl(workflowAccessor, ActivitiAccessor.class);
	}

	@RequestMapping(value = { "/process/{processDefinitionId}" }, method = RequestMethod.GET)
	public ModelAndView process(@PathVariable("processDefinitionId") final String processDefinitionId) {
		
		StartFormData startFormData = activitiAccessor.runExtraCommand(new Command<StartFormData>() {

			@Override
			public StartFormData execute(CommandContext commandContext) {
				ProcessDefinitionEntity pd = commandContext.getProcessDefinitionManager().findLatestProcessDefinitionById(processDefinitionId);
				return activitiAccessor.getFormService().getStartFormData(pd.getId());
			}
		});
		
		ModelAndView mav = new ModelAndView();
		mav.addObject("startFormData", startFormData);
		mav.addObject("processDefinitionId", processDefinitionId);
		mav.setViewName("/formrender/start");
		
		return mav;
	}
	
	@RequestMapping(value = { "/process/{processDefinitionId}/start" }, method = RequestMethod.POST)
	public void start(HttpServletRequest request, final HttpServletResponse response, WebRequest webRequest,
			@PathVariable("processDefinitionId") final String processDefinitionId, 
			@RequestParam("businessObjectId") String businessObjectId, 
			@RequestParam("afterStart") String afterStart) throws Exception {
		
		// FIXME Cross browser issue. IE8 work OK.
		response.setContentType("application/octet-stream; charset=UTF-8");
		try {
			Assert.notNull(businessObjectId, "Please passin businessObjectId using [businessObjectId].");
			String processDefinitionKey = activitiAccessor.runExtraCommand(new Command<String>() {

				@Override
				public String execute(CommandContext commandContext) {
					
					ProcessDefinitionEntity pde = commandContext.getProcessDefinitionManager().findLatestProcessDefinitionById(processDefinitionId);
					Assert.isTrue(pde.getId().equals(processDefinitionId), "Lastest process definition changed when start action. old: " + processDefinitionId + "; lastest:" + pde.getId());
					return pde.getKey();
				}
			});
			
			// FIXME where can i get start user.
			Map<String, String[]> map = webRequest.getParameterMap();
			Map<String, Object> startParams = new HashMap<String, Object>();
			for (Entry<String, String[]> entry : map.entrySet()) {
				Assert.isTrue(entry.getValue() != null && entry.getValue().length == 1, "Supported one value only at current version. " + entry);
				startParams.put(entry.getKey(), entry.getValue()[0]);
			}
			workflowAccessor.createProcessInstance(processDefinitionKey, "FIXME", businessObjectId, startParams);
			String processInstanceId = workflowAccessor.getEngineProcessInstanceIdByBOId(businessObjectId, processDefinitionKey);
			
			// Return and call method
			response.getWriter().println("<script>parent." + afterStart + "('" + processInstanceId  
	                + "', '')</script>"); 
			response.getWriter().flush();
		} catch (Exception e) {
			logger.error("Exception occurred when start process definition id " + processDefinitionId, e);
			// Return and call method
			response.getWriter().println("<script type='text/javascript'>parent." + afterStart + "('" + -1  
	                + "','" + e.getMessage() + "');</script>"); 
			response.getWriter().flush();
		}
	}
	
	@RequestMapping(value = { "/task/{taskInstanceId}" }, method = RequestMethod.GET)
	public ModelAndView task(@PathVariable("taskInstanceId") final String taskInstanceId) {
		
		TaskFormData taskFormData = activitiAccessor.runExtraCommand(new Command<TaskFormData>() {

			@Override
			public TaskFormData execute(CommandContext commandContext) {
				return activitiAccessor.getFormService().getTaskFormData(taskInstanceId);
			}
		});
		
		ModelAndView mav = new ModelAndView();
		mav.addObject("taskFormData", taskFormData);
		mav.setViewName("/formrender/start");
		
		return mav;
	}
	
	@RequestMapping(value = { "/task/{taskInstanceId}/complete" }, method = RequestMethod.POST)
	public ModelAndView complete(@PathVariable("taskInstanceId") final String taskInstanceId) {
		
		TaskFormData taskFormData = activitiAccessor.runExtraCommand(new Command<TaskFormData>() {

			@Override
			public TaskFormData execute(CommandContext commandContext) {
				return activitiAccessor.getFormService().getTaskFormData(taskInstanceId);
			}
		});
		
		ModelAndView mav = new ModelAndView();
		mav.addObject("taskFormData", taskFormData);
		mav.setViewName("/formrender/start");
		
		return mav;
	}
	
}
