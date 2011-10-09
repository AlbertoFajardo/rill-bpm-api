package org.rill.bpm.ws.metro.hello.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rill.bpm.ws.metro.hello.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/hello")
public class HelloController {
	
	private final String LIST_ACTION = "redirect:/entry/hello";
	
	private HelloService helloService;
	
	public final HelloService getHelloService() {
		return helloService;
	}

	@Autowired
	public final void setHelloService(HelloService helloService) {
		this.helloService = helloService;
	}

	@RequestMapping(value={"/new"}, method=RequestMethod.GET)
	public ModelAndView _new(HttpServletRequest request, HttpServletResponse response, HelloVO command) {
		
		ModelAndView mav = new ModelAndView();
		command.setWhoSay("");
		mav.addObject("command", command);
        mav.setViewName("/hello/new");
        return mav;
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public ModelAndView sayHello(HttpServletRequest request, HttpServletResponse response, HelloVO command) {
		
		getHelloService().sayHello(command.getWhoSay());
        return new ModelAndView(LIST_ACTION);
	}
	
	@RequestMapping
	public ModelAndView list(HttpServletRequest request, HttpServletResponse response) {
		
		ModelAndView mav = new ModelAndView();
        mav.setViewName("/hello/list");
        mav.addObject("whoSaid", getHelloService().whoSaid());
        
        return mav;
	}
}
