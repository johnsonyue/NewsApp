package johnsonyue.project.newsclientbeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends Activity {
	private Button submitBtn;
	private Button backBtn;
	private EditText searchEdit;
	private ListView resultList;
	
	private ArrayList<Map<String,Object>> results;
	private LoadSearchResultAsyncTask asyncTask;
	
	private String site;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_activity);
		
		Properties pro=new Properties();
        try {
			pro.load(this.getAssets().open("config.properties"));
		} catch (Exception e) {
			e.printStackTrace();
		}
        site=pro.getProperty("site");
		
		searchEdit=(EditText)findViewById(R.id.search_edit);
		
		submitBtn=(Button)findViewById(R.id.submit_btn);
		submitBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String key=searchEdit.getText().toString();
				if(!key.isEmpty()){
					Toast.makeText(SearchActivity.this, "Searching news containing \""+key+"\" ...", Toast.LENGTH_SHORT).show();
					
					asyncTask=new LoadSearchResultAsyncTask();
					asyncTask.execute(key);
				}
				else{
					Toast.makeText(SearchActivity.this, "Empty key word!", Toast.LENGTH_SHORT).show();
				}
			}});
		
		backBtn=(Button)findViewById(R.id.back_btn);
		backBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}});
		
		results=new ArrayList<Map<String,Object>>();
		resultList=(ListView)findViewById(R.id.result_list);
		resultList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				Intent i = new Intent(SearchActivity.this,NewsDetailActivity.class);
				Bundle bundle=new Bundle();
				
				TextView title=(TextView)view.findViewById(R.id.title);
				TextView snapShot=(TextView)view.findViewById(R.id.snap_shot);
				TextView newsSource=(TextView)view.findViewById(R.id.news_source);
				TextView date=(TextView)view.findViewById(R.id.date);
				bundle.putInt("news_id",(Integer) results.get(position).get("news_id"));
				bundle.putString("title", title.getText().toString());
				bundle.putString("snapShot", snapShot.getText().toString());
				bundle.putString("newsSource", newsSource.getText().toString());
				bundle.putString("date", date.getText().toString());
				bundle.putString("body",(String)results.get(position).get("body"));
				
				i.putExtras(bundle);
				startActivity(i);
			}});
	}
	
	private void fillList(String key){
		results=new ArrayList<Map<String,Object>>();
		String resultStr=null;
    	String url=site+"/getSearchResult";
    	String[] params={"keyword="+key};
    	
    	try {
			resultStr=SyncHttp.httpGet(url,params);
    		JSONObject jObject=new JSONObject(resultStr);
    		int retCode=jObject.getInt("ret");
    		if(retCode==0){
    			JSONObject data=jObject.getJSONObject("data");
    			JSONArray jArray=data.getJSONArray("result");
    			for(int i=0;i<jArray.length();i++){
    				Map<String,Object> t=new HashMap<String,Object>();
    				JSONObject newsObject = (JSONObject)jArray.opt(i);
    				t.put("news_id",newsObject.get("id"));
    				t.put("pic", R.drawable.pic);
    	    		t.put("title",	newsObject.get("title"));
    	    		t.put("snapShot", newsObject.get("snap_shot"));
    	    		t.put("date", newsObject.get("date"));
    	    		t.put("newsSource", newsObject.get("source"));
    	    		t.put("body", newsObject.get("body"));
    	    		results.add(t);
    			}
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
		/*
		for(int i=0;i<20;i++){
    		Map<String,Object> t=new HashMap<String,Object>();
    		t.put("pic", R.drawable.pic);
    		t.put("title", key+i);
    		t.put("snapShot", "unavailable");
    		t.put("date", "2014-10-21");
    		t.put("newsSource", "unknown");
    		results.add(t);
    	}
		*/
		
	}
	
	private class LoadSearchResultAsyncTask extends AsyncTask<String, Integer, String>{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			fillList(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			String[] from=new String[]{"pic","title","snapShot","date","newsSource"};
	        int[] to=new int[]{R.id.pic,R.id.title,R.id.snap_shot,R.id.date,R.id.news_source};
	        SimpleAdapter adapter=new SimpleAdapter(SearchActivity.this,results,R.layout.item_layout,from,to);
	        resultList.setAdapter(adapter);
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
		
	}
}

