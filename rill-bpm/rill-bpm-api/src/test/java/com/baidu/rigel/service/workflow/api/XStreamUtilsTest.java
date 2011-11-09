package com.baidu.rigel.service.workflow.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
	
	@Test
	public void testPrimitiveOrWrapperSerialize() {
		
		Integer integer = new Integer(12);
		String xml = XStreamSerializeHelper.serializeXml("serializeInteger", integer);
		Assert.assertEquals("<int>12</int>", xml);
		Assert.assertEquals(integer, XStreamSerializeHelper.deserializeObject(xml, "serializeInteger", integer.getClass()));
		
		
		Long longlong = new Long(12);
		xml = XStreamSerializeHelper.serializeXml("longlong", longlong);
		Assert.assertEquals("<long>12</long>", xml);
		
		String string = "12";
		xml = XStreamSerializeHelper.serializeXml("str", string);
		Assert.assertEquals("<str>12</str>", xml);
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
	
	@Test
	public void testIsXStreamSerialied() {
		
		String extendAttrsXml = "<extendAttrs>\n  <entry>\n    <string>auditAction</string>\n    <string>1</string>\n  </entry>\n  <entry>\n    <string>auditorName</string>\n    <string>mengran</string>\n  </entry>\n</extendAttrs>";
		Assert.assertTrue(XStreamSerializeHelper.isXStreamSerialized(extendAttrsXml));
		
		String simpleType = "abc";
		Assert.assertTrue(!XStreamSerializeHelper.isXStreamSerialized(simpleType));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testListStringSerialized() {
		
		List<String> list = new ArrayList<String>(2);
		list.add("1234");
		list.add("abcd");
		
		String xml = XStreamSerializeHelper.serializeXml(XStreamUtilsTest.class.getName() + ".testListStringSerialized", list);
		Assert.assertEquals("<list>\n  <string>1234</string>\n  <string>abcd</string>\n"
				+ "</list>", xml);
		
		List<String> deserializedList = XStreamSerializeHelper.deserializeObject(xml, XStreamUtilsTest.class.getName() + ".testListStringSerialized", List.class);
		Assert.assertEquals(list, deserializedList);
	}
	
}
