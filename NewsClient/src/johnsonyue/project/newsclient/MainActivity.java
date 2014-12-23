package johnsonyue.project.newsclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import me.maxwin.view.XListView;
import me.maxwin.view.XListView.IXListViewListener;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author JohnsonYue
 *
 */
public class MainActivity extends Activity implements IXListViewListener {
	//Constants and globals.
	private String site;
	public static final int NEWS_LIST=1;
	public static final int FAVOURITE_LIST=2;
	private static final int FETCH_AMOUNT=3;
	public static final int GREEN_THEME_ID=1;
	public static final int DARK_THEME_ID=2;
	
	private int id;//Theme id.
	private int cId=-1;
	private boolean useMainProBar;
	private String[] categories=new String[]{"C1","C2","C3","C4","C5","C6","C7","C8"};
	
	//Use viewPager, use buttons as tabs.
	private ViewPager viewPager;
	private List<View> viewList;
	private int[] btnIds=new int[]{R.id.btn_tab1,R.id.btn_tab2,R.id.btn_tab3,R.id.btn_tab4};
	private int[] btnStyle=new int[]{R.drawable.text,R.drawable.favourite,R.drawable.settings,R.drawable.about};
	private int[] btnPressed=new int[]{R.drawable.text_pressed,R.drawable.favourite_pressed,R.drawable.settings_pressed,R.drawable.about_pressed};
	private Button[] btnTabs=new Button[btnStyle.length];
	
	//View members.
	private Button SearchButton;
	private Button confirmButton;
	private RadioGroup group;
	private RadioButton darkThemeBtn;
	private RadioButton greenThemeBtn;
	private LinearLayout CategoryLayout;
	private XListView NewsList;
	private ProgressBar mainProBar;
	private ListView favouriteList;
	
	//Data set and adapter.
	private SimpleAdapter adapter;
	private ArrayList<Map<String,Object>> NewsTest=new ArrayList<Map<String,Object>>();
	private ArrayList<Map<String,Object>> CategoryTest=new ArrayList<Map<String,Object>>();
	private ArrayList<Map<String,Object>> temp=new ArrayList<Map<String,Object>>();
	private DBHelper mDBHelper;
	private Cursor mCursor;
	SimpleCursorAdapter cursorAdapter;
	
	//AsyncTasks.
	private LoadNewsListAsyncTask asyncTask;
	private getMoreNewsAsyncTask getMoreTask;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Set to no title full screen.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        SharedPreferences preferences2=getSharedPreferences("theme",Context.MODE_PRIVATE);
        id=preferences2.getInt("theme_id", GREEN_THEME_ID);
        Log.v("dbg","theme_id: "+id);
        if(id==DARK_THEME_ID){
            setTheme(R.style.DarkTheme);
        }
        else{
        	setTheme(R.style.GreenTheme);
        }
        setContentView(R.layout.main_pager);

        //Load configurations.
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
        
        //Initialize four views of viewPager.
        LayoutInflater inflater=this.getLayoutInflater();
        
        //Initialize mainView.
        View mainView=inflater.inflate(R.layout.activity_main, null);

        mDBHelper=new DBHelper(this);
        mDBHelper.open();
        
        mainProBar=(ProgressBar)mainView.findViewById(R.id.main_pro_bar);
        mainProBar.setVisibility(ProgressBar.GONE);
        
