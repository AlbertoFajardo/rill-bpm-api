/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rill.bpm.ws.activiti;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.datatype.Duration;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.DurationHelper;
import org.activiti.engine.impl.util.ReflectUtil;
import org.apache.commons.lang.StringUtils;
import org.rill.bpm.api.activiti.RillProcessEngineConfiguration;
import org.rill.bpm.ws.metro.WSImportToolImporterImpl;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.ReflectionUtils;


/**
 * WS extension process engine configuration.
 * 
 * @author mengran
 */
public class WSProcessEngineConfiguration extends
		RillProcessEngineConfiguration {

	private AtomicReference<BpmnParser> bpmnParserRef = new AtomicReference<BpmnParser>();
	private WSImportToolImporterImpl wsXmlImporter;
	
	private String holidayWSDL;
	private String operationQName;

	public final String getHolidayWSDL() {
		return holidayWSDL;
	}

	public final void setHolidayWSDL(String holidayWSDL) {
		this.holidayWSDL = holidayWSDL;
	}

	public final String getOperationQName() {
		return operationQName;
	}

	public final void setOperationQName(String operationQName) {
		this.operationQName = operationQName;
	}

	WSImportToolImporterImpl getWsXmlImporter() {
		return wsXmlImporter;
	}

	@Required
	public void setWsXmlImporter(WSImportToolImporterImpl wsXmlImporter) {
		this.wsXmlImporter = wsXmlImporter;
	}

	public BpmnParser getBpmnParser() {
		
		bpmnParserRef.compareAndSet(null, new BpmnParser(
				expressionManager));
		
		return bpmnParserRef.get();
	}

	@Override
	public ProcessEngine buildProcessEngine() {
		
		// FIXME: MENGRAN. Work-day???
		// Set duration day WS delegation BusinessCalendar
//		BusinessCalendar originalDurationBC = this.getBusinessCalendarManager().getBusinessCalendar(DurationBusinessCalendar.NAME);
//		((MapBusinessCalendarManager) this.getBusinessCalendarManager()).addBusinessCalendar(DurationBusinessCalendar.NAME, new DurationDayWSDelegationWrapper(originalDurationBC));
		
		return super.buildProcessEngine();
	}
	
	public class DurationDayWSDelegationWrapper implements BusinessCalendar {

		private BusinessCalendar originalCalendar;
		
		public DurationDayWSDelegationWrapper(
				BusinessCalendar originalCalendar) {
			super();
			this.originalCalendar = originalCalendar;
		}
		
		protected boolean needDelegate(Duration duration) {
		
			if (StringUtils.isEmpty(getHolidayWSDL()) || StringUtils.isEmpty(getOperationQName())) {
				return false;
			}
			
			return true;
		}

		@Override
		public Date resolveDuedate(String duedateDescription) {
			
			DurationHelper dh;
			Date forReturn = null;
			try {
				dh = new DurationHelper(duedateDescription);
				Field field = ReflectUtil.getField("period", dh);
				field.setAccessible(true);
				Duration duration = (Duration) ReflectionUtils.getField(field, dh);
				// Dispatch operation
				if (needDelegate(duration)) {
					forReturn = new Date();
					// NEED DEV.
				} else {
					throw new RuntimeException();
				}
			} catch (Exception e) {
				// Fall-back original
				forReturn = this.originalCalendar.resolveDuedate(duedateDescription);
			}
			
			return forReturn;
		}
		
	}

}
