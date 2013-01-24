package org.rill.bpm.web;

import java.io.StringWriter;

import org.activiti.engine.impl.util.json.JSONWriter;
import org.junit.Test;

public class JSONArrayTest {

	@Test
	public void jsonArray() {
		
		String[][] processDefArray = new String[3][];
		for (int i = 0; i < 3; i++) {
			String[] peerProcessDef = new String[2];
			peerProcessDef[0] = "Key" + i;
			peerProcessDef[1] = "Name" + i;
			processDefArray[i] = peerProcessDef;
		}
		
		StringWriter sw = new StringWriter();
		JSONWriter jsonWriter = new JSONWriter(sw);
		jsonWriter
			.object()
			.key("total")
			.value(1)
			.key("page")
			.value(1)
			.key("records")
			.value(20)
			.key("invdata")
			.value(processDefArray)
			.endObject();
		
		System.out.println(sw.toString());
	}
}
