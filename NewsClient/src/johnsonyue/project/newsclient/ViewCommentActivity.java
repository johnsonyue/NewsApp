package johnsonyue.project.newsclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ViewCommentActivity extends Activity {
	private TextView commentTitleText,commentDetailText;
	private ListView commentList;
	private ProgressBar viewCommentProBar;
	private ArrayList<Map<String,Object>> comments;
	private LoadCommentsAsyncTask asyncTask;
	private String site;
	private int news_id;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//Set to no title full screen.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		SharedPreferences preferences2=getSharedPreferences("theme",Context.MODE_PRIVATE);
        int id=preferences2.getInt("theme_id", MainActivity.GREEN_THEME_ID);
        Log.v("dbg","theme_id: "+id);
        if(id==MainActivity.DARK_THEME_ID){
            setTheme(R.style.DarkTheme);
        }
        else{
        	setTheme(R.style.GreenTheme);
        }
		
		setContentView(R.layout.view_comment_layout);
		
		Properties pro=new Properties();
        try {
			pro.load(this.getAssets().open("config.properties"));
		} catch (Exception e) {
			e.printStackTrace();
		}
        site=pro.getProperty("site");
        
		Bundle bundle=getIntent().getExtras();
		news_id=bundle.getInt("news_id");
		
		viewCommentProBar=(ProgressBar)findViewById(R.id.view_comment_pro_bar);
		viewCommentProBar.setVisibility(ProgressBar.GONE);
		
		commentTitleText=(TextView)findViewById(R.id.comment_title_text);
		commentTitleText.setText(bundle.getString("title"));
		commentDetailText=(TextView)findViewById(R.id.comment_detail_text);
		commentDetailText.setText(bundle.getString("detail"));
		
		commentList=(ListView)findViewById(R.id.comment_list);
		
		String url=site+"/getComments";
		asyncTask=new LoadCommentsAsyncTask();
		asyncTask.execute(url);
	}
	
	private String fillCommentList(String url){
		comments=new ArrayList<Map<String,Object>>();
		String resultStr=null;
		String[] params={"news_id="+news_id};
		String ret="Network Failed.";
		
		try {
			resultStr=SyncHttp.httpGet(url,params);
    		JSONObject jObject=new JSONObject(resultStr);
    		int retCode=jObject.getInt("ret");
    		if(retCode==0){
    			ret="Comments fetched";
    			JSONObject data=jObject.getJSONObject("data");
    			JSONArray jArray=data.getJSONArray("comments");
    			for(int i=0;i<jArray.length();i++){
    				Map<String,Object> t=new HashMap<String,Object>();
    				JSONObject commentsObject = (JSONObject)jArray.opt(i);
    				t.put("name",commentsObject.get("name"));
    				t.put("comment",commentsObject.get("comment"));
    				t.put("date",commentsObject.get("date"));
    				t.put("liked",commentsObject.get("liked"));
    				t.put("disliked",commentsObject.get("disliked"));
    	    		comments.add(t);
    			}
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*
		for(int i=0;i<20;i++){
			Map<String,Object> t=new HashMap<String,Object>();
			t.put("head",R.drawable.head);
			t.put("user_name", "Ïç´åÒ°µö");
			String comment=getResources().getString(R.string.comment_demo);
			t.put("user_comment", comment);
			t.put("date", "2014-10-22");
			comments.add(t);
		}
		*/
		
		return ret;
	}
	
	private class MyBaseAdapter extends BaseAdapter{
		
		private LayoutInflater inflater;
		
		public MyBaseAdapter(Context context){
			inflater=LayoutInflater.from(context);
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return comments.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			Holder holder;
			if(convertView==null){
				holder=new Holder();
				convertView=inflater.inflate(R.layout.comment_item, null);
				holder.head=(ImageView)convertView.findViewById(R.id.user_head);
				holder.user_name=(TextView)convertView.findViewById(R.id.user_name);
				holder.like_btn=(Button)convertView.findViewById(R.id.like_btn);
				holder.dislike_btn=(Button)convertView.findViewById(R.id.dislike_btn);
				holder.user_comment=(TextView)convertView.findViewById(R.id.user_comment);
				holder.date=(TextView)convertView.findViewById(R.id.comment_date);
				holder.like_num=(TextView)convertView.findViewById(R.id.like_num);
				holder.dislike_num=(TextView)convertView.findViewById(R.id.dislike_num);
				convertView.setTag(holder);
			}
			else{
				holder=(Holder)convertView.getTag();
			}
			holder.user_name.setText(comments.get(position).get("name").toString());
			holder.like_btn.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Toast.makeText(ViewCommentActivity.this, "liked", Toast.LENGTH_SHORT).show();
				}
				
			});
			holder.dislike_btn.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Toast.makeText(ViewCommentActivity.this, "disliked", Toast.LENGTH_SHORT).show();
				}
				
			});
			holder.user_comment.setText(comments.get(position).get("comment").toString());
			holder.date.setText(comments.get(position).get("date").toString());
			holder.like_num.setText(comments.get(position).get("liked").toString());
			holder.dislike_num.setText(comments.get(position).get("disliked").toString());
			
			return convertView;
		}
		
		class Holder{
			public ImageView head;
			public TextView user_name;
			public Button like_btn;
			public Button dislike_btn;
			public TextView user_comment;
			public TextView date;
			public TextView like_num;
			public TextView dislike_num;
		}
		
	}
	
	private class LoadCommentsAsyncTask extends AsyncTask<String, Integer, String>{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String ret=fillCommentList(params[0]);
			return ret;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			MyBaseAdapter adapter=new MyBaseAdapter(ViewCommentActivity.this);
			commentList.setAdapter(adapter);
			viewCommentProBar.setVisibility(ProgressBar.GONE);
			Toast.makeText(ViewCommentActivity.this,result,500).show();
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			viewCommentProBar.setVisibility(ProgressBar.VISIBLE);
			super.onPreExecute();
		}
		
	}
}
