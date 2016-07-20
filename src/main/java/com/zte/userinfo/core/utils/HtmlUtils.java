package com.zte.userinfo.core.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection.KeyVal;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

public class HtmlUtils {
	
	public static Map<String,String> formData(Elements form){
		FormElement form0=form.forms().get(0);
		List<KeyVal> list=form0.formData();
		Map<String,String> map=new HashMap<>();
		for(KeyVal keyVal:list){
			String key=keyVal.key();
			String value=keyVal.value();
			map.put(key, value);
		}
		return map;
	}
	
	
	public static String toCookieValue(List<String> cookies){
		StringBuilder sb=new StringBuilder();
		for(String cookie:cookies){
			//alc=9OSax7+Qz4owUOqZwKaXAA==; Path=/; HttpOnly;
			if(sb.length()>0){
				sb.append("; ");
			}
			sb.append(cookie.split(";")[0]);
		}
		return sb.toString();
	}
	
}

