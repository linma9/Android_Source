package com.example.testyahooapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import oauth.signpost.OAuth;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.signature.HmacSha1MessageSigner;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class TestMainActivity extends Activity {
	
	private static final String TAG = "TestMainActivity";
	
	private static final String REQUEST_TOKEN_ENDPOINT_URL = "https://api.login.yahoo.com/oauth/v2/get_request_token";
	private static final String ACCESS_TOKEN_ENDPOINT_URL = "https://api.login.yahoo.com/oauth/v2/get_token";
	private static final String AUTHORIZE_WEBSITE_URL = "https://api.login.yahoo.com/oauth/v2/request_auth";
	private static final int PIN_DIALOG = 0;
	String CALLBACK_URL = "oauth://ya4jsampleTest"; // this should be the same as the
	// SCHEME and HOST values in
	// your AndroidManifest.xml file
	String CONSUMER_KEY = "dj0yJmk9ckVUZ2Z6WktEZW43JmQ9WVdrOVRrcFNZMnN5TkdrbWNHbzlNQS0tJnM9Y29uc3VtZXJzZWNyZXQmeD00YQ--";//
	String CONSUMER_SECRET = "fff1202503a8c0bd8af0ae5d1e0c34efdf3dab2b";
	private CommonsHttpOAuthConsumer myConsumer;
	private CommonsHttpOAuthProvider myProvider;
	private String requestToken;
	private String accessToken;
	
	private String oauth_token;
	private String oauth_verifier;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				callOAuth();
			}
		}).start();
		
		// createPinDialog().show();
	}

	private void callOAuth() {
		try {
			// retrieve the consumer token and then sign it
			myConsumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY,	CONSUMER_SECRET);

			myConsumer.setMessageSigner(new HmacSha1MessageSigner());

			HttpClient client = new DefaultHttpClient();
			// retrieve the provider by using the signed consumer token
			myProvider = new CommonsHttpOAuthProvider(REQUEST_TOKEN_ENDPOINT_URL, ACCESS_TOKEN_ENDPOINT_URL, AUTHORIZE_WEBSITE_URL, client);
			myProvider.setOAuth10a(true);
			String aUrl = myProvider.retrieveRequestToken(myConsumer, CALLBACK_URL);
			Log.i(TAG, "aUrl : " + aUrl);

			requestToken = myConsumer.getToken();
			Log.i(TAG, "requestToken : " + requestToken);
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(aUrl)));
		} catch (Exception ex) {
			Log.e(TAG, "Exception : ", ex);
			Toast.makeText(getApplicationContext(), "callOAuth : " + ex.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onDestroy() {
	    Log.i(TAG, "onDestroy()");
	    super.onDestroy();
	}
	
	private String yahoo_oauth_token;
	private String yahoo_oauth_verifier;
	// this is the callback function that will run when oauth authenticates
	// successfully
	@Override
	protected void onNewIntent(Intent intent) {
		Log.i(TAG, "OnNewIntent");
		Toast.makeText(getApplicationContext(), "OnNewIntent - It works!",
				Toast.LENGTH_LONG).show();
		Uri uriData = intent.getData();
		if (uriData != null && uriData.toString().startsWith(CALLBACK_URL)) {
			
			yahoo_oauth_token = uriData.getQueryParameter("oauth_token");
			yahoo_oauth_verifier = uriData.getQueryParameter("oauth_verifier");
			
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						String request = "https://api.login.yahoo.com/oauth/v2/get_token?oauth_consumer_key=" + myConsumer.getConsumerKey() + "&oauth_signature_method=PLAINTEXT&oauth_nonce=" + generateRandomString(11) + "&oauth_signature=" + myConsumer.getConsumerSecret() + "%26"
							 + myConsumer.getTokenSecret() + "&oauth_timestamp=" + new Date().getTime() + "&oauth_verifier=" + yahoo_oauth_verifier + "&oauth_version=1.0&oauth_token=" + yahoo_oauth_token;
						NetworkConnect networkConnect = new NetworkConnect();
						String response = networkConnect.httpsPostRequest(request);
						Log.i(TAG, "response : " + response);
						String[] keyAndValueArray = response.split("&");
						String oauthToken = "";
						String xoauthYahooGuid = "";
						String oauthTokenSecret = "";
						for(int i=0;i<keyAndValueArray.length;i++){
							String[] keyAndValue = keyAndValueArray[i].split("=");
							if(keyAndValue[0].equals("oauth_token")){
								oauthToken = keyAndValue[1];
							}else if(keyAndValue[0].equals("xoauth_yahoo_guid")){
								xoauthYahooGuid = keyAndValue[1];
							}else if(keyAndValue[0].equals("oauth_token_secret")){
								oauthTokenSecret = keyAndValue[1];
							}
						}
						
						String host_url = "https://social.yahooapis.com/v1/user/" + xoauthYahooGuid + "/profile?format=json";
						
						String timeStamp = ""+(System.currentTimeMillis()/1000L);
						String nonce = generateRandomString(11);
						String params = 
				                ""+encode("oauth_consumer_key")+"=" + encode(myConsumer.getConsumerKey())
				                + "&"+encode("oauth_nonce")+"="+encode(nonce)
				                + "&"+encode("oauth_signature_method")+"="+encode("HMAC-SHA1")
				                + "&"+encode("oauth_timestamp")+"="+encode(timeStamp)
				                + "&"+encode("oauth_token")+"="+oauthToken
				                + "&"+encode("oauth_version")+"="+encode("1.0")

				                ;
				        String baseString = encode("POST")+"&"+encode(host_url)+"&"+encode(params);
				        String signingKey = encode(myConsumer.getConsumerSecret())+"38"+encode(oauthTokenSecret);
				        Log.i(TAG, "base string: " + baseString);
				        Log.i(TAG, "key : " + signingKey);
				        String lSignature = computeHmac(baseString, signingKey);
				        Log.i(TAG, "signature: " + lSignature);
				        lSignature = encode(lSignature);
						
						String getEmail = "https://social.yahooapis.com/v1/user/" + xoauthYahooGuid + "/profile?format=json"
				                  //+ "&realm=yahooapis.com"
				                  + "&oauth_consumer_key="
				                  + myConsumer.getConsumerKey()
				                  + "&oauth_nonce="
				                  + nonce
				                  + "&oauth_signature_method=" + "HMAC-SHA1"
				                  + "&oauth_timestamp="
				                  + timeStamp
				                  + "&oauth_token="
				                  + oauthToken
				                  + "&oauth_version="
				                  + "1.0"
				                  + "&oauth_signature=" + lSignature;
				        
						Log.i(TAG, "getEmail : " + getEmail);
						String response2 = networkConnect.httpsPostRequest(getEmail);
						Log.i(TAG, "response2 : " + response2);
						
						/*
						String randomString = ServerConstant.generateRandomString(11);
						String timestageString = String.valueOf((System.currentTimeMillis()/1000L));
						String url = "http://social.yahooapis.com/v1/user/" + xoauthYahooGuid + "/profile?format=json";
					    StringBuilder body = new StringBuilder();
					    DefaultHttpClient httpclient = new DefaultHttpClient(); // create new httpClient
					    HttpGet httpGet = new HttpGet(url); // create new httpGet object
					    httpGet.setHeader("Authorization: OAuth realm", "yahooapis.com");
					    httpGet.setHeader("oauth_consumer_key", myConsumer.getConsumerKey());
					    httpGet.setHeader("oauth_nonce", randomString);
					    httpGet.setHeader("oauth_signature_method", "HMAC-SHA1");
					    httpGet.setHeader("oauth_timestamp", timestageString);
					    httpGet.setHeader("oauth_token", oauthToken);
					    httpGet.setHeader("oauth_version", ServerConstant.YAHOO_OAUTH_VERSION_VALUE);
					    String hmacSha1Value = "GET&http%3A%2F%2Fsocial.yahooapis.com%2Fv1%2Fuser%2F" + xoauthYahooGuid + "%2Fprofile&format%3Djson"
					    		+ "%26oauth_consumer_key%3D" + myConsumer.getConsumerKey()
					    		+ "%26oauth_nonce%3D" + randomString
					    		+ "%26oauth_signature_method%3DHMAC-SHA1" 
					    		+ "%26oauth_timestamp%3D" + timestageString
					    		+ "%26oauth_token%3D" + oauthToken
					    		+ "%26oauth_version%3D" + ServerConstant.YAHOO_OAUTH_VERSION_VALUE;
					    httpGet.setHeader("oauth_signature", calculateRFC2104HMAC(hmacSha1Value, myConsumer.getConsumerSecret() + "%26" + oauthTokenSecret));
					    //httpGet.setHeader("oauth_signature", myConsumer.getConsumerSecret() + "%26" + oauthTokenSecret);
					    HttpResponse response2;
					    try {
					    	response2 = httpclient.execute(httpGet); // execute httpGet
					        StatusLine statusLine = response2.getStatusLine();
					        int statusCode = statusLine.getStatusCode();
					        if (statusCode == HttpStatus.SC_OK) {
					            // System.out.println(statusLine);
					            body.append(statusLine + "\n");
					            HttpEntity e = response2.getEntity();
					            String entity = EntityUtils.toString(e);
					            body.append(entity);
					        } else {
					            body.append(statusLine + "\n");
					            // System.out.println(statusLine);
					        }
					    } catch (ClientProtocolException e) {
					        e.printStackTrace();
					    } catch (IOException e) {
					        e.printStackTrace();
					    } finally {
					        httpGet.abort(); // stop connection
					    }
					    Log.i(TAG, "body : " + body.toString());
						*/
						
						/*
						HttpClient httpclient = new DefaultHttpClient();
						
						String host_url = "http://social.yahooapis.com/v1/user/" + xoauthYahooGuid+ "/contacts";
						
						String timeStamp = ""+(System.currentTimeMillis()/1000L);
						String nonce = ServerConstant.generateRandomString(11);
						String params = 
				                ""+encode("oauth_consumer_key")+"=" + encode(myConsumer.getConsumerKey())
				                + "&"+encode("oauth_nonce")+"="+encode(nonce)
				                + "&"+encode("oauth_signature_method")+"="+encode("HMAC-SHA1")
				                + "&"+encode("oauth_timestamp")+"="+encode(timeStamp)
				                + "&"+encode("oauth_token")+"="+oauthToken
				                + "&"+encode("oauth_version")+"="+encode("1.0")

				                ;
				        String baseString = encode("GET")+"&"+encode(host_url)+"&"+encode(params);
				        String signingKey = encode(myConsumer.getConsumerSecret())+"&"+encode(oauthTokenSecret);
				        Log.i(TAG, "base string: " + baseString);
				        String lSignature = computeHmac(baseString, signingKey);
				        Log.i(TAG, "signature: " + lSignature);
				        lSignature = encode(lSignature);
				        Log.i(TAG, "signature enacoded: " + lSignature);

				        String lRequestUrl = host_url
				                            + "?oauth_consumer_key="+myConsumer.getConsumerKey()
				                            + "&oauth_nonce="+nonce
				                            + "&oauth_signature_method=HMAC-SHA1"
				                            + "&oauth_timestamp="+timeStamp
				                            + "&oauth_token="+oauthToken
				                            + "&oauth_version=1.0"
				                            + "&oauth_signature="+lSignature;
				        Log.i(TAG, lRequestUrl);
				        HttpGet httpget = new HttpGet(lRequestUrl);
				        ResponseHandler<String> responseHandler = new BasicResponseHandler();
				        String responseBody = httpclient.execute(httpget, responseHandler);
				        Log.i(TAG, "responseBody : " + responseBody);
				        */
					} catch (Exception e) {
						Log.e(TAG, "Exception : ", e);
					}
					
				}
			}).start();
		}
	}

	AlertDialog createPinDialog() {
		Log.i(TAG, "createPinDialog");
		LayoutInflater factory = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		// LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.pin, null);
		final EditText pinText = (EditText) textEntryView
				.findViewById(R.id.pin_text);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Twitter OAuth PIN");
		builder.setView(textEntryView);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if (pinText != null)
					gotOAuthPin(pinText.getText().toString());
				onResume();
			}
		});
		return builder.create();
	}

	private void gotOAuthPin(String pin) {
		Log.i(TAG, "pin : " + pin);
		SharedPreferences.Editor editor = getSharedPreferences("yahoo", MODE_PRIVATE).edit();
		try {
			myProvider.retrieveAccessToken(myConsumer, pin);
			accessToken = myConsumer.getToken();
			Log.i(TAG, "accessToken : " + accessToken);
		} catch (OAuthMessageSignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthNotAuthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (accessToken != null && accessToken.length() > 0) {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(TestMainActivity.this, "Authorized", Toast.LENGTH_SHORT).show();
				}
			});
			
			HttpPost request = new HttpPost(
					"http://social.yahooapis.com/v1/user/profile?format=json");
			StringEntity body = null;
			/*
			 * try { body = new StringEntity("city=hamburg&label=" +
			 * URLEncoder.encode("Send via Signpost!", "UTF-8")); } catch
			 * (UnsupportedEncodingException e1) { // TODO Auto-generated catch
			 * block e1.printStackTrace(); }
			 * body.setContentType("application/x-www-form-urlencoded");
			 * request.setEntity(body);
			 */

			try {
				myConsumer.sign(request);
			} catch (OAuthMessageSignerException e1) {
				Log.e(TAG, "OAuthMessageSignerException : ", e1);
			} catch (OAuthExpectationFailedException e1) {
				Log.e(TAG, "OAuthExpectationFailedException : ", e1);
			} catch (OAuthCommunicationException e1) {
				Log.e(TAG, "OAuthCommunicationException : ", e1);
			}

			Log.i(TAG, "Sending update request to Fire Eagle... : " + request.getURI().getQuery());
			HttpClient httpClient = new DefaultHttpClient();
			//HttpResponse response = null;
			try {
				responseGetProfile = httpClient.execute(request);
			} catch (ClientProtocolException e) {
				Log.e(TAG, "ClientProtocolException : ", e);
			} catch (IOException e) {
				Log.e(TAG, "IOException : ", e);
			}

			Log.i(TAG, "Response: getStatusCode is " + responseGetProfile.getStatusLine().getStatusCode()
					+ ", getReasonPhrase is " + responseGetProfile.getStatusLine().getReasonPhrase());
			Log.i(TAG, "responseGetProfile : " + responseGetProfile.getParams().toString());
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(
							TestMainActivity.this,
							"Response: getStatusCode is" + responseGetProfile.getStatusLine().getStatusCode()
									+ ", getReasonPhrase is" + responseGetProfile.getStatusLine().getReasonPhrase(),
							Toast.LENGTH_SHORT).show();
					
				}
			});
			
		} else {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(TestMainActivity.this, "Not Authorized", Toast.LENGTH_SHORT).show();	
				}
			});
		}
	}
	
	private HttpResponse responseGetProfile = null;

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PIN_DIALOG:
			LayoutInflater factory = LayoutInflater.from(this);
			final View textEntryView = factory.inflate(R.layout.pin, null);
			final EditText pinText = (EditText) textEntryView.findViewById(R.id.pin_text);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("OAuth PIN");
			builder.setView(textEntryView);
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,	int whichButton) {
							if (pinText != null){
								new Thread(new Runnable() {
									
									@Override
									public void run() {
										gotOAuthPin(pinText.getText().toString());
									}
								}).start();
							}
						}
					});
			return builder.create();
		}

		return super.onCreateDialog(id);
	}
	
	/*
     * 隨機產生字串 yahoo api 使用
     */
	private static String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    public static String generateRandomString(int length) throws Exception {

		StringBuffer buffer = new StringBuffer();

		int charactersLength = characters.length();

		for (int i = 0; i < length; i++) {
			double index = Math.random() * charactersLength;
			buffer.append(characters.charAt((int) index));
		}
		return buffer.toString();
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
    
    public String encode(String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (isUnsafe(ch)) {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            } else {
                resultStr.append(ch);
            }
        }
        return resultStr.toString().trim();
    }
	
	private boolean isUnsafe(char ch) {
        if (ch > 128 || ch < 0)
            return true;
        return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
    }
	
	private char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }
	
	public String computeHmac(String baseString, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes("UTF-8"),
                    "HMAC-SHA1");
            mac.init(signingKey);
            byte[] digest = mac.doFinal(baseString.getBytes());
            String result = Base64.encodeToString(digest, Base64.URL_SAFE);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "error while generating sha", e);
        }
        return null;

    }
}