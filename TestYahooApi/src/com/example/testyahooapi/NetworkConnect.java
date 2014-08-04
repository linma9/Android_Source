package com.example.testyahooapi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkConnect{
	public static String TAG = "NetworkConnect";
	public static boolean checkNetworkConneted(Activity activity){
		boolean result = false;
		ConnectivityManager CM = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = CM.getActiveNetworkInfo();
		if(info==null || !info.isConnected()){
			return false;
		}else{
    		Log.i(TAG, "info.getTypeName() : " + info.getTypeName());
    		if(info.getType()==ConnectivityManager.TYPE_MOBILE || info.getType()==ConnectivityManager.TYPE_WIFI || info.getType()==ConnectivityManager.TYPE_WIMAX){
	    		if (!info.isAvailable()){
	    			result = false;
	    		}else{
	    			result = true;
	    		}
    		}else{
    			result = false;
    		}
    	}
    	return result;
	}
	
	public String httpClientPostRequest(String serverUrl, List<NameValuePair> param){
		String response = "";
		try {
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			// The default value is zero, that means the timeout is not used. 
			int timeoutConnection = 15000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT) 
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 20000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			HttpProtocolParams.setVersion(httpParameters, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(httpParameters, HTTP.UTF_8);
			DefaultHttpClient defaultHttpClient = new DefaultHttpClient(httpParameters);
			//retry
			defaultHttpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(3, true));
			HttpPost request = new HttpPost(serverUrl);
			
			if(param!=null && param.size()>0){
				request.setEntity(new UrlEncodedFormEntity(param, HTTP.UTF_8));
			}
			HttpResponse httpResponse = defaultHttpClient.execute(request); 
			response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8").trim();
			Log.d(TAG, "httpClientPostRequest Respose : " + response);
		} catch (Exception e) {
			Log.e(TAG, "httpClientPostRequest Exception : ", e);
		}
		
		return response;
	}
	
	public String httpClientGetRequest(String serverUrl){
		String response = "";
		try {
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			// The default value is zero, that means the timeout is not used. 
			int timeoutConnection = 15000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT) 
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 20000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			HttpProtocolParams.setVersion(httpParameters, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(httpParameters, HTTP.UTF_8);
			DefaultHttpClient defaultHttpClient = new DefaultHttpClient(httpParameters);
			//retry
			defaultHttpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(3, true));
			HttpGet request = new HttpGet(serverUrl);
			
			HttpResponse httpResponse = defaultHttpClient.execute(request); 
			response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8").trim();
			Log.d(TAG, "httpClientGetRequest Respose : " + response);
		} catch (Exception e) {
			Log.e(TAG, "httpClientGetRequest Exception : ", e);
		}
		
		return response;
	}
	
	public String httpsPostRequest(String serverUrl){
		String response = "";
		InputStream iStream = null;
		HttpsURLConnection conn = null;
		try {
			StringBuilder sbUrl = new StringBuilder(serverUrl); 
			
			URL url = new URL(sbUrl.toString());
		    conn = (HttpsURLConnection) url.openConnection();

		    // Create the SSL connection
		    SSLContext sc;
		    sc = SSLContext.getInstance("TLS");
		    sc.init(null, null, new java.security.SecureRandom());
		    conn.setSSLSocketFactory(sc.getSocketFactory());
		    
		    // set Timeout and method
		    conn.setReadTimeout(7000);
		    conn.setConnectTimeout(7000);
		    conn.setRequestMethod("POST");
		    conn.setDoInput(true);

		    conn.connect();
		    Log.i(TAG, "getResponseCode :  " + conn.getResponseCode() + ", getResponseMessage : " + conn.getResponseMessage());
		    // Reading data from url
 			iStream = conn.getInputStream();

 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					iStream));

 			StringBuffer sb = new StringBuffer();

 			String line = "";
 			while ((line = br.readLine()) != null) {
 				sb.append(line);
 			}

 			response = sb.toString();

 			br.close();
 			Log.i(TAG, "httpsPostRequest response : " + response);
 		} catch (Exception e) {
 			Log.e(TAG, "httpsPostRequest Exception : ", e);
 		} finally {
 			if(iStream!=null){
 				try {
					iStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
 			}
 			if(conn!=null){
 				conn.disconnect();
 			}
 		}
		
		return response;
	}
	
	public String httpsGetRequest(String serverUrl){
		String response = "";
		InputStream iStream = null;
		HttpsURLConnection conn = null;
		try {
			StringBuilder sbUrl = new StringBuilder(serverUrl); 
			
			URL url = new URL(sbUrl.toString());
		    conn = (HttpsURLConnection) url.openConnection();

		    // Create the SSL connection
		    SSLContext sc;
		    sc = SSLContext.getInstance("TLS");
		    sc.init(null, null, new java.security.SecureRandom());
		    conn.setSSLSocketFactory(sc.getSocketFactory());
		    
		    // set Timeout and method
		    conn.setReadTimeout(7000);
		    conn.setConnectTimeout(7000);
		    //conn.setRequestMethod("GET");
		    conn.setDoInput(true);

		    conn.connect();
		    
		    // Reading data from url
 			iStream = conn.getInputStream();

 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					iStream));

 			StringBuffer sb = new StringBuffer();

 			String line = "";
 			while ((line = br.readLine()) != null) {
 				sb.append(line);
 			}

 			response = sb.toString();

 			br.close();
 			Log.i(TAG, "httpsGetRequest response : " + response);
 		} catch (Exception e) {
 			Log.e(TAG, "httpsGetRequest Exception : ", e);
 		} finally {
 			if(iStream!=null){
 				try {
					iStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
 			}
 			if(conn!=null){
 				conn.disconnect();
 			}
 		}
		
		return response;
	}
	
	private String setQueryParameter(List<NameValuePair> params) throws UnsupportedEncodingException
	{
	    StringBuilder result = new StringBuilder();
	    boolean first = true;

	    for (NameValuePair pair : params)
	    {
	        if (first){
	            first = false;
	        }else{
	            result.append("&");
	        }
	        result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
	        result.append("=");
	        result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
	    }

	    return result.toString();
	}
}