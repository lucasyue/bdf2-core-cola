package com.bstek.bdf2.core.exception.interceptor;

import java.util.Collection;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.bstek.bdf2.core.exception.IAjaxExceptionHandler;
import com.bstek.dorado.common.proxy.PatternMethodInterceptor;
import com.bstek.dorado.core.Configure;
import com.bstek.dorado.view.resolver.ClientRunnableException;

/**
 * @author Jacky.gao
 * @since 2013年11月25日
 */
public class AjaxMethodInterceptor extends PatternMethodInterceptor implements ApplicationContextAware{
	private Collection<IAjaxExceptionHandler> handlers;
	public Object invoke(MethodInvocation invocation) throws Throwable {
		try {
            return invocation.proceed();
        }catch (Exception exception) {
        	Throwable ex=getException(exception);
            for(IAjaxExceptionHandler handler:handlers){
            	if(handler.support(ex)){
            		handler.handle(ex, invocation);
            		break;
            	}
            }
            if(ex instanceof ClientRunnableException){
            	throw ex;
            }
            Throwable excep=getException(exception);
            String errorInfo=Configure.getString("bdf2.ajaxInvodeExceptionMessage");
            String error=excep.getMessage();
            if(error==null){
            	error="";
            }
			if (error.indexOf("$") > -1){
		        error = java.util.regex.Matcher.quoteReplacement(error);  
	        }
            errorInfo = errorInfo.replaceAll("#errorMessage", error);            	
            throw new Exception(errorInfo);
        }
	}
		
	private Throwable getException(Throwable ex){
		if(ex.getCause()==null){
			return ex;
		}else{
			return ex.getCause();
		}
	}
	
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		handlers=applicationContext.getBeansOfType(IAjaxExceptionHandler.class).values();
	}
}
