/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.baidu.rigel.service.workflow.api.support;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import com.baidu.rigel.service.workflow.api.TaskExecutionContext;
import com.baidu.rigel.service.workflow.api.WorkflowOperations;
import com.baidu.rigel.service.workflow.api.activiti.ActivitiAccessor;
import com.baidu.rigel.service.workflow.api.activiti.support.GenericEngineDrivenTLIAdapter;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;

/**
 *
 * @author mengran
 */
public class GenericEngineDrivenTLIAdapterTest {

    public GenericEngineDrivenTLIAdapterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    public static final class MyDTO {

        public MyDTO() {
        }
        
        private String propertyA;

        public String getPropertyA() {
            return propertyA;
        }

        public MyDTO setPropertyA(String propertyA) {
            this.propertyA = propertyA;
            return this;
        }

    }

    interface IMyServiceA {

        public String serviceA();
        public String serviceA(MyDTO myDto);
        public String serviceA(int i, String str);
        public String serviceA(int i, String str, Integer ii);
        public String serviceA(int i, String str, MyDTO myDto);

    }
    static class MyServiceA implements IMyServiceA {

            public String serviceA() {

                return "Hello serviceA.serviceA()";
            };

            public String serviceA(MyDTO myDto) {

                return "Hello serviceA.serviceA(myDto) with property: " + myDto.getPropertyA();
            }

            public String serviceA(int i, String str) {

                return "Hello serviceA.serviceA(int, str) with property: " + i + str;
            }

            public String serviceA(int i, String str, Integer ii) {

                return "Hello serviceA.serviceA(int, str, Integer) with property: " + i + str + ii;
            }

            public String serviceA(int i, String str, MyDTO myDto) {

                return "Hello serviceA.serviceA(int, str, myDto) with property: " + i + str + myDto.getPropertyA();
            }
        }

    static class MyBeanFactory implements BeanFactory {

        public Object getBean(String name) throws BeansException {

            return new MyServiceA();
        }

