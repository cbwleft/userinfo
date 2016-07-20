package com.zte.userinfo.jd;

import com.github.kevinsawicki.http.HttpRequest;
import com.zte.userinfo.core.exceptions.LoginException;

import static com.zte.userinfo.jd.Config.useFiddlerProxy;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Order {

	private final static String OrderPage="http://order.jd.com/center/list.action?search=0&d=2015&s=4096";//订单页面
	
	private final static String OrderUrl="http://order.jd.com/lazy/getOrderProductInfo.action";//真实的订单数据请求
	
	private final static String JESSIONID="thor";//京东的会话ID
	
	public String getUserOrder(String cookie){
		HttpRequest getOrderPageReq=HttpRequest.get(OrderPage);
		if(useFiddlerProxy){
			getOrderPageReq.useProxy("localhost", 8888).trustAllCerts().trustAllHosts();
		}
		getOrderPageReq.header("Cookie", cookie);
		if(!getOrderPageReq.ok()){
			throw new LoginException("登录失效：请求状态码"+getOrderPageReq.code());
		}
		String body=getOrderPageReq.body();//请求订单页面
		System.out.println(body);
		Document doc=Jsoup.parse(body);
		Map<String,String> data=getOrderParams(doc);
		HttpRequest postOrderUrlReq=HttpRequest.post(OrderUrl);
		if(useFiddlerProxy){
			postOrderUrlReq.useProxy("localhost", 8888).trustAllCerts().trustAllHosts();
		}
		postOrderUrlReq.header("Cookie", cookie).form(data);
		String body2=postOrderUrlReq.body();
		return body2;
	}
	
	/**
	 * 订单数据的请求参数需要从该页面中的JS代码中获取：
	 * var orderWareIds = $ORDER_CONFIG.orderWareIds;
	 * var orderWareTypes = $ORDER_CONFIG.orderWareTypes;
	 * var orderIds = $ORDER_CONFIG.orderIds;
	 * var orderTypes = $ORDER_CONFIG.orderTypes;
	 * var orderSiteIds = $ORDER_CONFIG.orderSiteIds;
	 * 代码出处：http://misc.360buyimg.com/user/myjd-2015/js/page/order/list-service.js 格式化后
	 */
	private Map<String,String> getOrderParams(Document doc){
		
		String[] keys={"orderWareIds","orderWareTypes","orderIds","orderTypes","orderSiteIds"};
		Map<String,String> data=new HashMap<>();
		Elements scripts=doc.select("script");
		for(Element script:scripts){
			String text=script.html();
			if(text.contains("$ORDER_CONFIG")){//先定位到是哪一个script脚本
				System.out.println(text);
				String[] lines=text.split("\n");
				for(String line:lines){
					for(String key:keys){
						if(line.contains(key)){
							String value=line.split("=")[1];
							value=value.substring(value.indexOf("'")+1, value.lastIndexOf("'"));
							data.put(key, value);
						}
					}
				}
			}
		}
		System.out.println(data);
		return data;
	}
	
}