        SearchButton=(Button)mainView.findViewById(R.id.search_button);
        SearchButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i=new Intent(MainActivity.this,SearchActivity.class);
				startActivity(i);
			}});
        
        //Initialize the category bar of mainView.
        fillCategoryTest();
        String[] from1=new String[]{"category"};
        int[] to1=new int[]{R.id.category_title};
        SimpleAdapter adapter1=new SimpleAdapter(this,CategoryTest,R.layout.category_title_layout,from1,to1);
        final GridView gridView=new GridView(this);
        
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int columnWidth=dm.widthPixels/4;
        gridView.setColumnWidth(columnWidth);
        gridView.setNumColumns(GridView.AUTO_FIT);
        gridView.setGravity(Gravity.CENTER);
        gridView.setSelector(R.drawable.category_bar_selector);
        LayoutParams params = new LayoutParams(columnWidth*8, LayoutParams.MATCH_PARENT);
		gridView.setLayoutParams(params);
        gridView.setAdapter(adapter1);
        CategoryLayout=(LinearLayout)mainView.findViewById(R.id.category_layout);
        CategoryLayout.addView(gridView);
        
        gridView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) {
				// TODO Auto-generated
				TextView text;
				if(id==GREEN_THEME_ID){
					for(int i=0;i<gridView.getChildCount();i++){
						text=(TextView) gridView.getChildAt(i);
						text.setBackgroundResource(R.drawable.category_bar_background);
						text.setTextAppearance(MainActivity.this, R.style.titlebar_title_style);
					}
				
					text=(TextView) gridView.getChildAt(position);
					text.setBackgroundResource(R.drawable.focused_shape);
					text.setTextAppearance(MainActivity.this, R.style.category_title_style);
				}
				else{
					for(int i=0;i<gridView.getChildCount();i++){
						text=(TextView) gridView.getChildAt(i);
						text.setBackgroundResource(R.drawable.category_bar_background_dark);
						text.setTextAppearance(MainActivity.this, R.style.titlebar_title_style);
					}
				
					text=(TextView) gridView.getChildAt(position);
					text.setBackgroundResource(R.drawable.focused_shape);
					text.setTextAppearance(MainActivity.this, R.style.category_title_style);
				}
				
				String date="";
				boolean isCategoryChanged;
				if(cId==(position+1)){
					isCategoryChanged=false;
					if(!NewsTest.isEmpty()){
						date=(String) NewsTest.get(0).get("date");
					}
				}
				else{
					isCategoryChanged=true;
					cId=position+1;
				}
				
				useMainProBar=true;
				asyncTask=new LoadNewsListAsyncTask();
				asyncTask.execute(cId,date,FETCH_AMOUNT,isCategoryChanged);
			}});
        
        //Initialize News List of MainView.
        NewsList=(XListView)mainView.findViewById(R.id.news_list);
        NewsList.setPullRefreshEnable(true);
        NewsList.setPullLoadEnable(true);
        NewsList.setXListViewListener(this);
        NewsList.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				// TODO Auto-generated method sub
				Intent i = new Intent(MainActivity.this,NewsDetailActivity.class);
				Bundle bundle=new Bundle();
				
				TextView title=(TextView)view.findViewById(R.id.title);
				TextView snapShot=(TextView)view.findViewById(R.id.snap_shot);
				TextView newsSource=(TextView)view.findViewById(R.id.news_source);
				TextView date=(TextView)view.findViewById(R.id.date);
				bundle.putInt("news_id",(Integer) NewsTest.get(position-1).get("news_id"));
				bundle.putString("title", title.getText().toString());
				bundle.putString("snapShot", snapShot.getText().toString());
				bundle.putString("newsSource", newsSource.getText().toString());
				bundle.putString("date", date.getText().toString());
				bundle.putString("body",(String)NewsTest.get(position-1).get("body"));
				bundle.putString("related", (String)NewsTest.get(position-1).get("related"));
				
				i.putExtras(bundle);
				startActivityForResult(i,NEWS_LIST);
			}
        });

        //Initialize settingsView of main pager.
        View settingsLayoutView=inflater.inflate(R.layout.settings_layout, null);
        group=(RadioGroup)settingsLayoutView.findViewById(R.id.radio_group);
        darkThemeBtn=(RadioButton)settingsLayoutView.findViewById(R.id.dark_theme_btn);
        greenThemeBtn=(RadioButton)settingsLayoutView.findViewById(R.id.green_theme_btn);
        confirmButton=(Button)settingsLayoutView.findViewById(R.id.confirm_btn);
        confirmButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int id=GREEN_THEME_ID;
				if(darkThemeBtn.getId()==group.getCheckedRadioButtonId()){
					id=DARK_THEME_ID;
				}
				else if(greenThemeBtn.getId()==group.getCheckedRadioButtonId()){
					id=GREEN_THEME_ID;
				}
				else{
					Toast.makeText(MainActivity.this, "Select one theme.", Toast.LENGTH_SHORT).show();
					return;
				}
				SharedPreferences preferences=getSharedPreferences("theme",Context.MODE_PRIVATE);
				Editor editor=preferences.edit();
				editor.putInt("theme_id", id);
				editor.commit();
				Log.v("dbg","Putted theme_id: "+id);
				Intent i=new Intent();
		        i.setClass(MainActivity.this, MainActivity.this.getClass());
		        MainActivity.this.startActivity(i);
		        MainActivity.this.finish();
			}});
        
        //Initialize favouriteView of main pager.
        View favouriteLayoutView=inflater.inflate(R.layout.favourite_layout, null);
        favouriteList=(ListView)favouriteLayoutView.findViewById(R.id.favourite_list);
        favouriteList.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				Intent i = new Intent(MainActivity.this,NewsDetailActivity.class);
				Bundle bundle=new Bundle();
				
				Cursor cur=mCursor;
				cur.moveToPosition(position);
				int id=cur.getColumnIndex(DBHelper.KEY_NEWS_ID);
				int  title=cur.getColumnIndex(DBHelper.KEY_TITLE);
				int  date=cur.getColumnIndex(DBHelper.KEY_DATE);
				int  snapShot=cur.getColumnIndex(DBHelper.KEY_SNAPSHOT);
				int  source=cur.getColumnIndex(DBHelper.KEY_NEWS_SOURCE);
				int  body=cur.getColumnIndex(DBHelper.KEY_BODY);
				int related=cur.getColumnIndex(DBHelper.KEY_RELATED);
				
				bundle.putInt("news_id",(Integer)cur.getInt(id));
				bundle.putString("title", cur.getString(title));
				bundle.putString("snapShot", cur.getString(snapShot));
				bundle.putString("newsSource", cur.getString(source));
				bundle.putString("date", cur.getString(date));
				bundle.putString("body", cur.getString(body));
				bundle.putString("related", cur.getString(related));
				
				i.putExtras(bundle);
				startActivityForResult(i,FAVOURITE_LIST);
			}});
        
        //Initialize aboutView of main pager.
        View aboutLayoutView=inflater.inflate(R.layout.about_layout, null);
        
        //Initialize tabs of main pager.
        for(int i=0;i<btnStyle.length;i++){
        	btnTabs[i]=(Button)findViewById(btnIds[i]);
        	btnTabs[i].setFocusable(true);
        	btnTabs[i].setBackgroundResource(btnStyle[i]);
        	btnTabs[i].setOnClickListener(new MyTabListener(i));
        }
        
        viewPager=(ViewPager)findViewById(R.id.main_pager);
        viewList=new ArrayList<View>();
        viewList.add(mainView);
        viewList.add(favouriteLayoutView);
        viewList.add(settingsLayoutView);
        viewList.add(aboutLayoutView);
        viewPager.setAdapter(new MyPagerAdapter(viewList));
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        viewPager.setCurrentItem(0);
        btnTabs[0].setBackgroundResource(btnStyle[0]);
    }
    
    private void fillCategoryTest(){
    	String[] categoryArray = getResources().getStringArray(R.array.categories);
    	for(int i=0;i<categories.length;i++){
    		HashMap<String,Object> t=new HashMap<String, Object>();
    		t.put("category", categoryArray[i]);
    		CategoryTest.add(t);
    	}
    }
    
    /**
     * @description get news whose date is newer than parameter date.  
     * @param cId current category.
     * @param date newest date of the list.
     * @param limit most items at a time.
     */
    private void fillListTest(int cId,String date, int limit){
    	temp.clear();
    	temp.addAll(NewsTest);
    	String resultStr=null;
    	String url=site+"/getNewerNews";
    	String[] params={"category="+cId,"date="+date,"limit="+limit};
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
    	    		t.put("related",newsObject.get("related"));
    	    		temp.add(i,t);
    			}
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    /**
     * @description The same as fillListTest() except refillListTest() clears the list instead of add items to the list.
     * @param cId current category.
     * @param date newest date of the list.
     * @param limit most items at a time.
     */
    private void refillListTest(int cId,String date, int limit){
    	temp.clear();
    	String resultStr=null;
    	String url=site+"/getLatestNews";
    	String[] params={"category="+cId,"date="+date,"limit="+limit};
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
    	    		t.put("related",newsObject.get("related"));
    	    		temp.add(i,t);
    			}
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    }
    
    /**
     * @description get news whose date is older than parameter date.  
     * @param cId current category.
     * @param date oldest date of the list.
     * @param limit most items at a time.
     */
    public void addMoreToList(int cId,String date, int limit){
    	temp.clear();
    	temp.addAll(NewsTest);
    	String resultStr=null;
    	String url=site+"/getMoreNews";
    	String[] params={"category="+cId,"date="+date,"limit="+limit};
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
    	    		t.put("related",newsObject.get("related"));
    	    		temp.add(t);
    			}
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    //AsyncTask inner class.
    private class LoadNewsListAsyncTask extends AsyncTask<Object, Integer, Integer>{
		@Override
		protected Integer doInBackground(Object... params) {
			// TODO Auto-generated method stub
			boolean isCategoryChanged=(Boolean)params[3];
			if(!isCategoryChanged){
				fillListTest((Integer)params[0],(String)params[1],(Integer)params[2]);
			}
			else{
				refillListTest((Integer)params[0],(String)params[1],(Integer)params[2]);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			String[] from=new String[]{"pic","title","snapShot","date","newsSource"};
	        int[] to=new int[]{R.id.pic,R.id.title,R.id.snap_shot,R.id.date,R.id.news_source};
	        NewsTest.clear();
	        NewsTest.addAll(temp);
	        adapter=new SimpleAdapter(MainActivity.this, NewsTest, R.layout.item_layout,from,to);
	        NewsList.setAdapter(adapter);
	        mainProBar.setVisibility(ProgressBar.GONE);

			NewsList.stopRefresh();
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			if(useMainProBar){
				mainProBar.setVisibility(ProgressBar.VISIBLE);
			}
			//Toast.makeText(MainActivity.this, "Fetching news list ...", 500).show();
		}

    }
    
    private class getMoreNewsAsyncTask extends AsyncTask<Object, Integer, Integer>{

		@Override
		protected Integer doInBackground(Object... params) {
			// TODO Auto-generated method stub
			addMoreToList((Integer)params[0],(String)params[1],(Integer)params[2]);
			Log.v("dbg","size=="+NewsTest.size());
			return null;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			NewsTest.clear();
			NewsTest.addAll(temp);
			adapter.notifyDataSetChanged();
			NewsList.stopLoadMore();
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
    	
    }
    
    //FavouriteView.
    private void fillFavList(){
    	mCursor=mDBHelper.query();
    	String[] from=new String[]{DBHelper.KEY_TITLE,DBHelper.KEY_SNAPSHOT,DBHelper.KEY_DATE,DBHelper.KEY_NEWS_SOURCE};
    	int[] to=new int[]{R.id.fav_title,R.id.fav_snap_shot,R.id.fav_date,R.id.fav_news_source};
    	
    	cursorAdapter=new SimpleCursorAdapter(MainActivity.this, R.layout.favourite_item, mCursor, from, to);
    	favouriteList.setAdapter(cursorAdapter);
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
			case NEWS_LIST:
				break;
			case FAVOURITE_LIST:
				fillFavList();
				break;
		}
	}
	
	//Inner class for pager. PagerAdapter, TabListener, PageChangeListener.
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
    
    private class MyTabListener implements OnClickListener{
    	int index=-1;
    	
    	public MyTabListener(int i){
    		index=i;
    	}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			viewPager.setCurrentItem(index);
			for(int i=0;i<btnTabs.length;i++){
				if(i==index){
					btnTabs[i].setBackgroundResource(btnPressed[i]);
				}
				else{
					btnTabs[i].setBackgroundResource(btnStyle[i]);
				}
			}
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
			for(int i=0;i<btnTabs.length;i++){
				if(i==arg0){
					btnTabs[i].setBackgroundResource(btnPressed[i]);
				}
				else{
					btnTabs[i].setBackgroundResource(btnStyle[i]);
				}
			}
			
			if(arg0==1){
				fillFavList();
			}
		}
    }
    
    //Overwrite two functions of XListView.
    @Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		//Toast.makeText(MainActivity.this, "Refreshing...", Toast.LENGTH_SHORT).show();
		String date="";
		if(!NewsTest.isEmpty()){
			date=(String) NewsTest.get(0).get("date");
		}
		
		useMainProBar=false;
		asyncTask=new LoadNewsListAsyncTask();
		asyncTask.execute(cId,date,FETCH_AMOUNT,false);
	}

	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		//Toast.makeText(MainActivity.this, "Fetching More...", Toast.LENGTH_SHORT).show();
		String date="";
		if(!NewsTest.isEmpty()){
			date=(String) NewsTest.get(NewsTest.size()-1).get("date");
		}
		getMoreTask=new getMoreNewsAsyncTask();
		getMoreTask.execute(cId,date,FETCH_AMOUNT);
	}
}