        public Object getBean(String name, Class requiredType) throws BeansException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object getBean(Class requiredType) throws BeansException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object getBean(String name, Object... args) throws BeansException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean containsBean(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isTypeMatch(String name, Class targetType) throws NoSuchBeanDefinitionException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String[] getAliases(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    static final class MyProxyBeanFactory extends MyBeanFactory {

        @Override
        public Object getBean(String name) throws BeansException {

            MyServiceA serviceA = new MyServiceA();
            ProxyFactory factory = new ProxyFactory(serviceA);
            factory.addAdvice(new MyAdvice());

            return factory.getProxy();
        }


    }

    static final class MyAdvice implements MethodBeforeAdvice {

        public void before(Method method, Object[] args, Object target) throws Throwable {

            System.out.println("My Advice");
        }

    }

    static final class MyWorkflowAccessor implements WorkflowOperations {

        public HashMap<String, String> getTaskInstanceExtendAttrs(String engineTaskInstanceId) {

            if (engineTaskInstanceId == null) {
            HashMap<String, String> extendAttrs = new HashMap<String, String>();
                extendAttrs.put(ActivitiAccessor.TASK_SERVICE_INVOKE_EXPRESSION, "serviceA.serviceA()");

                return extendAttrs;
            } else if (engineTaskInstanceId.equals("primitive")) {
                HashMap<String, String> extendAttrs = new HashMap<String, String>();
                extendAttrs.put(ActivitiAccessor.TASK_SERVICE_INVOKE_EXPRESSION, "serviceA.serviceA(" + int.class.getName() + ", " + String.class.getName() + ")");

                return extendAttrs;
            } else if (engineTaskInstanceId.equals("primitiveOrWrapper")) {
                HashMap<String, String> extendAttrs = new HashMap<String, String>();
                extendAttrs.put(ActivitiAccessor.TASK_SERVICE_INVOKE_EXPRESSION, "serviceA.serviceA(" + int.class.getName() + ", " + String.class.getName() + ", " + Integer.class.getName() + ")");

                return extendAttrs;
            } else if (engineTaskInstanceId.equals("compsite")) {
                HashMap<String, String> extendAttrs = new HashMap<String, String>();
                extendAttrs.put(ActivitiAccessor.TASK_SERVICE_INVOKE_EXPRESSION, "serviceA.serviceA(" + int.class.getName() + ", " + String.class.getName() + ", " + MyDTO.class.getName() + ")");

                return extendAttrs;
            } else {
                HashMap<String, String> extendAttrs = new HashMap<String, String>();
                extendAttrs.put(ActivitiAccessor.TASK_SERVICE_INVOKE_EXPRESSION, "serviceA.serviceA(" + MyDTO.class.getName() + ")");

                return extendAttrs;
            }
        }

        public List<String> createProcessInstance(String processDefinitionKey, String processStarter, String businessObjectId, Map<String, String> startParams) throws ProcessException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void terminalProcessInstance(String engineProcessInstanceId, String operator, String reason) throws ProcessException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void suspendProcessInstance(String engineProcessInstanceId, String operator, String reason) throws ProcessException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void resumeProcessInstance(String engineProcessInstanceId, String operator, String reason) throws ProcessException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public List<String> completeTaskInstance(String engineTaskInstanceId, String operator, Map<String, String> workflowParams) throws ProcessException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Map<String, List<String>> batchCompleteTaskIntances(Map<String, Map<String, String>> batchDTO, String operator) throws ProcessException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getTaskNameByDefineId(String processDefinitionKey, String taskDefineId) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Set<String> getProcessInstanceVariableNames(String engineProcessInstanceId) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void abortTaskInstance(String engineTaskInstanceId) throws ProcessException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String obtainTaskRole(String engineTaskInstanceId) throws ProcessException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void reassignTaskExecuter(String engineProcessInstanceId, String engineTaskInstanceId, String oldExecuter, String newExecuter) throws ProcessException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

		@Override
		public String getEngineProcessInstanceIdByBOId(String businessObjectId,
				String processDefinitionKey) throws ProcessException {
			// TODO Auto-generated method stub
			return null;
		}

        

    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
    public void engineDrivenEmptyParams() {

        GenericEngineDrivenTLIAdapter adapter = new GenericEngineDrivenTLIAdapter();
        adapter.setBeanFactory(new MyBeanFactory());
        adapter.setWorkflowAccessor(new MyWorkflowAccessor());

        TaskExecutionContext context = new TaskExecutionContext();
        Map<String, Object> returnMap = adapter.preComplete(context);
        Assert.assertEquals("Hello serviceA.serviceA()", returnMap.get(WorkflowOperations.ENGINE_DRIVEN_TASK_RETURN_DATA_KEY));
    }
    
    @Test
    public void engineDrivenMyDTO() {

        GenericEngineDrivenTLIAdapter adapter = new GenericEngineDrivenTLIAdapter();
        adapter.setBeanFactory(new MyBeanFactory());
        adapter.setWorkflowAccessor(new MyWorkflowAccessor());

        TaskExecutionContext context = new TaskExecutionContext();
        context.setTaskInstanceId("have parameter");
        context.getWorkflowParams().put(WorkflowOperations.ENGINE_DRIVEN_TASK_FORM_DATA_KEY, new MyDTO().setPropertyA("Rill Meng"));
        Map<String, Object> returnMap = adapter.preComplete(context);
        Assert.assertEquals("Hello serviceA.serviceA(myDto) with property: Rill Meng", returnMap.get(WorkflowOperations.ENGINE_DRIVEN_TASK_RETURN_DATA_KEY));
    }

    @Test
    public void engineDrivenNoDTO() {

        GenericEngineDrivenTLIAdapter adapter = new GenericEngineDrivenTLIAdapter();
        adapter.setBeanFactory(new MyBeanFactory());
        adapter.setWorkflowAccessor(new MyWorkflowAccessor());

        TaskExecutionContext context = new TaskExecutionContext();
        context.setTaskInstanceId("have parameter");
//        context.getWorkflowParams().put(WorkflowOperations.ENGINE_DRIVEN_TASK_FORM_DATA_KEY, new MyDTO("Rill Meng"));
        try {
            Map<String, Object> returnMap = adapter.preComplete(context);
        } catch(Exception e) {
            Assert.assertTrue(e.getMessage().contains(", so we need a actual parameter."));
        }
    }

    @Test
    public void engineDrivenProxiedService() {

        GenericEngineDrivenTLIAdapter adapter = new GenericEngineDrivenTLIAdapter();
        adapter.setBeanFactory(new MyProxyBeanFactory());
        adapter.setWorkflowAccessor(new MyWorkflowAccessor());

        TaskExecutionContext context = new TaskExecutionContext();
        context.setTaskInstanceId("have parameter");
        context.getWorkflowParams().put(WorkflowOperations.ENGINE_DRIVEN_TASK_FORM_DATA_KEY, new MyDTO().setPropertyA("Rill Meng"));
        Map<String, Object> returnMap = adapter.preComplete(context);
        Assert.assertEquals("Hello serviceA.serviceA(myDto) with property: Rill Meng", returnMap.get(WorkflowOperations.ENGINE_DRIVEN_TASK_RETURN_DATA_KEY));
    }

    @Test
    public void engineDrivenPrimitive() {

        GenericEngineDrivenTLIAdapter adapter = new GenericEngineDrivenTLIAdapter();
        adapter.setBeanFactory(new MyProxyBeanFactory());
        adapter.setWorkflowAccessor(new MyWorkflowAccessor());

        TaskExecutionContext context = new TaskExecutionContext();
        context.setTaskInstanceId("primitive");
        context.getWorkflowParams().put(WorkflowOperations.ENGINE_DRIVEN_TASK_FORM_DATA_KEY, new Object[] {2, "3"});
        Map<String, Object> returnMap = adapter.preComplete(context);
        Assert.assertEquals("Hello serviceA.serviceA(int, str) with property: 23", returnMap.get(WorkflowOperations.ENGINE_DRIVEN_TASK_RETURN_DATA_KEY));
    }

    @Test
    public void engineDrivenPrimitiveOrWrapper() {

        GenericEngineDrivenTLIAdapter adapter = new GenericEngineDrivenTLIAdapter();
        adapter.setBeanFactory(new MyProxyBeanFactory());
        adapter.setWorkflowAccessor(new MyWorkflowAccessor());

        TaskExecutionContext context = new TaskExecutionContext();
        context.setTaskInstanceId("primitiveOrWrapper");
        context.getWorkflowParams().put(WorkflowOperations.ENGINE_DRIVEN_TASK_FORM_DATA_KEY, new Object[] {2, "3", new Integer(4)});
        Map<String, Object> returnMap = adapter.preComplete(context);
        Assert.assertEquals("Hello serviceA.serviceA(int, str, Integer) with property: 234", returnMap.get(WorkflowOperations.ENGINE_DRIVEN_TASK_RETURN_DATA_KEY));
    }

    @Test
    public void engineDrivenCompsite() {

        GenericEngineDrivenTLIAdapter adapter = new GenericEngineDrivenTLIAdapter();
        adapter.setBeanFactory(new MyProxyBeanFactory());
        adapter.setWorkflowAccessor(new MyWorkflowAccessor());

        TaskExecutionContext context = new TaskExecutionContext();
        context.setTaskInstanceId("compsite");
        context.getWorkflowParams().put(WorkflowOperations.ENGINE_DRIVEN_TASK_FORM_DATA_KEY, new Object[] {2, "3", new MyDTO().setPropertyA("Rill Meng")});
        Map<String, Object> returnMap = adapter.preComplete(context);
        Assert.assertEquals("Hello serviceA.serviceA(int, str, myDto) with property: 23Rill Meng", returnMap.get(WorkflowOperations.ENGINE_DRIVEN_TASK_RETURN_DATA_KEY));
    }

}