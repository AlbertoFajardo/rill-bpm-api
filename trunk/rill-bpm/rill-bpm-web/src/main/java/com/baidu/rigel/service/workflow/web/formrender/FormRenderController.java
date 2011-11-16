package com.baidu.rigel.service.workflow.web.formrender;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.form.StartFormData;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.util.json.JSONWriter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.baidu.rigel.service.workflow.api.WorkflowOperations;
import com.baidu.rigel.service.workflow.api.activiti.ActivitiAccessor;

@Controller
@RequestMapping("/formrender")
public class FormRenderController {

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

	@RequestMapping(value = { "/{processDefinitionKey}" }, method = RequestMethod.GET)
	public ModelAndView start(@PathVariable("processDefinitionKey") String processDefinitionKey) {
		
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/formrender/start");
		
		return mav;
	}
	
	@RequestMapping(value = { "/{processDefinitionKey}/start" }, method = RequestMethod.GET)
	public void renderStartForm(HttpServletRequest request,
			HttpServletResponse response, 
			@PathVariable("processDefinitionKey") final String processDefinitionKey) throws Exception {
		
		StartFormData startFormData = activitiAccessor.runExtraCommand(new Command<StartFormData>() {

			@Override
			public StartFormData execute(CommandContext commandContext) {
				ProcessDefinitionEntity pd = commandContext.getProcessDefinitionManager().findLatestProcessDefinitionByKey(processDefinitionKey);
				return activitiAccessor.getFormService().getStartFormData(pd.getId());
			}
		});
		
		// Return historic task information
		response.setContentType("application/json;charset=UTF-8");
		JSONWriter jsonWriter = new JSONWriter(response.getWriter());
		jsonWriter
			.object()
			.key("formProperties")
			.value(startFormData.getFormProperties())
			.key("formKey")
			.value(startFormData.getFormKey())
			.endObject();
		
		response.getWriter().flush();
	}
}
