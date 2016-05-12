package util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpUtil {

	private static HttpClient httpClient;
	// 默认编码
	private static String chaset = "UTF-8";
	// 百度爬虫代理
	public static String baiduSpider = "Baiduspider+(+http://www.baidu.com/search/spider.htm)";
	
	/**
	 * 初始化HttpClient实例
	 */
	static {
		httpClient = HttpClients.createDefault();
	}
	
	/**
	 * 获取HttpClient实例
	 */
	public static HttpClient getHttpClient() {
		return httpClient;
	}

	/**
	 * get请求(不带参数)
	 * 
	 * @param url 请求url
	 * @return html 页面数据
	 */
	public static String get(String url) {
		HttpGet httpGet = new HttpGet(url);
		String html = null;
		try {
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			html = EntityUtils.toString(entity, chaset);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpGet.releaseConnection();
		}
		return html;
	}
	
	/**
	 * get请求(不带参数)
	 * 
	 * @param url 请求url
	 * @param headers 请求头
	 * @return html 页面数据
	 */
	public static String get(String url, Map<String, String> headers) {
		HttpGet httpGet = new HttpGet(url);
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			httpGet.setHeader(entry.getKey(), entry.getValue());
		}
		String html = null;
		try {
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			html = EntityUtils.toString(entity, chaset);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpGet.releaseConnection();
		}
		return html;
	}

	/**
	 * get请求(带参数)
	 * 
	 * @param url 请求url
	 * @param params get请求参数
	 * @return html 页面数据
	 */
	public static String getWithParams(String url, Map<String, String> paramsMap) {
		url = url + "?" + parseParams(paramsMap);
		HttpGet httpGet = new HttpGet(url);
		String html = null;
		try {
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			html = EntityUtils.toString(entity, chaset);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpGet.releaseConnection();
		}
		return html;
	}

	/**
	 * post请求
	 * 
	 * @param url 请求url
	 * @param params get请求参数
	 * @return html 页面数据
	 */
	public static String post(String url, Map<String, String> paramsMap){
		HttpPost httpPost = new HttpPost(url);
		String html = null;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
			params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			html = EntityUtils.toString(entity, chaset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpPost.releaseConnection();
		}
		return html;
	}
	
	/**
	 * 设置编码
	 * 
	 * @param chaset
	 * @return
	 */
	public static void setCharset(String chaset){
		HttpUtil.chaset = chaset;
	}
	
	/**
	 * 转换参数列表用于get请求
	 * 
	 * @param paramsMap 
	 * @return 
	 */
	private static String parseParams(Map<String, String> paramsMap) {
		String params = "";
		for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
			params += entry.getKey() + "=" + entry.getValue() + "&";
		}
		return params.substring(0, params.length() - 1);
	}

}
