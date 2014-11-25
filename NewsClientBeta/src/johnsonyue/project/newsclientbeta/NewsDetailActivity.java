package johnsonyue.project.newsclientbeta;

import java.text.SimpleDateFormat;
import java.util.Properties;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class NewsDetailActivity extends Activity {
	public static final int KEY_LOGIN=0;
	
	private TextView titleText,detailText,bodyText;
	private Button submitBtn,viewCommentBtn;
	private EditText commentEdit;
	private ProgressBar detailProBar;
	
	private CommentAsyncTask asyncTask;
	private String site;
	private int newsId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news_detail_layout);
		
		Properties pro=new Properties();
        try {
			pro.load(this.getAssets().open("config.properties"));
		} catch (Exception e) {
			e.printStackTrace();
		}
        site=pro.getProperty("site");
		
		Intent i = getIntent();
		Bundle bundle = i.getExtras();
		String title=bundle.getString("title");
		String date=bundle.getString("date");
		String snapShot=bundle.getString("snapShot");
		String newsSource=bundle.getString("newsSource");
		String body=bundle.getString("body");
		newsId=bundle.getInt("news_id");
		
		detailProBar=(ProgressBar)findViewById(R.id.detail_pro_bar);
		detailProBar.setVisibility(ProgressBar.GONE);
		
		commentEdit=(EditText)findViewById(R.id.comment_edit);
		
		titleText=(TextView)findViewById(R.id.title_text);
		titleText.setText(title);
		detailText=(TextView)findViewById(R.id.detail_text);
		detailText.setText(date+" "+snapShot+" "+newsSource);
		bodyText=(TextView)findViewById(R.id.body_text);
		bodyText.setText(body);
		
		submitBtn=(Button)findViewById(R.id.submit_btn);
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
						Toast.makeText(NewsDetailActivity.this,"Empty comment",500).show();
						return;
					}
					String url=site+"/comment";
					asyncTask=new CommentAsyncTask();
					asyncTask.execute(url);
				}
			}});
		
		viewCommentBtn=(Button)findViewById(R.id.view_comment_btn);
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
			Toast.makeText(NewsDetailActivity.this, result, 500).show();
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			detailProBar.setVisibility(ProgressBar.VISIBLE);
			Toast.makeText(NewsDetailActivity.this, "Submitting..", 500).show();
			super.onPreExecute();
		}
		
	}
}
