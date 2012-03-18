package org.rill.bpm.ws.lb;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.transaction.Transaction;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.api.scaleout.ScaleoutInterceptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import com.sun.xml.ws.tx.at.common.TransactionManagerImpl;

/**
 * @author mengran
 *
 */
public class LoadBalanceInterceptor extends ScaleoutInterceptor implements MethodInterceptor, InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass().getName());
	
	private ConcurrentHashMap<Integer, Object> jtaTransactionBindingMap = new ConcurrentHashMap<Integer, Object>();

	protected Object retrieveExecuteTarget(MethodInvocation invocation) throws Throwable {
		
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
				transactionAssociationTarget = super.retrieveExecuteTarget(invocation);
				Object putResult = jtaTransactionBindingMap.putIfAbsent(hash, transactionAssociationTarget);
				if (putResult == null) {
					logger.info("Associate transaction:" + jtaTransaction + " with LB target:" + transactionAssociationTarget);
				} else {
					logger.warn("We sure that transaction and thread is one-one relationship strategy, current environment is not??");
				}
			}
		} else {
			// Maybe not in JTA environment
			transactionAssociationTarget = super.retrieveExecuteTarget(invocation);
		}
		
		return transactionAssociationTarget;
	}
	
	protected void doFailOverExecuteTarget(Object failTarget) {
		
		// Remove from service list
		getTargetsHashMap().remove(new Integer(failTarget.hashCode()).toString());
		
		// Record fail time
		SIMPLE_FAIL_OVER.addFailOverTarget(failTarget);
	}
	
	private final SimpleFailOver SIMPLE_FAIL_OVER = new SimpleFailOver(); 
	
	private class SimpleFailOver implements Runnable {
		
		private final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		
		private ConcurrentHashMap<Object, Date> failTimeMap = new ConcurrentHashMap<Object, Date>();
		AtomicReference<ScheduledExecutorService> ses = new AtomicReference<ScheduledExecutorService>();
		
		public void addFailOverTarget(Object failTarget) {
			
			// For first time
			boolean isFirstTime = ses.compareAndSet(null, Executors.newScheduledThreadPool(1, new CustomizableThreadFactory("SIMPLE_FAIL_OVER")));
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
			Object addSuccessfully = getTargetsHashMap().putIfAbsent(new Integer(releaseTarget.hashCode()).toString(), releaseTarget);
			logger.info("Add releaseTarget to service list " + addSuccessfully);
		}
		
	}

}
