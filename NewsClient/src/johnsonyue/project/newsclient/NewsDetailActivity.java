package johnsonyue.project.newsclient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class NewsDetailActivity extends Activity {
	public static final int KEY_LOGIN=0;
	
	private TextView titleText,detailText,bodyText;
	private Button submitBtn,viewCommentBtn,favouriteBtn;
	private EditText commentEdit;
	private ProgressBar detailProBar;
	private ProgressBar relatedProBar;
	private ListView relatedNewsListView;
	private ViewPager viewPager;
	private List<View> viewList;
	
	private CommentAsyncTask asyncTask;
	private LoadRelatedAsyncTask relatedAsyncTask;
	private String site;
	private int newsId;
	private String title;
	private String date;
	private String snapShot;
	private String newsSource;
	private String body;
	private String related;
	private boolean firstFetch=true;
	private boolean favAdded=false;

	private ArrayList<Map<String, Object>> relatedNewsList;
	private SimpleAdapter adapter;

	private DBHelper mDBHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//Set to no title full screen.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		SharedPreferences preferences2=getSharedPreferences("theme",Context.MODE_PRIVATE);
        int id=preferences2.getInt("theme_id", MainActivity.GREEN_THEME_ID);
        if(id==MainActivity.DARK_THEME_ID){
            setTheme(R.style.DarkTheme);
        }
        else{
        	setTheme(R.style.GreenTheme);
        }
        
		setContentView(R.layout.view_comment_layout);
        
		setContentView(R.layout.news_detail_pager);
		
		Properties pro=new Properties();
        try {
			pro.load(this.getAssets().open("config.properties"));
		} catch (Exception e) {
			e.printStackTrace();
		}
        site=pro.getProperty("site");
		
        mDBHelper=new DBHelper(this);
        mDBHelper.open();
        
		Intent i = getIntent();
		Bundle bundle = i.getExtras();
		title=bundle.getString("title");
		date=bundle.getString("date");
		snapShot=bundle.getString("snapShot");
		newsSource=bundle.getString("newsSource");
		body=bundle.getString("body");
		newsId=bundle.getInt("news_id");
		related=bundle.getString("related");
		
        favAdded=mDBHelper.isAdded(newsId);

		LayoutInflater inflater=this.getLayoutInflater();
		View detailView=inflater.inflate(R.layout.news_detail_layout, null);
		detailProBar=(ProgressBar)detailView.findViewById(R.id.detail_pro_bar);
		detailProBar.setVisibility(ProgressBar.GONE);
		
		commentEdit=(EditText)detailView.findViewById(R.id.comment_edit);
		
		titleText=(TextView)detailView.findViewById(R.id.title_text);
		titleText.setText(title);
		detailText=(TextView)detailView.findViewById(R.id.detail_text);
		detailText.setText(date+" "+snapShot+" "+newsSource);
		bodyText=(TextView)detailView.findViewById(R.id.body_text);
		bodyText.setText(body);
		
		submitBtn=(Button)detailView.findViewById(R.id.submit_btn);
		submitBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SharedPreferences preferences=getSharedPreferences("login", Context.MODE_PRIVATE);
				boolean isLoggedIn=preferences.getBoolean("isLoggedIn",false);
				if(!isLoggedIn){
					Intent i=new Intent(NewsDetailActivity.this,LoginActivity.class);
					startActivityForResult(i,0);
				}
				else{
					if(commentEdit.getText().toString().isEmpty()){
						Toast.makeText(NewsDetailActivity.this,"Empty comment",Toast.LENGTH_SHORT).show();
						return;
					}
					String url=site+"/comment";
					asyncTask=new CommentAsyncTask();
					asyncTask.execute(url);
				}
			}});
		
		viewCommentBtn=(Button)detailView.findViewById(R.id.view_comment_btn);
		viewCommentBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(NewsDetailActivity.this,ViewCommentActivity.class);
				Bundle bundle = new Bundle();
				String title=titleText.getText().toString();
				String detail=detailText.getText().toString();
				
				bundle.putInt("news_id",newsId);
				bundle.putString("title",title);
				bundle.putString("detail",detail);
				i.putExtras(bundle);
				startActivity(i);
			}});
		
		favouriteBtn=(Button)detailView.findViewById(R.id.favourite_btn);
		if(favAdded){
			favouriteBtn.setBackgroundResource(R.drawable.favourite_selected);
		}
		favouriteBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!favAdded){
					ContentValues values=new ContentValues();
					values.put(DBHelper.KEY_NEWS_ID,newsId);
					values.put(DBHelper.KEY_TITLE,title);
					values.put(DBHelper.KEY_SNAPSHOT,snapShot);
					values.put(DBHelper.KEY_DATE,date);
					values.put(DBHelper.KEY_BODY,body);
					values.put(DBHelper.KEY_NEWS_SOURCE,newsSource);
					values.put(DBHelper.KEY_RELATED, related);
					mDBHelper.insert(values);
					favouriteBtn.setBackgroundResource(R.drawable.favourite_selected);
					Toast.makeText(NewsDetailActivity.this, "Favourite Added.", Toast.LENGTH_SHORT).show();
					favAdded=true;
				}
				else{
					mDBHelper.delete(newsId);
					favouriteBtn.setBackgroundResource(R.drawable.favourite_not_selected);
					Toast.makeText(NewsDetailActivity.this, "Deleted from Favourite.", Toast.LENGTH_SHORT).show();
					favAdded=false;
				}
			}});

		View relatedView=inflater.inflate(R.layout.related_news_layout, null);
		
		relatedProBar=(ProgressBar)relatedView.findViewById(R.id.related_pro_bar);
		relatedNewsListView=(ListView)relatedView.findViewById(R.id.related_news_list);
		relatedNewsListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				Intent i = new Intent(NewsDetailActivity.this,NewsDetailActivity.class);
				Bundle bundle=new Bundle();
				
				bundle.putInt("news_id",(Integer) relatedNewsList.get(position).get("news_id"));
				bundle.putString("title",(String) relatedNewsList.get(position).get("title"));
				bundle.putString("snapShot", (String) relatedNewsList.get(position).get("snapShot"));
				bundle.putString("newsSource", (String) relatedNewsList.get(position).get("newsSource"));
				bundle.putString("date", (String) relatedNewsList.get(position).get("date"));
				bundle.putString("body",(String) relatedNewsList.get(position).get("body"));
				bundle.putString("related", (String) relatedNewsList.get(position).get("related"));
				
				i.putExtras(bundle);
				startActivityForResult(i,MainActivity.NEWS_LIST);
			}});
        
        site=pro.getProperty("site");
		viewList=new ArrayList<View>();
		viewList.add(detailView);
		viewList.add(relatedView);
		viewPager=(ViewPager)findViewById(R.id.view_pager);
		viewPager.setAdapter(new MyPagerAdapter(viewList));
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch(requestCode){
		case KEY_LOGIN:
				boolean allowed=false;
				int userId=-1;
				if(data!=null){
					allowed=data.getExtras().getBoolean("allowed");
					userId=data.getExtras().getInt("user_id");
				}
				if(allowed){
					SharedPreferences preferences=getSharedPreferences("login",Context.MODE_PRIVATE);
					Editor editor=preferences.edit();
					editor.putBoolean("isLoggedIn", true);
					editor.putInt("user_id", userId);
					Log.v("dbg","user_id=="+userId);
					editor.commit();
				}
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private String comment(String url){
		SharedPreferences preferences=getSharedPreferences("login",Context.MODE_PRIVATE);
		int userId=preferences.getInt("user_id", -1);
		
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");       
		String date = sDateFormat.format(new java.util.Date());
		
		String[] params={"user_id="+userId, "news_id="+newsId, "date="+date, "comment="+commentEdit.getText().toString()};
		String ret="Network failed.";
		
		try {
			String t=SyncHttp.httpGet(url, params);
			if(Integer.parseInt(t)==0){
				ret="Submitted.";
			}
			else{
				ret="Failed to comment.";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	
	private class CommentAsyncTask extends AsyncTask<String, Integer, String>{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String ret = comment(params[0]);
			return ret;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			detailProBar.setVisibility(ProgressBar.GONE);
			Toast.makeText(NewsDetailActivity.this, result, Toast.LENGTH_SHORT).show();
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			detailProBar.setVisibility(ProgressBar.VISIBLE);
			Toast.makeText(NewsDetailActivity.this, "Submitting..", Toast.LENGTH_SHORT).show();
			super.onPreExecute();
		}
		
	}
	
	private void fillList(String rids){
		String[] ids=rids.split(",");
		relatedNewsList=new ArrayList<Map<String,Object>>();
		if(ids.length<=1){
			return;
		}
		else{
			String resultStr=null;
	    	String url=site+"/getNews";
	    	String[] params={"ids="+rids};
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
	    	    		t.put("related", newsObject.get("related"));
	    	    		relatedNewsList.add(t);
	    			};
	    			
	    		}
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}
		}
	}
	
	private class LoadRelatedAsyncTask extends AsyncTask<Object, Integer, Integer>{

		@Override
		protected Integer doInBackground(Object... params) {
			// TODO Auto-generated method stub
			fillList((String)params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			String[] from=new String[]{"pic","title","snapShot","date","newsSource"};
	        int[] to=new int[]{R.id.pic,R.id.title,R.id.snap_shot,R.id.date,R.id.news_source};
	        adapter=new SimpleAdapter(NewsDetailActivity.this, relatedNewsList, R.layout.item_layout,from,to);
	        relatedNewsListView.setAdapter(adapter);
	        relatedProBar.setVisibility(ProgressBar.GONE);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			relatedProBar.setVisibility(ProgressBar.VISIBLE);
		}
		
	}

	private class MyPagerAdapter extends PagerAdapter{
		List<View> viewList;
		
		public MyPagerAdapter(List<View> list){
			viewList=list;
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return viewList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return (arg0==arg1);
		}
		@Override
		public void destroyItem(View container, int position, Object object) {
			// TODO Auto-generated method stub
			((ViewPager)container).removeView(viewList.get(position));
		}
		@Override
		public Object instantiateItem(View container, int position) {
			// TODO Auto-generated method stub
			((ViewPager)container).addView(viewList.get(position));
			return viewList.get(position);
		}
		
	}
	
	private class MyOnPageChangeListener implements OnPageChangeListener{

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageSelected(int arg0) {
			// TODO Auto-generated method stub
			if(arg0==1){
				if(firstFetch){
					relatedAsyncTask=new LoadRelatedAsyncTask();
					relatedAsyncTask.execute(related);
					firstFetch=false;
				}
			}
		}
		
	}
}
