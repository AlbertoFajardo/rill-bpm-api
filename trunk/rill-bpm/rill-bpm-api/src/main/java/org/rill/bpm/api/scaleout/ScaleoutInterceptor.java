package org.rill.bpm.api.scaleout;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.api.WorkflowCache;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author mengran
 *
 */
public class ScaleoutInterceptor implements MethodInterceptor, InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass().getName());
	
	static final ThreadLocal<Object> EXECUTE_TARGET = new ThreadLocal<Object>();
	
	private Class<?> serviceInterface;
	private ConcurrentHashMap<String, Object> targets;
	private List<Class<?>> failOverExceptions;
	@Resource(name="workflowCache")
	private WorkflowCache<HashMap<String, String>> workflowCache;
	
	public final WorkflowCache<HashMap<String, String>> getWorkflowCache() {
		return workflowCache;
	}

	public final void setWorkflowCache(WorkflowCache<HashMap<String, String>> workflowCache) {
		this.workflowCache = workflowCache;
	}

	public final List<Class<?>> getFailOverExceptions() {
		return failOverExceptions;
	}

	public final void setFailOverExceptions(List<Class<?>> failOverExceptions) {
		this.failOverExceptions = failOverExceptions;
	}

	public final Class<?> getServiceInterface() {
		return serviceInterface;
	}

	public final void setServiceInterface(Class<?> serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	public final void setTargets(List<Object> targets) {
		this.targets = new ConcurrentHashMap<String, Object>(targets.size());
		for (Object target : targets) {
			this.targets.put(new Integer(target.hashCode()).toString(), target);
		}
	}

	public final ConcurrentHashMap<String, Object> getTargetsHashMap() {
		return targets;
	}

	protected Object retrieveExecuteTarget(MethodInvocation invocation) throws Throwable {
		
		Object transactionAssociationTarget = null;
		
		// Scaleout binding
		transactionAssociationTarget = scaleoutKeyBindingExecuteTarget(invocation);
		
		return transactionAssociationTarget;
	}
	
	private Object scaleoutKeyBindingExecuteTarget(MethodInvocation invocation) {
		
		Method method = invocation.getMethod();
		ScaleoutKeySource source = AnnotationUtils.findAnnotation(method, ScaleoutKeySource.class);
		if (source == null) {
			return randomRetrieveExecuteTarget();
		}
		
		String businessKey = null;
		// FIXME MENGRAN. Need refactor using design pattern such as Visitor
		// Handle annotation
		if (source.value().equals(ScaleoutKeySource.RETRIEVE_TYPE.BO_ID)) {
			businessKey = invocation.getArguments()[source.index()].toString();
		} else if (source.value().equals(ScaleoutKeySource.RETRIEVE_TYPE.TASK_INSTANCE_ID)) {
			String taskInstanceId = invocation.getArguments()[source.index()].toString();
			businessKey = ScaleoutHelper.getBusinessKeyByTaskInstanceId(workflowCache, getTargetsHashMap(), taskInstanceId);
		} else if (source.value().equals(ScaleoutKeySource.RETRIEVE_TYPE.PROCESS_INSTANCE_ID)) {
			String processInstanceId = invocation.getArguments()[source.index()].toString();
			businessKey = ScaleoutHelper.getBusinessKeyByProcessInstanceId(workflowCache, getTargetsHashMap(), processInstanceId);
		}
		
		Assert.notNull(businessKey, "Can not retrieve scalout key for this invocation: " + ObjectUtils.getDisplayString(invocation));
		// FIXME MENGRAN. Need test if targetHashcode is null and cache is exists.
		String targetHashcode = null;
		try {
			targetHashcode = new Integer(randomRetrieveExecuteTarget().hashCode()).toString();
		} catch (Exception e) {
			logger.warn("Can not retrieve execute target. We pray that have cached.");
		}
		
		// Cache scale-out key and execute target hash code
		String cachedTargetHashcode = workflowCache.getOrSetUserInfo(ScaleoutHelper.generateScaloutKey(businessKey), targetHashcode);
		logger.debug("cachedTargetHashcode:" + cachedTargetHashcode + ", targetHashcode:" + targetHashcode);
		
		return targets.get(cachedTargetHashcode);
	}
	
	private Object randomRetrieveExecuteTarget() {
		
		// Maybe throw NullPointerException
		int randomIndex = new Random().nextInt(this.targets.size());
		return new ArrayList<Object>(this.targets.values()).get(randomIndex);
	}
	
	protected void doFailOverExecuteTarget(Object failTarget) {
		
		// warm-cache
	}
	
	protected boolean needFailOverException(Throwable t) {
		
		if (CollectionUtils.isEmpty(failOverExceptions)) {
			return false;
		}
		
		for (Class<?> clazz : failOverExceptions) {
			if (clazz.equals(t.getClass()) || (t.getCause() != null && clazz.equals(t.getCause().getClass()))) {
				return true;
			}
		}
		
		return false;
	}
	
	private void exceptionHandler(Object failTarget, Throwable t) throws Throwable {
		
		if (needFailOverException(t)) {
			doFailOverExecuteTarget(failTarget);
		}
		
		throw t;
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {

		if (AopUtils.isToStringMethod(invocation.getMethod())) {
			return "Load balance for " + serviceInterface.getName() + ", targets:" + ObjectUtils.getDisplayString(targets);
		}
		
		if (this.targets.isEmpty()) {
			throw new IllegalStateException("No target is available, Please wait a moment.");
		}
		
		Object result = null;
		Object executeTarget = null;
		try {
			executeTarget = retrieveExecuteTarget(invocation);
			// Binding execute target to thread
			EXECUTE_TARGET.set(executeTarget);
			// Do execute
			result = ReflectionUtils.invokeMethod(invocation.getMethod(), executeTarget, invocation.getArguments());
		} catch (Throwable e) {
			exceptionHandler(executeTarget, e);
		} finally {
			EXECUTE_TARGET.set(null);
		}
		
		return result;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		Assert.notEmpty(this.targets, "No any target to load balance.");
		
	}

}
