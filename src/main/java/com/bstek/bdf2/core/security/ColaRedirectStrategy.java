package com.bstek.bdf2.core.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.util.UrlUtils;

import com.bstek.bdf2.core.context.ContextHolder;
import com.bstek.dorado.core.Configure;

/**
 * @author rd-yue.yanjie
 * @since  2016年4月27日
 */
public class ColaRedirectStrategy extends DefaultRedirectStrategy {

    protected final Log logger = LogFactory.getLog(getClass());

    private boolean contextRelative;

    /**
     * Redirects the response to the supplied URL.
     * <p>
     * If <tt>contextRelative</tt> is set, the redirect value will be the value after the request context path. Note
     * that this will result in the loss of protocol information (HTTP or HTTPS), so will cause problems if a
     * redirect is being performed to change to HTTPS, for example.
     */
    public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        String redirectUrl = calculateRedirectUrl(request.getContextPath(), url);
        redirectUrl = response.encodeRedirectURL(redirectUrl);

        if (logger.isDebugEnabled()) {
            logger.debug("Redirecting to '" + redirectUrl + "'");
        }
        String uri=request.getRequestURI();
        String loginProcessUrl=Configure.getString("bdf2.loginProcessUrl");
        String loginSuccessUrl=Configure.getString("bdf2.loginSuccessDefaultTargetUrl");
        String loginUrl=Configure.getString("bdf2.formLoginUrl");
        //处理ajax请求的响应结果
        if(loginProcessUrl.equals(uri)&&redirectUrl.equals(loginSuccessUrl)){
        	String cname=ContextHolder.getLoginUser().getCname();
        	String cpId=ContextHolder.getLoginUser().getCompanyId();
        	String cpName=ContextHolder.getLoginUser().getPassword();
        	response.setContentType("text/json; charset=UTF-8");
        	response.getWriter().write("{\"login\":\"success\",\"url\":\""+redirectUrl+"\",\"cname\":\""+cname+"\",\"cpId\":\""+cpId+"\",\"cpName\":\""+cpName+"\"}");
        }else  if(loginProcessUrl.equals(uri)&&redirectUrl.equals(loginUrl)){
        	response.setContentType("text/json; charset=UTF-8");
        	response.getWriter().write("{\"login\":\"failure\",\"msg\":\"BadUsernameorpassword\",\"url\":\""+redirectUrl+"\"}");
        } else{
        	String sessionExpired=Configure.getString("bdf2.sessionExpiredUrl");
        	String sessionKicked=Configure.getString("bdf2.sessionKickAwayUrl");
        	//String sessionExpired="/frame1/SessionExpired";
        	//String sessionKicked="/frame1/SessionKicked";
        	String referer=request.getHeader("Referer");
        	String XMLHttpRequest=request.getHeader("X-Requested-With"); 
        	if(referer==null){
        		if(redirectUrl.equals(sessionKicked)||redirectUrl.equals(sessionExpired)){
        			response.sendRedirect(loginUrl);
        		}else{
        			//直接输入网址
        			response.sendRedirect(redirectUrl);
        		}
        	}else if(redirectUrl.endsWith(sessionExpired)||redirectUrl.endsWith(sessionKicked)){
            	if("XMLHttpRequest".equals(XMLHttpRequest)){
            		response.setStatus(401);
            		response.getWriter().write("Unauthorized");
            	}else{
        			response.sendRedirect(loginUrl);
            	}
        	}else if(redirectUrl.endsWith(loginUrl)&&!loginSuccessUrl.equals(uri)){
        		if(referer.endsWith(sessionKicked)){
            		response.sendRedirect(redirectUrl);
        		}else{
        			if("XMLHttpRequest".equals(XMLHttpRequest)){
                		response.setStatus(401);
                		response.getWriter().write("Unauthorized");
                	}else{
            			response.sendRedirect(loginUrl);
                	}
        		}
        	}else{
        		response.sendRedirect(redirectUrl);
        	}
        }
    }

    private String calculateRedirectUrl(String contextPath, String url) {
        if (!UrlUtils.isAbsoluteUrl(url)) {
            if (contextRelative) {
                return url;
            } else {
                return contextPath + url;
            }
        }

        // Full URL, including http(s)://

        if (!contextRelative) {
            return url;
        }

        // Calculate the relative URL from the fully qualified URL, minus the scheme and base context.
        url = url.substring(url.indexOf("://") + 3); // strip off scheme
        url = url.substring(url.indexOf(contextPath) + contextPath.length());

        if (url.length() > 1 && url.charAt(0) == '/') {
            url = url.substring(1);
        }

        return url;
    }
}