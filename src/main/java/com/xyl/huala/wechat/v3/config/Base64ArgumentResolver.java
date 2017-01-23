package com.xyl.huala.wechat.v3.config;  
  
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xyl.huala.core.annotation.Base64;
import com.xyl.huala.wechat.v3.domain.Order;
  

  
public class Base64ArgumentResolver implements HandlerMethodArgumentResolver {  
  
    public boolean supportsParameter(MethodParameter parameter) {  
        //仅作用于添加了注解ListAttribute的参数  
        return parameter.getParameterAnnotation(Base64.class) != null;  
    }  
  
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Object resolveArgument(MethodParameter parameter,  
            ModelAndViewContainer mavContainer, NativeWebRequest webRequest,  
            WebDataBinderFactory binderFactory) throws Exception {  
    	Class c=parameter.getParameterType();
    	HttpServletRequest s=(HttpServletRequest) webRequest.getNativeRequest();
    	
    	String body = null;  
        StringBuilder stringBuilder = new StringBuilder();  
        BufferedReader bufferedReader = null;  
        try {  
            InputStream inputStream = s.getInputStream();  
            if (inputStream != null) {  
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));  
                char[] charBuffer = new char[128];  
                int bytesRead = -1;  
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {  
                    stringBuilder.append(charBuffer, 0, bytesRead);  
                }  
            } else {  
                stringBuilder.append("");  
            }  
        } catch (IOException ex) {  
            throw ex;  
        } finally {  
            if (bufferedReader != null) {  
                try {  
                    bufferedReader.close();  
                } catch (IOException ex) {  
                    throw ex;  
                }  
            }  
        }  
        body = stringBuilder.toString();  
        body =new String(Base64Utils.decodeFromString(body),Charset.defaultCharset());
        Object cc=JSONObject.parseObject(body,c);
        return cc;  
    }  
  
}  