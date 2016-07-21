package com.zte.userinfo.core.utils;

import com.github.kevinsawicki.http.HttpRequest;

public class HttpUtils {

	public static boolean useFiddlerProxy=false;//测试用代理，使用fiddler抓包解密Https请求
	
	public static boolean useDefaultUserAgent=true;
	
	private static final String DEFAULT_USER_AGENT="Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.110 Safari/537.36";
	
	/**
	 * 全局设置，给每个请求设置代理 
	 * @param httpRequest
	 * @return
	 */
	public static HttpRequest proxy(HttpRequest httpRequest){
		if(useFiddlerProxy){
			httpRequest.useProxy("localhost", 8888).trustAllCerts().trustAllHosts();
		}
		return httpRequest;
	}
	
	/**
	 * 使用全局的设置来创建http请求，设置默认UA等
	 * @param url
	 * @param method
	 * @return
	 */
	public static HttpRequest request(String url,String method){
		HttpRequest httpRequest=new HttpRequest(url,method);
		if(useDefaultUserAgent){
			httpRequest.userAgent(DEFAULT_USER_AGENT);
		}
		proxy(httpRequest);
		return httpRequest;
	}
}
