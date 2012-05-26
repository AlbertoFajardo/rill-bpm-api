package org.rill.bpm.api.scaleout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.activiti.engine.impl.util.ReflectUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.api.WorkflowCache;
import org.rill.bpm.api.WorkflowCache.CacheTargetRetriever;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.activiti.ActivitiAccessor;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.target.EmptyTargetSource;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

/**
 * SPI helper and for internal usage only.
 * 
 * @author mengran
 *
 */
public abstract class ScaleoutHelper {

	private static final String SCALEOUT_KEY = "SCALEOUT_KEY";
	
	private static AtomicBoolean needRetrieve = new AtomicBoolean(true);
	// Use count down latch for lazy initialize. MENGRAN at 2012-03-11
	private static CountDownLatch initializing = new CountDownLatch(1);
	private static volatile ConcurrentHashMap<String, WorkflowOperations> scaleoutTarget = new ConcurrentHashMap<String, WorkflowOperations>();
	
	// For cache retrieve. FIXME Need retrieve size from environment
	private static ExecutorService scaleoutFailOver = Executors.newFixedThreadPool(30, new CustomizableThreadFactory("ScaleoutHelper"));
	
	// Add by MENGRAN at 2012-05-23
	public static final long BLIND_RETRIEVE_TIMEOUT = new Long(System.getProperty("activiti.blindRetrieveTimeout", "30"));
	private static final Log LOGGER = LogFactory.getLog(ScaleoutHelper.class);
	
	static class BlindScaleoutKeyRetriever implements CacheTargetRetriever<String>, Callable<String> {
		
		private Object[] arguments;
		private String methodName;
		private ConcurrentHashMap<String, Object> scaleoutTargets;
		
		public BlindScaleoutKeyRetriever(Object[] arguments, String methodName, ConcurrentHashMap<String, Object> scaleoutTargets) {
			super();
			this.arguments = arguments;
			this.methodName = methodName;
			this.scaleoutTargets = scaleoutTargets;
		}

		@Override
		public String getCacheTarget(String key) throws Throwable {
			
			LOGGER.warn("BLIND_RETRIEVE_SCALEOUT_KEY: " + key);
			Future<String> futureResult = scaleoutFailOver.submit(this);
			return futureResult.get(BLIND_RETRIEVE_TIMEOUT, TimeUnit.SECONDS);
		}

		@Override
		public String call() throws Exception {
			
			for (Entry<String, Object> entry : scaleoutTargets.entrySet()) {
				try {
					String result = (String) ReflectUtil.invoke(entry.getValue(), methodName, arguments);
					if (result != null) {
						return entry.getKey();
					}
				} catch (Exception e) {
					// Ignore
				}
			}
			
			LOGGER.error("Even blind, not found scale-out key by " + ObjectUtils.getDisplayString(arguments));
			return null;
		}
		
	}

	static class BlindRetriever implements CacheTargetRetriever<HashMap<String, String>>, Callable<HashMap<String, String>> {
		
		private String infoKey;
		private String methodName;
		private ConcurrentHashMap<String, Object> scaleoutTargets;
		private HashMap<String, String> infos = new HashMap<String, String>();
		
		public BlindRetriever(String infoKey, String methodName, ConcurrentHashMap<String, Object> scaleoutTargets) {
			super();
			this.infoKey = infoKey;
			this.methodName = methodName;
			this.scaleoutTargets = scaleoutTargets;
		}

		@Override
		public HashMap<String, String> getCacheTarget(String key) throws Throwable {
			
			Future<HashMap<String, String>> futureResult = scaleoutFailOver.submit(this);
			return futureResult.get(BLIND_RETRIEVE_TIMEOUT, TimeUnit.SECONDS);
		}

		@SuppressWarnings("unchecked")
		@Override
		public HashMap<String, String> call() throws Exception {
			
			for (Entry<String, Object> entry : scaleoutTargets.entrySet()) {
				try {
					infos = (HashMap<String, String>) ReflectUtil.invoke(entry.getValue(), methodName, new Object[] {infoKey});
				} catch (Exception e) {
					// Ignore
				}
				if (!CollectionUtils.isEmpty(infos)) {
					break;
				}
			}
			return infos;
		}
		
	}
	
	public static ConcurrentHashMap<String, WorkflowOperations> getScaleoutTarget(WorkflowOperations workflowAccessor) {
		
		if (needRetrieve.compareAndSet(true, false)) {
			WorkflowOperations scaleout = workflowAccessor;
			if (scaleout instanceof SpringProxy && 
					((Advised) scaleout).getAdvisors().length == 1 && ((Advised) scaleout).getAdvisors()[0].getAdvice() instanceof ScaleoutInterceptor) {
				for (Entry<String, Object> entry : ((ScaleoutInterceptor) ((Advised) scaleout).getAdvisors()[0].getAdvice()).getTargetsHashMap().entrySet()) {
					scaleoutTarget.put(((WorkflowOperations) entry.getValue()).getName(), (WorkflowOperations) entry.getValue());
				}
			} else {
				scaleoutTarget.put(scaleout.getName(), scaleout);
			}
			initializing.countDown();
		}
		
		try {
			initializing.await();
		} catch (InterruptedException e) {
			// Ignore
		}
		
		return scaleoutTarget;
	}
	
