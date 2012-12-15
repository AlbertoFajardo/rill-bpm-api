package nu.com.rill.analysis.report.excel.export;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import nu.com.rill.analysis.report.REException;

import org.springframework.util.StringUtils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ViaEmailReportExportHttpInvokerHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		
		URI requestUri = exchange.getRequestURI();
		String query = requestUri.getRawQuery();
		
		try {
			List<String> queryParameters = parseQueryParameter(query);
			
			ViaEmailReportExportMain.export(queryParameters.toArray(new String[0]));
			
			exchange.getResponseHeaders().set("Content-Type", "text/html;charset=UTF-8");
			exchange.sendResponseHeaders(200, 0);
			exchange.getResponseBody().write("OK".getBytes("UTF8"));
			exchange.getResponseBody().flush();
			exchange.close();
		} catch (Throwable e) {
			e.printStackTrace();
			exchange.getResponseHeaders().set("Content-Type", "text/html;charset=UTF-8");
			exchange.sendResponseHeaders(500, -1);
		}
	}
	
	private List<String> parseQueryParameter(String query) {
		
		List<String> queryParameters = new ArrayList<String>();
		if (!StringUtils.hasText(query)) {
			return queryParameters;
		}
		
		String[] pairs = query.split("[&]");
		if (pairs == null || pairs.length < 1) {
			return queryParameters;
		}
		
		for (String pair : pairs) {
			if (!StringUtils.hasText(pair)) {
				continue;
			}
			try {
				String[] param = URLDecoder.decode(pair, "UTF-8").split("[=]");
				queryParameters.add(StringUtils.replace(param[1], " ", "="));
			} catch (Exception e) {
				throw new REException(e);
			}
		}
		
		return queryParameters;
		
	}
	
}
