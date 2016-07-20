package com.zte.userinfo.jd;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.github.kevinsawicki.http.HttpRequest;
import com.zte.userinfo.core.exceptions.LoginException;
import com.zte.userinfo.core.utils.HtmlUtils;
import com.zte.userinfo.core.utils.StringUtils;

public class Login {
	
	private boolean test=false;//测试用代理，使用fiddler抓包解密Https请求
	
	private static final String LoginPage="https://passport.jd.com/new/login.aspx";
	
	private static final String LoginUrl="https://passport.jd.com/uc/loginService";
	
	private static final String LoginSuccess="({\"success\":\"http://www.jd.com\"})";
	
	/**
	 * 登录账户和密码，返回登录成功后的cookie，如果登录失败则抛出登录异常
	 */
	public List<String> login(String loginname,String loginpwd){
		HttpRequest getLoginPageReq=HttpRequest.get(LoginPage);//请求登录页面
		if(test){
			getLoginPageReq.useProxy("localhost", 8888).trustAllCerts().trustAllHosts();
		}
		String body=getLoginPageReq.body();
		Map<String, List<String>> responseHeaders=getLoginPageReq.headers();
		List<String> cookies=responseHeaders.get("Set-Cookie");//获取JSEESIONID
		System.out.println(body);
		Document doc=Jsoup.parse(body);//解析Html页面
		Elements form=doc.select("form#formlogin");//获取登录表单并且填写账号密码
		form.select("#loginname").val(loginname);
		form.select("#nloginpwd").val(loginpwd);
		form.select("#loginpwd").val(loginpwd);
		Map<String,String> formData=HtmlUtils.formData(form);//构造登录表单数据
		formData.put("eid", "DE2C01C22FBCF776DCE2EAD33B9950BE720FFDD22945D78399F84BD18EC4CFECC030F455A2EB7267716043858060147F");
		formData.put("fp","57c2bd9af0067836ac64a23e2add8304");//js构造的表单参数
		String uuid=form.select("#uuid").get(0).val();
		String url=LoginUrl+"?uuid="+uuid+"&r="+Math.random()+"&version=2015";//登录请求地址
		try{
			Thread.sleep(2000);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		HttpRequest postLoginUrlReq=HttpRequest.post(url);
		if(test){
			postLoginUrlReq.useProxy("localhost", 8888).trustAllCerts().trustAllHosts();
		}
		String cookie=HtmlUtils.toCookieValue(cookies);
		Map<String,String> headers=new HashMap<>();
		headers.put("Cookie", cookie);
		//headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.110 Safari/537.36");
		//headers.put("X-Requested-With", "XMLHttpRequest");
		//headers.put("Referer", LoginPage);
		//headers.put("Accept", "text/plain, */*; q=0.01");
		//headers.put("Accept-Encoding", "gzip, deflate");
		//headers.put("Accept-Language", "zh-CN,zh;q=0.8");
		postLoginUrlReq.headers(headers).form(formData);
		String body2=postLoginUrlReq.body();
		System.out.println(StringUtils.decodeUnicode(body2));
		if(!LoginSuccess.equals(body2)){
			throw new LoginException("登录失败，登录返回数据为："+body2);
		}
		Map<String, List<String>> responseHeaders2=postLoginUrlReq.headers();
		List<String> cookies2=responseHeaders2.get("Set-Cookie");
		return cookies2;
	}
	
	
	
	public static void main(String[] args) {
		Login login=new Login();
		//login.test=true;
		List<String> cookies=login.login("这里输你的京东账号", "这里输密码");
		Order order=new Order();
		System.out.println(order.getUserOrder(HtmlUtils.toCookieValue(cookies)));
		
	}
}
