
package org.saiku.web.rest.objects.resultset;


public class MarkResult {
	
	private String result;
	private boolean ok;
	
	public MarkResult(String result, boolean ok) {
		this.result = result;
		this.ok = ok;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}
	
}
