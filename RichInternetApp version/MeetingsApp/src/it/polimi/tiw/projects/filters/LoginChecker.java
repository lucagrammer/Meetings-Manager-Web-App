package it.polimi.tiw.projects.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginChecker implements Filter {

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException 

	{
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String loginpath = req.getServletContext().getContextPath() + "/index.html";
		
		res.setHeader("Cache-Control","no-cache"); //Forces caches to obtain a new copy of the page from the origin server
		res.setHeader("Cache-Control","no-store"); //Directs caches not to store the page under any circumstance
		res.setDateHeader("Expires", 0);
		res.setHeader("Pragma","no-cache"); //HTTP 1.0 backward compatibility

		HttpSession session = req.getSession();
		//System.out.println("Checking "+req.getRequestURI());
		if (session.isNew() || session.getAttribute("user") == null) {
			res.sendRedirect(loginpath);
			return;
		}
		
		// execute the next filter of the filter chain
		chain.doFilter(request, response);
	}
}


