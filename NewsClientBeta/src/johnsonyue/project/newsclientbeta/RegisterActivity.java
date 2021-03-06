package johnsonyue.project.newsclientbeta;

import java.util.Properties;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	private Button submitBtn;
	private ProgressBar registerProBar;
	private String site;
	private RegisterAsyncTask asyncTask;
	private EditText nickNameEdit,passwordEdit,mailboxEdit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_activity);
		
		Properties pro=new Properties();
        try {
			pro.load(this.getAssets().open("config.properties"));
		} catch (Exception e) {
			e.printStackTrace();
		}
        site=pro.getProperty("site");
		
        registerProBar=(ProgressBar)findViewById(R.id.register_pro_bar);
        registerProBar.setVisibility(ProgressBar.GONE);
        
		submitBtn=(Button)findViewById(R.id.reg_btn);
		submitBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(nickNameEdit.getText().toString().isEmpty()||passwordEdit.getText().toString().isEmpty()||mailboxEdit.getText().toString().isEmpty()){
					Toast.makeText(RegisterActivity.this,"Invalid Input",500).show();
					return;
				}
				
				String url=site+"/register";
				asyncTask=new RegisterAsyncTask();
				asyncTask.execute(url);
			}}
		);
		
		nickNameEdit=(EditText)findViewById(R.id.reg_nick_name_edit);
		passwordEdit=(EditText)findViewById(R.id.reg_password_edit);
		mailboxEdit=(EditText)findViewById(R.id.reg_mailbox_edit);
	}
	
	private String register(String url){
		String[] params={"nick_name="+nickNameEdit.getText().toString(),
				"password="+passwordEdit.getText().toString(),
				"mailbox="+mailboxEdit.getText().toString()};
		String ret="Network Failed.";
		
		try{
			ret=SyncHttp.httpGet(url, params);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return ret;
	}
	
	private class RegisterAsyncTask extends AsyncTask<String,Integer,String>{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String ret=register(params[0]);
			return ret;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			registerProBar.setVisibility(ProgressBar.GONE);
			Toast.makeText(RegisterActivity.this, result, 500).show();
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			registerProBar.setVisibility(ProgressBar.VISIBLE);
			Toast.makeText(RegisterActivity.this, "Submitting...", 500).show();
		}
		
	}
	
}
