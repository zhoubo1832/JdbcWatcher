package prd.jdbc.webservice;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;

import prd.jdbc.proxy.JdbcWrapper;

@WebService
public class JdbcProxyWebService {

	@WebMethod
	@WebResult
	public String getJdbcInfo(){
		int used = JdbcWrapper.getUSED_CONNECTION_NUM();
		int active = JdbcWrapper.getACTIVE_CONNECTION_NUM();
		
		return "{\"opened\":\"" + used + "\",\"active\":\"" + active + "\"}";
	}
}
