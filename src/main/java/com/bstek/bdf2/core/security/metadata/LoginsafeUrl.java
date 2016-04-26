package com.bstek.bdf2.core.security.metadata;

/**
 *  *设置那些登录后无需授权即可访问的URL，如主框架页面，URL在设置的时候可以采用通配符模式,<br>
 * @author rd-yue.yanjie
 *
 */
public class LoginsafeUrl {
	private String urlPattern;

	public String getUrlPattern() {
		return urlPattern;
	}

	public void setUrlPattern(String urlPattern) {
		this.urlPattern = urlPattern;
	}
}
