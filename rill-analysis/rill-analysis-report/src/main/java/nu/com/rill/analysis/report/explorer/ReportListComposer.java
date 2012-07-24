package nu.com.rill.analysis.report.explorer;

import java.util.ArrayList;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zss.app.file.SpreadSheetMetaInfo;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

public class ReportListComposer extends GenericForwardComposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Grid reportGrid;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		reportGrid.setModel(new ListModelArray(new ArrayList<SpreadSheetMetaInfo>(SpreadSheetMetaInfo.getMetaInfos().values())));
		reportGrid.setRowRenderer(new RowRenderer() {
			
			int index = 0;
			@Override
			public void render(Row row, Object data) throws Exception {
				SpreadSheetMetaInfo ssmi = (SpreadSheetMetaInfo) data;
				row.appendChild(new Label(new Integer(++index).toString()));
				row.appendChild(new Label(ssmi.getFileName()));
				row.appendChild(new Label(ssmi.getFormatedImportDateString()));
				Div div = new Div();
				Button open = new Button("打开");
				open.setWidgetAttribute("fileName", ssmi.getFileName());
				open.addEventListener(Events.ON_CLICK, new EventListener() {
					
					@Override
					public void onEvent(Event event) throws Exception {
						Executions.getCurrent().sendRedirect("view.zul?fileName=" + event.getTarget().getWidgetAttribute("fileName"));
					}
				});
				div.appendChild(open);
				row.appendChild(div);
			}
		});
	}

}
