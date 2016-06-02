package com.bstek.bdf2.core.security;


import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import com.bstek.bdf2.core.exception.NoneLoginException;
import com.bstek.bdf2.core.security.session.SessionStateConstants;

/**
 * @author Jacky.gao
 * @since 2013年12月19日
 */
public class FormLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {
    private static final Log logger = LogFactory.getLog(LoginUrlAuthenticationEntryPoint.class);
	private String sessionKickAwayUrl;
	private String expiredUrl;
	private boolean forceHttps = false;
	private boolean useForward = false;
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
	public FormLoginUrlAuthenticationEntryPoint(String loginFormUrl,String sessionKickAwayUrl,String expiredUrl) {
		super(loginFormUrl);
		this.sessionKickAwayUrl=sessionKickAwayUrl;
		this.expiredUrl=expiredUrl;
	}
	/**
     * Performs the redirect (or forward) to the login form URL.
     */
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        String redirectUrl = null;

        if (useForward) {

            if (forceHttps && "http".equals(request.getScheme())) {
                // First redirect the current request to HTTPS.
                // When that request is received, the forward to the login page will be used.
                redirectUrl = buildHttpsRedirectUrlForRequest(request);
            }

            if (redirectUrl == null) {
                String loginForm = determineUrlToUseForThisRequest(request, response, authException);

                if (logger.isDebugEnabled()) {
                    logger.debug("Server side forward to: " + loginForm);
                }

                RequestDispatcher dispatcher = request.getRequestDispatcher(loginForm);

                dispatcher.forward(request, response);

                return;
            }
        } else {
            // redirect to login page. Use https if forceHttps true

            redirectUrl = buildRedirectUrlToLoginPage(request, response, authException);

        }

        redirectStrategy.sendRedirect(request, response, redirectUrl);
    }
	@Override
    protected String buildRedirectUrlToLoginPage(HttpServletRequest request, HttpServletResponse response,AuthenticationException authException) {
    	if(authException!=null){
    		Throwable ex=(Throwable)getCause(authException);
    		if(ex instanceof NoneLoginException){
    			NoneLoginException exception=(NoneLoginException)ex;
    			if(exception.isSessionKickAway()){
    				return sessionKickAwayUrl;
    			}
    		}
    	}
    	HttpSession session=request.getSession();
    	String state=(String)session.getAttribute(SessionStateConstants.SESSION_STATE);
    	if(state!=null){
    		//session.removeAttribute(SessionStateConstants.SESSION_STATE);
    		if(state.equals(SessionStateConstants.KICKAWAY)){
    			return sessionKickAwayUrl;
    		}else if(state.equals(SessionStateConstants.EXPIRED)){
    			return expiredUrl;
    		}
    	}
    	return super.buildRedirectUrlToLoginPage(request, response, authException);
    }
    
    private Throwable getCause(Throwable ex){
    	if(ex.getCause()!=null){
    		return getCause(ex.getCause());
    	}else{
    		return ex;
    	}
    }
	public RedirectStrategy getRedirectStrategy() {
		return redirectStrategy;
	}
	public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
		this.redirectStrategy = redirectStrategy;
	}
}
