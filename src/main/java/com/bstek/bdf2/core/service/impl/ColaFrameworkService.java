package com.bstek.bdf2.core.service.impl;

import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.bstek.bdf2.core.business.IUser;
import com.bstek.bdf2.core.context.ContextHolder;
import com.bstek.bdf2.core.service.IFrameworkService;

/**客户端采用Cola之类Ajax产品时实现该接口
 * @author rd-yue.yanjie
 * @since  2016年6月3日
 */
public abstract class ColaFrameworkService implements IFrameworkService{
	public void authenticate(IUser user, UsernamePasswordAuthenticationToken authentication){
		Map<String,Object> map=authenticate1(user,authentication);
		if(map!=null){
			if(map.containsKey("toClientMap")){
				ContextHolder.getHttpSession().setAttribute(com.bstek.bdf2.core.security.ColaRedirectStrategy.LOGIN_USER_SESSION_KEY_TOCLIENTMAP,map.get("toClientMap"));
			}
			if(map.containsKey("sessionUser")){
				Object iuser=map.get("sessionUser");
				if(iuser instanceof IUser){
					ContextHolder.getHttpSession().setAttribute(ContextHolder.LOGIN_USER_SESSION_KEY,iuser);
				}
			}
		}
	}
	/**
	 * toClientMap返回客户端的JSON数据
	 * sessionUser需实现IUser接口，可以通过com.bstek.bdf2.core.context.ContextHolder.getLoginUser()获取到
	 * @param user
	 * @param authentication
	 * @return Map<String,Object>
	 * key:toClientMap返回客户端的JSON数据
	 * key:sessionUser需实现IUser接口，可以通过com.bstek.bdf2.core.context.ContextHolder.getLoginUser()获取到
	 */
	public abstract Map<String,Object> authenticate1(IUser user, UsernamePasswordAuthenticationToken authentication);
}
