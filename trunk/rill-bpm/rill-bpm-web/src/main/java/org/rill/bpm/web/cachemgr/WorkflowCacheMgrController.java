package org.rill.bpm.web.cachemgr;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.util.json.JSONWriter;
import org.rill.bpm.api.scaleout.ScaleoutHelper;
import org.rill.bpm.web.ScaleoutControllerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/cachemgr")
public class WorkflowCacheMgrController extends ScaleoutControllerSupport {

	private CacheManager cacheManager;

	public final CacheManager getCacheManager() {
		return cacheManager;
	}
	
	@Autowired(required=true)
	public final void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	
	@RequestMapping("/")
	public void init(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		response.setContentType("application/json;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		
		JSONWriter jsonWriter = new JSONWriter(out);
		
		jsonWriter
			.object()
			.key("cacheNames")
			.value(ObjectUtils.getDisplayString(cacheManager.getCacheNames()))
			.endObject();
		out.flush();
	}
	
	@RequestMapping(value = { "/{cacheName}/{cacheKey}" }, method = RequestMethod.GET)
	public void getCacheValue(HttpServletRequest request,
			HttpServletResponse response, 
			@PathVariable(value="cacheName") final String cacheName, 
			@PathVariable(value="cacheKey") final String cacheKey) throws Exception {
		
		response.setContentType("application/json;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		
		JSONWriter jsonWriter = new JSONWriter(out);
		Cache cache = cacheManager.getCache(cacheName);
		
		jsonWriter
			.object()
			.key("type")
			.value(RequestMethod.GET.name())
			.key("cacheName")
			.value(cacheName)
			.key("cacheKey")
			.value(cacheKey)
			.key("cacheValue")
			.value(ObjectUtils.getDisplayString(cache.get(cacheKey) == null ? null : cache.get(cacheKey).get()))
			.endObject();
		out.flush();
		
	}
	
	@RequestMapping(value = { "/{cacheName}/{cacheKey}/{cacheValue}" }, method = RequestMethod.PUT)
	public void updateCacheValue(HttpServletRequest request,
			HttpServletResponse response, 
			@PathVariable(value="cacheName") final String cacheName, 
			@PathVariable(value="cacheKey") final String cacheKey,
			@PathVariable(value="cacheValue") final String cacheValue) throws Exception {
		
		response.setContentType("application/json;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		
		JSONWriter jsonWriter = new JSONWriter(out);
		Cache cache = cacheManager.getCache(cacheName);
		cache.put(cacheKey, cacheValue);
		
		jsonWriter
			.object()
			.key("type")
			.value(RequestMethod.PUT.name())
			.key("cacheName")
			.value(cacheName)
			.key("cacheKey")
			.value(cacheKey)
			.key("cacheValue")
			.value(ObjectUtils.getDisplayString(cache.get(cacheKey) == null ? null : cache.get(cacheKey).get()))
			.endObject();
		out.flush();
		
	}
	
	@RequestMapping(value = { "/{cacheName}/SCALEOUT_KEY" }, method = RequestMethod.PUT)
	public void rebuildScaleout(HttpServletRequest request,
			HttpServletResponse response, @PathVariable(value="cacheName") final String cacheName) throws Exception {
		
		// FIXME MENGRAN. only support PERSISTENT_CACHE
		response.setContentType("application/json;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		
		Cache cache = cacheManager.getCache(cacheName);
		Assert.notNull(cache, "Can not get cache by name " + cacheName);
		
		List<String> add = new ArrayList<String>();
		List<String> update = new ArrayList<String>();
		
		for (Entry<String, ProcessEngine> entry : ProcessEngines.getProcessEngines().entrySet()) {
			List<HistoricProcessInstance> list = entry.getValue().getHistoryService().createHistoricProcessInstanceQuery().unfinished().orderByProcessInstanceStartTime().asc().list();
			LOGGER.info("--- Start rebuild scaleout key(" + list.size() + ") for " + entry.getKey());
			for (HistoricProcessInstance hpi : list) {
				if (hpi.getBusinessKey() != null) {
					ValueWrapper vw = cache.get(ScaleoutHelper.generateScaloutKey(hpi.getBusinessKey()));
					if (vw == null) {
						LOGGER.info("Add scaleout key " + entry.getKey() + "'s hashcode for business key " + hpi.getBusinessKey());
						cache.put(ScaleoutHelper.generateScaloutKey(hpi.getBusinessKey()), new Integer(entry.getKey().hashCode()).toString());
						add.add(ScaleoutHelper.generateScaloutKey(hpi.getBusinessKey()) + "=" + new Integer(entry.getKey().hashCode()).toString());
					} else {
						if (new Integer(entry.getKey().hashCode()).toString().equals(vw.get().toString())) {
							LOGGER.info("No-op scaleout key " + entry.getKey() + "'s hashcode for business key " + hpi.getBusinessKey());
						} else {
							LOGGER.info("Update scaleout key " + entry.getKey() + "'s hashcode for business key " + hpi.getBusinessKey());
							String old = vw.get().toString();
							cache.put(ScaleoutHelper.generateScaloutKey(hpi.getBusinessKey()), new Integer(entry.getKey().hashCode()).toString());
							update.add(ScaleoutHelper.generateScaloutKey(hpi.getBusinessKey()) + "=" + new Integer(entry.getKey().hashCode()).toString() + " " + old);
						}
					}
				}
			}
		}
		
		JSONWriter jsonWriter = new JSONWriter(out);
		jsonWriter
			.object()
			.key("type")
			.value("MIX")
			.key("add")
			.value(add)
			.key("update")
			.value(update)
			.endObject();
		out.flush();
		
	}
	
	@RequestMapping(value = { "/{cacheName}/{cacheKey}" }, method = RequestMethod.DELETE)
	public void deleteCacheValue(HttpServletRequest request,
			HttpServletResponse response, 
			@PathVariable(value="cacheName") final String cacheName, 
			@PathVariable(value="cacheKey") final String cacheKey) throws Exception {
		
		response.setContentType("application/json;charset=UTF-8");
		final PrintWriter out = response.getWriter();
		
		JSONWriter jsonWriter = new JSONWriter(out);
		Cache cache = cacheManager.getCache(cacheName);
		cache.evict(cacheKey);
		
		jsonWriter
			.object()
			.key("type")
			.value(RequestMethod.DELETE.name())
			.key("cacheName")
			.value(cacheName)
			.key("cacheKey")
			.value(cacheKey)
			.key("cacheValue")
			.value(ObjectUtils.getDisplayString(cache.get(cacheKey) == null ? null : cache.get(cacheKey).get()))
			.endObject();
		out.flush();
		
	}
	
}
