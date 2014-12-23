package johnsonyue.project.newsclient;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class SyncHttp {
	public static String httpGet(String url, String[] params) throws Exception
	{	
		String response = null; //返回信息
		
		url += "?"+params[0];
		for(int i=1;i<params.length;i++){
			url += "&"+params[i];
		}
		url=url.replaceAll(" ", "%20");
		Log.v("dbg",url);
		
		final int timeoutConnection = 10000;  
		final int timeoutSocket = 10000;  
		HttpParams httpParameters = new BasicHttpParams();// Set the timeout in milliseconds until a connection is established.  
	    HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);// Set the default socket timeout (SO_TIMEOUT) // in milliseconds which is the timeout for waiting for data.  
	    HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);  
	    
		// 构造HttpClient的实例
		HttpClient httpClient = new DefaultHttpClient(httpParameters);  
		// 创建GET方法的实例
		HttpGet httpGet = new HttpGet(url);
    	
		try
		{
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK){
				// 获得返回结果
				response = EntityUtils.toString(httpResponse.getEntity());
			}
			else{
				response = "返回码："+statusCode;
			}
		}catch (Exception e){
			e.printStackTrace();
		} 
		return response;
	}
}
