package org.rill.bpm.ws.lb;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

public class LoadBalanceInterceptor implements MethodInterceptor, InitializingBean {

	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	private Class<?> serviceInterface;
	private CopyOnWriteArrayList<Object> targets;
	private List<Class<?>> failOverExceptions;

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

	public final CopyOnWriteArrayList<Object> getTargets() {
		return targets;
	}

	public final void setTargets(CopyOnWriteArrayList<Object> targets) {
		this.targets = targets;
	}

	protected Object retrieveExecuteTarget() {
		
		Object randomTarget = getTargets().get(new Random().nextInt(getTargets().size()));
		return randomTarget;
	}
	
	protected void doFailOverExecuteTarget(Object failTarget) {
		
		// Remove from service list
		targets.remove(failTarget);
		
		// Record fail time
		SIMPLE_FAIL_OVER.addFailOverTarget(failTarget);
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
		
		if (getTargets().isEmpty()) {
			throw new IllegalStateException("No target is available, Please wait a moment.");
		}
		
		Object result = null;
		Object executeTarget = retrieveExecuteTarget();
		try {
			result = ReflectionUtils.invokeMethod(invocation.getMethod(), executeTarget, invocation.getArguments());
		} catch (Throwable e) {
			exceptionHandler(executeTarget, e);
		}
		
		return result;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		Assert.notEmpty(getTargets(), "No any target to load balance.");
	}
	
	private final SimpleFailOver SIMPLE_FAIL_OVER = new SimpleFailOver(); 
	
	private class SimpleFailOver implements Runnable {
		
		private final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		
		private ConcurrentHashMap<Object, Date> failTimeMap = new ConcurrentHashMap<Object, Date>();
		AtomicReference<ScheduledExecutorService> ses = new AtomicReference<ScheduledExecutorService>();
		
		public void addFailOverTarget(Object failTarget) {
			
			// For first time
			boolean isFirstTime = ses.compareAndSet(null, Executors.newScheduledThreadPool(1));
			if (isFirstTime) {
				// Start release thread...
				logger.info("Start fail over thread...");
				ses.get().scheduleAtFixedRate(this, 1, 5, TimeUnit.MINUTES);
			}
			
			Date existsDate  = failTimeMap.putIfAbsent(failTarget, new Date());
			if (existsDate != null) {
				logger.warning("Fail target object" + failTarget + " in fail-over list already[" + existsDate + "].");
			}
		}

		@Override
		public void run() {
			
			// try to release a fail target.
			if (failTimeMap.isEmpty()) {
				logger.info("No fail target, great...");
				return;
			}
			
			Entry<Object, Date> releaseEntry = failTimeMap.entrySet().iterator().next();
			logger.info("Release a fail target " + releaseEntry.getKey() + 
					", fail time " + SIMPLE_DATE_FORMAT.format(releaseEntry.getValue()) + 
					" release time " + SIMPLE_DATE_FORMAT.format(new Date()));
			Object releaseTarget = releaseEntry.getKey();
			// Release it
			failTimeMap.remove(releaseTarget);
			boolean addSuccessfully = targets.addIfAbsent(releaseTarget);
			logger.info("Add releaseTarget to service list " + addSuccessfully);
		}
		
		
	}

}
