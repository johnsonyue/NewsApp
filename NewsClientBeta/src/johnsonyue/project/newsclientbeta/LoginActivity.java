package johnsonyue.project.newsclientbeta;

import java.util.Properties;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class LoginActivity extends Activity {

	private EditText nickNameEdit,passwordEdit;
	private Button loginBtn,registerBtn;
	private ProgressBar loginProBar;
	private String site;
	private LoginAsyncTask asyncTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity_layout);
		
		Properties pro=new Properties();
        try {
			pro.load(this.getAssets().open("config.properties"));
		} catch (Exception e) {
			e.printStackTrace();
		}
        site=pro.getProperty("site");
        
        loginProBar=(ProgressBar)findViewById(R.id.login_pro_bar);
        loginProBar.setVisibility(ProgressBar.GONE);
        
		loginBtn=(Button)findViewById(R.id.login_btn);
		loginBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				String url=site+"/login";
				asyncTask=new LoginAsyncTask();
				asyncTask.execute(url);
			}
		});
		
		registerBtn=(Button)findViewById(R.id.register_btn);
		registerBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent i = new Intent(LoginActivity.this,RegisterActivity.class);
				startActivity(i);
			}}
		);
		
		nickNameEdit=(EditText)findViewById(R.id.nick_name_edit);
		passwordEdit=(EditText)findViewById(R.id.password_edit);
	}
	
	private String login(String url){
		String[] params={"nick_name="+nickNameEdit.getText().toString(),
				"password="+passwordEdit.getText().toString()};
		String ret="Network failed.";
		
		try{
			String t=SyncHttp.httpGet(url, params);
			if(Integer.parseInt(t)!=-1){
				ret="Successfully Logged In.";
				Intent i=new Intent(LoginActivity.this,NewsDetailActivity.class);
				Bundle extras=new Bundle();
				
				extras.putBoolean("allowed", true);
				extras.putInt("user_id", Integer.parseInt(t));
				Log.v("dbg",Integer.parseInt(t)+" putted");
				i.putExtras(extras);
				setResult(NewsDetailActivity.KEY_LOGIN,i);
				finish();
			}
			else{
				ret="Login Failed";
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return ret;
	}
	
	private class LoginAsyncTask extends AsyncTask<String,Integer,String>{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String ret=login(params[0]);
			return ret;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			loginProBar.setVisibility(ProgressBar.GONE);
			Toast.makeText(LoginActivity.this, result, 500).show();
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			loginProBar.setVisibility(ProgressBar.VISIBLE);
			Toast.makeText(LoginActivity.this, "Logging in...", 500).show();
		}
		
	}
}
