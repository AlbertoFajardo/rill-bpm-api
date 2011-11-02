package com.baidu.rigel.service.workflow.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.baidu.rigel.service.workflow.api.WorkflowOperations.XStreamSerializeHelper;
import com.baidu.rigel.service.workflow.api.processvar.DummyOrderAudit;


public class XStreamUtilsTest {

	@Test
	public void testSerialize() {
		
		DummyOrderAudit orderAudit = new DummyOrderAudit();
		orderAudit.setAuditorName("mengran");
		orderAudit.setAuditAction(DummyOrderAudit.REJECT);
		
		String xml = XStreamSerializeHelper.serializeXml("orderAudit", orderAudit);
		Assert.assertEquals("<orderAudit>\n  <auditAction>1</auditAction>\n  <auditorName>mengran</auditorName>\n</orderAudit>", xml);
		
		Map<String, String> extendAttrs = new LinkedHashMap<String, String>();
		extendAttrs.put("auditAction", "1");
		extendAttrs.put("auditorName", "mengran");
		String extendAttrsXml = XStreamSerializeHelper.serializeXml("extendAttrs", extendAttrs);
		Assert.assertEquals("<extendAttrs>\n  <entry>\n    <string>auditAction</string>\n    <string>1</string>\n  </entry>\n  <entry>\n    <string>auditorName</string>\n    <string>mengran</string>\n  </entry>\n</extendAttrs>", extendAttrsXml);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDeseialize() {
		
		String xml = "<orderAudit><auditorName>mengran</auditorName><auditAction>1</auditAction></orderAudit>";
		DummyOrderAudit orderAudit = XStreamSerializeHelper.deserializeObject(xml, "orderAudit", DummyOrderAudit.class);
		Assert.assertNotNull(orderAudit);
		Assert.assertEquals("mengran", orderAudit.getAuditorName());
		Assert.assertEquals(DummyOrderAudit.REJECT, orderAudit.getAuditAction());
		
		Map<String, String> extendAttrs = new LinkedHashMap<String, String>();
		extendAttrs.put("auditAction", "1");
		extendAttrs.put("auditorName", "mengran");
		String extendAttrsXml = "<extendAttrs>\n  <entry>\n    <string>auditAction</string>\n    <string>1</string>\n  </entry>\n  <entry>\n    <string>auditorName</string>\n    <string>mengran</string>\n  </entry>\n</extendAttrs>";
		Map<String, String> deserializeMap = XStreamSerializeHelper.deserializeObject(extendAttrsXml, "extendAttrs", Map.class);
		
		Assert.assertEquals(extendAttrs, deserializeMap);
	}
	
}
