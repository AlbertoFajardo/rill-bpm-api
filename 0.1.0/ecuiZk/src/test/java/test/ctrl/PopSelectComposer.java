package test.ctrl;

import java.util.ArrayList;
import java.util.HashMap;

import com.foo.ecuiZk.PopSelect;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;

public class PopSelectComposer extends GenericForwardComposer {
	
	private PopSelect myComp;
	
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		HashMap<String, String> options = new HashMap<String, String>();
		ArrayList<ArrayList<String>> items = new ArrayList<ArrayList<String>>();
		ArrayList<String> item = new ArrayList<String>();
		options.put("butWidth", "400px");
		options.put("panelWidth", "350px");
		item.add("0");
		item.add("hello world");
		item.add("true");
		item.add("false");
		items.add(item);
		ArrayList<String> item1 = new ArrayList<String>();
		item1.add("1");
		item1.add("hello baidu");
		item1.add("true");
		item1.add("false");
		items.add(item1);
		myComp.setText("Hello ZK Component!! Please click me你妹你妹你妹.");
		myComp.setOptions(options);
		myComp.setItems(items);
		
	}
	
	public void onChange$myComp (ForwardEvent event) {
		Event mouseEvent = (Event) event.getOrigin();
	}
}