package com.zte.userinfo.jd;

import com.github.kevinsawicki.http.HttpRequest;
import com.zte.userinfo.core.exceptions.LoginException;
import com.zte.userinfo.core.utils.HttpUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.kevinsawicki.http.HttpRequest.METHOD_GET;
import static com.github.kevinsawicki.http.HttpRequest.METHOD_POST;


public class Order {

	private final static String OrderPage="http://order.jd.com/center/list.action?search=0&d={0,number,#}&s=4096";//订单页面
	
	private final static String OrderUrl="http://order.jd.com/lazy/getOrderProductInfo.action";//真实的订单数据请求
	
	private static Logger logger=LoggerFactory.getLogger(Order.class);
	
	public String getUserOrder(String cookie,int year){
		String url=MessageFormat.format(OrderPage, year);
		HttpRequest getOrderPageReq=HttpUtils.request(url, METHOD_GET);//请求订单页面
		getOrderPageReq.header("Cookie", cookie);
		if(!getOrderPageReq.ok()){
			throw new LoginException("登录失效：请求状态码"+getOrderPageReq.code());
		}
		String body=getOrderPageReq.body();//请求订单页面
		logger.debug("请求订单页面{}的响应结果为{}",url,body);
		Document doc=Jsoup.parse(body);
		Map<String,String> data=getOrderParams(doc);
		HttpRequest postOrderUrlReq=HttpUtils.request(OrderUrl, METHOD_POST);//懒加载订单详情
		postOrderUrlReq.header("Cookie", cookie).form(data);
		String body2=postOrderUrlReq.body();
		logger.info("请求订单数据的结果为{}",body2);
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
				logger.debug("填充订单脚本的脚本代码为{}",text);
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
		logger.info("请求订单数据的请求参数为{}",data);
		return data;
	}
	
}
