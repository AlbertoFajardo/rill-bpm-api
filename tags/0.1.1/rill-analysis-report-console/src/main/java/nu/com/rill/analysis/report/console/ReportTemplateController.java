package nu.com.rill.analysis.report.console;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;

import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility;

@RequestMapping("/reporttemplates")
@Controller
@RooWebScaffold(path = "reporttemplates", formBackingObject = ReportTemplate.class)
public class ReportTemplateController {
	
	@InitBinder
	protected void initBinder(HttpServletRequest request, 
            ServletRequestDataBinder binder) {
		
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
	}
	
	@RequestMapping(value = "/download/{id}", method = RequestMethod.GET, produces = "text/html")
    public void download(HttpServletRequest request, HttpServletResponse response, 
    		@PathVariable("id") Long id, Model uiModel) {
        ReportTemplate reportTemplate = ReportTemplate.findReportTemplate(id);
        
        String result = reportTemplate.getName();
        try {
			if (request.getHeader("User-Agent").indexOf("MSIE") != -1) {
				// IE
				result = URLEncoder.encode(result, "UTF-8");
			} else {
				// NON-IE
				result = MimeUtility.encodeText(result, "GBK", "B");
			}
			
			response.addHeader("Content-Disposition", "attachment;filename=" + result);
            response.addHeader("Content-Length", "" + reportTemplate.getContents().length);
			
			IOUtils.copy(new ByteArrayInputStream(reportTemplate.getContents()), response.getOutputStream());
			response.flushBuffer();
        } catch (Exception e) {
        	// Ignore
        }
        
        return;
    }
}
