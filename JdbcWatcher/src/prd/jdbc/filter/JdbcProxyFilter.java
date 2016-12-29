package prd.jdbc.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Endpoint;

import prd.jdbc.webservice.JdbcProxyWebService;

public class JdbcProxyFilter implements Filter{
	private String port = "9001";
	private String configPort;
	private static final String DOMAIN = "http://localhost:";
	private static final String CONTEXT = "/jdbcproxy";
	private Endpoint point;
	
	public void destroy() {
		if(point != null) {
			point.stop();
		}
	}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
		HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
		
		if(CONTEXT.equals(httpRequest.getServletPath())) {
			BufferedReader reader = null;
			try{
				String buff = new String();
				InputStream is = JdbcProxyFilter.class.getResourceAsStream("jdbcchart.html");
				reader = new BufferedReader(new InputStreamReader(is));
				
				httpResponse.setContentType("text/html");
				httpResponse.setCharacterEncoding("utf-8");
				PrintWriter writer = httpResponse.getWriter();
				while( (buff = reader.readLine()) != null) {
					writer.println(updateContent(buff));
				}
				
				return;
			} finally {
				if(reader != null) {
					reader.close();
				}
			}
		} else {
			filterChain.doFilter(httpRequest, httpResponse);
		}
		
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		this.configPort = filterConfig.getInitParameter("port");
		if(this.configPort != null && !"".equals(this.configPort)) {
			this.port = this.configPort;
		}
		point = Endpoint.publish(DOMAIN + this.port + CONTEXT, new JdbcProxyWebService());
	}
	
	private String updateContent(String s) {
		if(this.configPort == null || "".equals(this.configPort)) {
			return s;
		}
		
		int start = s.indexOf(DOMAIN);
		if(start == -1) {
			return s;
		}
		
		int end = s.indexOf(CONTEXT);
		if(end == -1) {
			return s;
		}
		
		return s.substring(0,start+DOMAIN.length()) + this.configPort + s.substring(end);

	}

}
