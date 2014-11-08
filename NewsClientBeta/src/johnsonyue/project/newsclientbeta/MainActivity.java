package johnsonyue.project.newsclientbeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {	
	private String site;
	
	private int cId=1;
	
	private Button SearchButton;
	private LinearLayout CategoryLayout;
	private ListView NewsList;
	private ProgressBar mainProBar;
	
	private SimpleAdapter adapter;
	private ArrayList<Map<String,Object>> NewsTest=new ArrayList<Map<String,Object>>();
	private ArrayList<Map<String,Object>> CategoryTest=new ArrayList<Map<String,Object>>();
	private LoadNewsListAsyncTask asyncTask;
	
	private String[] categories=new String[]{"C1","C2","C3","C4","C5","C6","C7","C8"}; 
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences=getSharedPreferences("login",Context.MODE_PRIVATE);
		Editor editor=preferences.edit();
		editor.putBoolean("isLoggedIn", false);
		editor.putInt("user_id", -1);
		editor.commit();
        
        Properties pro=new Properties();
        try {
			pro.load(this.getAssets().open("config.properties"));
		} catch (Exception e) {
			e.printStackTrace();
		}
        site=pro.getProperty("site");
        
        mainProBar=(ProgressBar)findViewById(R.id.main_pro_bar);
        
        SearchButton=(Button)findViewById(R.id.search_button);
        SearchButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i=new Intent(MainActivity.this,SearchActivity.class);
				startActivity(i);
			}});
        
        asyncTask=new LoadNewsListAsyncTask();
        asyncTask.execute(cId);
        
        fillCategoryTest();
        String[] from1=new String[]{"category"};
        int[] to1=new int[]{R.id.category_title};
        SimpleAdapter adapter1=new SimpleAdapter(this,CategoryTest,R.layout.category_title_layout,from1,to1);
        GridView gridView=new GridView(this);
        gridView.setColumnWidth(200);
        gridView.setNumColumns(GridView.AUTO_FIT);
        gridView.setGravity(Gravity.CENTER);
        gridView.setSelector(R.drawable.category_bar_selector);
        LayoutParams params = new LayoutParams(1600, LayoutParams.MATCH_PARENT);
		gridView.setLayoutParams(params);
        gridView.setAdapter(adapter1);
        CategoryLayout=(LinearLayout)findViewById(R.id.category_layout);
        CategoryLayout.addView(gridView);
        
        NewsList=(ListView)findViewById(R.id.news_list);
        NewsList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				Intent i = new Intent(MainActivity.this,NewsDetailActivity.class);
				Bundle bundle=new Bundle();
				
				TextView title=(TextView)view.findViewById(R.id.title);
				TextView snapShot=(TextView)view.findViewById(R.id.snap_shot);
				TextView newsSource=(TextView)view.findViewById(R.id.news_source);
				TextView date=(TextView)view.findViewById(R.id.date);
				bundle.putInt("news_id",(Integer) NewsTest.get(position).get("news_id"));
				bundle.putString("title", title.getText().toString());
				bundle.putString("snapShot", snapShot.getText().toString());
				bundle.putString("newsSource", newsSource.getText().toString());
				bundle.putString("date", date.getText().toString());
				bundle.putString("body",(String)NewsTest.get(position).get("body"));
				
				i.putExtras(bundle);
				startActivity(i);
			}
        	
        });
        
        gridView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				// TODO Auto-generated
				cId=position+1;
				asyncTask=new LoadNewsListAsyncTask();
				asyncTask.execute(cId);
			}});
    }
    
    private void fillListTest(int cId){
    	NewsTest=new ArrayList<Map<String,Object>>();
    	String resultStr=null;
    	String url=site+"/getSpecNews";
    	String[] params={"category="+cId};
    	try {
			resultStr=SyncHttp.httpGet(url,params);
    		JSONObject jObject=new JSONObject(resultStr);
    		int retCode=jObject.getInt("ret");
    		if(retCode==0){
    			JSONObject data=jObject.getJSONObject("data");
    			JSONArray jArray=data.getJSONArray("newslist");
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
    	    		NewsTest.add(t);
    			}
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    }
    
    private void fillCategoryTest(){
    	String[] categoryArray = getResources().getStringArray(R.array.categories);
    	for(int i=0;i<categories.length;i++){
    		HashMap<String,Object> t=new HashMap<String, Object>();
    		t.put("category", categoryArray[i]);
    		CategoryTest.add(t);
    	}
    }
    
    private class LoadNewsListAsyncTask extends AsyncTask<Object, Integer, Integer>{
		@Override
		protected Integer doInBackground(Object... params) {
			// TODO Auto-generated method stub
			fillListTest((Integer)params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			String[] from=new String[]{"pic","title","snapShot","date","newsSource"};
	        int[] to=new int[]{R.id.pic,R.id.title,R.id.snap_shot,R.id.date,R.id.news_source};
	        adapter=new SimpleAdapter(MainActivity.this, NewsTest, R.layout.item_layout,from,to);
	        NewsList.setAdapter(adapter);
	        mainProBar.setVisibility(ProgressBar.GONE);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			mainProBar.setVisibility(ProgressBar.VISIBLE);
			Toast.makeText(MainActivity.this, "Fetching news list ...", 500).show();
		}

    }
}