	private static boolean isScaleout(WorkflowOperations scaleout) {
		
		return scaleout instanceof SpringProxy && 
				((Advised) scaleout).getTargetSource().equals(EmptyTargetSource.INSTANCE) && 
				((Advised) scaleout).getAdvisors().length == 1 && ((Advised) scaleout).getAdvisors()[0].getAdvice() instanceof ScaleoutInterceptor;
	}
	
	static String randomRetrieveExecuteTargetHashcode(ConcurrentHashMap<String, Object> scaleoutTargets) {
		
		// Maybe throw NullPointerException
		int randomIndex = new Random().nextInt(scaleoutTargets.size());
		Object randomTarget = new ArrayList<Object>(scaleoutTargets.values()).get(randomIndex);
		return new Integer(randomTarget.hashCode()).toString();
	}
	
	static String retrieveScaleoutTargetHashcode(WorkflowCache<HashMap<String, String>> cache, final ConcurrentHashMap<String, Object> scaleoutTargets, final String scaloutKey) {
		
		// Warning!!! Blind retrieve
		return cache.getOrSetUserInfo(scaloutKey, new BlindScaleoutKeyRetriever(new Object[]{deGenerateScaloutKey(scaloutKey), null}, "getEngineProcessInstanceIdByBOId", scaleoutTargets) {

			@Override
			public String getCacheTarget(String key) throws Throwable {
				String blindRetrieve = super.getCacheTarget(key);
				if (blindRetrieve == null) {
					LOGGER.info("Not found even BLIND_RETRIEVE_SCALEOUT. May be new scaleout key: " + scaloutKey);
					return randomRetrieveExecuteTargetHashcode(scaleoutTargets);
				}
				return blindRetrieve;
			}
			
		});
	}
	
	public static WorkflowOperations determineImpl(WorkflowCache<HashMap<String, String>> cache, WorkflowOperations scaleout, String scaloutKey) {
		
		WorkflowOperations impl = scaleout;
		if (isScaleout(scaleout)) {
			ScaleoutInterceptor scaleoutInterceptor = (ScaleoutInterceptor) ((Advised) scaleout).getAdvisors()[0].getAdvice();
			// Warning!!! Blind retrieve
			String implHash = cache.getOrSetUserInfo(scaloutKey, new BlindScaleoutKeyRetriever(new Object[]{deGenerateScaloutKey(scaloutKey), null}, "getEngineProcessInstanceIdByBOId", scaleoutInterceptor.getTargetsHashMap()));
			impl = (WorkflowOperations) scaleoutInterceptor.getTargetsHashMap().get(implHash);
		}
		
		return ActivitiAccessor.retrieveActivitiAccessorImpl(impl, WorkflowOperations.class);
	}
	
	public static WorkflowOperations determineImplWithBusinessKey(WorkflowCache<HashMap<String, String>> cache, WorkflowOperations scaleout, String businessKey) {
		
		if (!isScaleout(scaleout)) return scaleout;
		
		return determineImpl(cache, scaleout, generateScaloutKey(businessKey));
	}
	
	public static String getBusinessKeyByTaskInstanceId(WorkflowCache<HashMap<String, String>> cache, ConcurrentHashMap<String, Object> scaleoutTargets, String taskInstanceId) {
		
		String businessKey = cache.getTaskRelatedInfo(taskInstanceId, 
				new BlindRetriever(taskInstanceId, "getTaskInstanceInformations", scaleoutTargets)).get(WorkflowOperations.TaskInformations.BUSINESS_OBJECT_ID.name());
		return businessKey;
	}
	
	public static WorkflowOperations determineImplWithTaskInstanceId(WorkflowCache<HashMap<String, String>> cache, WorkflowOperations scaleout, String taskInstanceId) {
		
		if (!isScaleout(scaleout)) return scaleout;
		
		ConcurrentHashMap<String, Object> scaleoutTargets = new ConcurrentHashMap<String, Object>();
		scaleoutTargets.putAll(getScaleoutTarget(scaleout));
		String businessKey = getBusinessKeyByTaskInstanceId(cache, scaleoutTargets, taskInstanceId);
		return determineImplWithBusinessKey(cache, scaleout, businessKey);
	}
	
	public static String getBusinessKeyByProcessInstanceId(WorkflowCache<HashMap<String, String>> cache, ConcurrentHashMap<String, Object> scaleoutTargets, String processInstanceId) {
		
		String businessKey = cache.getProcessRelatedInfo(processInstanceId, 
				new BlindRetriever(processInstanceId, "getProcessInstanceInformations", scaleoutTargets)).get(WorkflowOperations.ProcessInformations.P_BUSINESS_OBJECT_ID.name());
		return businessKey;
	}
	
	public static WorkflowOperations determineImplWithProcessInstanceId(WorkflowCache<HashMap<String, String>> cache, WorkflowOperations scaleout, String processInstanceId) {
		
		if (!isScaleout(scaleout)) return scaleout;
		
		ConcurrentHashMap<String, Object> scaleoutTargets = new ConcurrentHashMap<String, Object>();
		scaleoutTargets.putAll(getScaleoutTarget(scaleout));
		String businessKey = getBusinessKeyByProcessInstanceId(cache, scaleoutTargets, processInstanceId);
		return determineImplWithBusinessKey(cache, scaleout, businessKey);
	}
	
	public static String generateScaloutKey(String businessKey) {
		
		return SCALEOUT_KEY + businessKey;
	}
	
	public static String deGenerateScaloutKey(String scaleoutKey) {
		
		return scaleoutKey.substring(SCALEOUT_KEY.length());
	}
	
}
