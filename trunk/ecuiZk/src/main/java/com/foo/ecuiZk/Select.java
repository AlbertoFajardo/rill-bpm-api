package com.foo.ecuiZk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.zkoss.lang.Objects;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.impl.XulElement;

public class Select extends XulElement {

	/*
	static {
		addClientEvent(Select.class, "onFoo", CE_IMPORTANT);
	}
	*/
	
	/* Here's a simple example for how to implements a member field */

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String _text;
	
	private String _value;
	
	private HashMap<String, String> _options;
	
	private ArrayList<HashMap<String, String>> _items;

	public String getText() {
		return _text;
	}
	
	public String getValue() {
		return _value;
	}
	
	public HashMap<String, String> getOptions() {
		return _options;
	}
	
	public ArrayList<HashMap<String, String>> getItems() {
		return _items;
	}
	
	public void setText(String text) {
		if (!Objects.equals(_text, text)) {
			_text = text;
//			smartUpdate("text", _text);
		}
	}
	
	public void setValue(String value) {
		if (!Objects.equals(_value, value)) {
			_value = value;
//			smartUpdate("value", _value);
			
			// Add by MENGRAN for synchronize value and text
			for (HashMap<String, String> map : getItems()) {
				if (map.get("value").equals(_value)) {
					setText(map.get("text"));
					break;
				}
			}
		}
	}
	
	public void setOptions(HashMap<String, String> options) {
		_options = options;
		smartUpdate("options", _options);
	}
	
	public void setItems(ArrayList<HashMap<String, String>> items) {
		_items = items;
		smartUpdate("items", _items);
		
		// Add by MENGRAN for synchronize value and items
		for (HashMap<String, String> map : items) {
			if (map.containsKey("selected")) {
				setValue(map.get("value"));
				break;
			}
		}
	}
	
	static {
		addClientEvent(Select.class, "onChange", CE_IMPORTANT);
	}

	//super//
	protected void renderProperties(org.zkoss.zk.ui.sys.ContentRenderer renderer)
	throws java.io.IOException {
		super.renderProperties(renderer);

		render(renderer, "text", _text);
		render(renderer, "options", _options);
		render(renderer, "items", _items);
	}
	
	@SuppressWarnings("rawtypes")
	public void service(AuRequest request, boolean everError) {
		final String cmd = request.getCommand();
		final Map data = request.getData();
		
		if (cmd.equals("onFoo")) {
			final String foo = (String)data.get("foo");
			System.out.println("do onFoo, data:" + foo);
			Events.postEvent(Event.getEvent(request));
		} 
		else if (cmd.equals("onChange")) {
			final String value = (String)data.get("value");
			this.setValue(value);
			//System.out.println(_value);
			Events.postEvent(Event.getEvent(request));
		}else
			super.service(request, everError);
	}

	/**
	 * The default zclass is "z-select"
	 */
	public String getZclass() {
		return (this._zclass != null ? this._zclass : "z-select");
	}
}

