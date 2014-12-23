package johnsonyue.project.newsclient;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "NEWS_DB";
	private static final String TABLE_NAME = "FAV_TABLE";
	private static final int DB_VERSION = 2;
	private static final String CREATE_TABLE="CREATE TABLE "+TABLE_NAME+"(id int,category int,title text,snap_shot text,date text,source text,body text,pic_url text,portal text,related text)";
	public static final String KEY_NEWS_ID="id";
	public static final String KEY_TITLE ="title";
	public static final String KEY_DATE ="date";
	public static final String KEY_SNAPSHOT ="snap_shot";
	public static final String KEY_NEWS_SOURCE ="source";
	public static final String KEY_BODY ="body";
	public static final String KEY_RELATED ="related";
	
	private SQLiteDatabase mDataBase=null;
	
	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		mDataBase=db;
		mDataBase.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
	public void open(){
		mDataBase=this.getWritableDatabase();
	}
	
	public void close(){
		if(mDataBase!=null){
			mDataBase.close();
		}
	}
	
	public void insert(ContentValues values){
		mDataBase.insert(TABLE_NAME, null, values);
	}
	
	public void delete(int newsId){
		mDataBase.delete(TABLE_NAME,KEY_NEWS_ID+"="+newsId,null);
	}
	
	public boolean isAdded(int newsId){
		Cursor c=mDataBase.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE id="+newsId,null);
		if(c.getCount()==0){
			return false;
		}
		return true;
	}
	
	public Cursor query(){
		Cursor c=mDataBase.rawQuery("SELECT id as _id, * FROM "+TABLE_NAME,null);
		return c;
	}
}
