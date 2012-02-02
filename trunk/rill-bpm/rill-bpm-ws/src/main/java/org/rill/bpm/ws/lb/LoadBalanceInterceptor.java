package org.rill.bpm.ws.lb;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.transaction.Transaction;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import com.sun.xml.ws.tx.at.common.TransactionManagerImpl;

/**
 * @author mengran
 *
 */
public class LoadBalanceInterceptor implements MethodInterceptor, InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass().getName());
	
	private Class<?> serviceInterface;
	private CopyOnWriteArrayList<Object> targets;
	private List<Class<?>> failOverExceptions;
	private ConcurrentHashMap<Integer, Object> jtaTransactionBindingMap = new ConcurrentHashMap<Integer, Object>();

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

	protected Object retrieveExecuteTarget() throws Throwable {
		
		Object transactionAssociationTarget = null;
		// Process JTA Transaction Binding feature
		Transaction jtaTransaction = null;
		if ((jtaTransaction = TransactionManagerImpl.getInstance().getTransaction()) != null) {
			int hash = jtaTransaction.hashCode();
			if (jtaTransactionBindingMap.containsKey(hash)) {
				transactionAssociationTarget = jtaTransactionBindingMap.get(hash);
				logger.debug("Use LB target:" + transactionAssociationTarget + " for transaction:" + jtaTransaction);
			} else {
				// Have not associate with transaction
				transactionAssociationTarget = randomRetrieveExecuteTarget();
				Object putResult = jtaTransactionBindingMap.putIfAbsent(hash, transactionAssociationTarget);
				if (putResult == null) {
					logger.info("Associate transaction:" + jtaTransaction + " with LB target:" + transactionAssociationTarget);
				} else {
					logger.warn("We sure that transaction and thread is one-one relationship strategy, current environment is not??");
				}
			}
		} else {
			// Maybe not in JTA environment
			transactionAssociationTarget = randomRetrieveExecuteTarget();
		}
		
		return transactionAssociationTarget;
	}
	
	private Object randomRetrieveExecuteTarget() {
		
		// Maybe throw NullPointerException
		return getTargets().get(new Random().nextInt(getTargets().size()));
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
		Object executeTarget = null;
		try {
			executeTarget = retrieveExecuteTarget();
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
				logger.warn("Fail target object" + failTarget + " in fail-over list already[" + existsDate + "].");
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
