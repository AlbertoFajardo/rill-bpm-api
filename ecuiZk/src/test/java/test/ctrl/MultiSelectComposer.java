package test.ctrl;

import java.util.ArrayList;
import java.util.HashMap;

import com.foo.ecuiZk.MultiSelect;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;

public class MultiSelectComposer extends GenericForwardComposer {
	
	private MultiSelect myComp;
	
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		HashMap<String, String> options = new HashMap<String, String>();
		ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> item = new HashMap<String, String>();
		options.put("name", "sex");
		options.put("width", "200px");
		item.put("value", "0");
		item.put("text", "male");
		items.add(item);
		HashMap<String, String> item1 = new HashMap<String, String>();
		item1.put("value", "1");
		item1.put("text", "female");
		item1.put("selected", "selected");
		items.add(item1);
		myComp.setText("Hello ZK Component!! Please click me你妹你妹你妹.");
		myComp.setOptions(options);
		myComp.setItems(items);
		
	}
	
	public void onChange$myComp (ForwardEvent event) {
		Event mouseEvent = (Event) event.getOrigin();
	}
}