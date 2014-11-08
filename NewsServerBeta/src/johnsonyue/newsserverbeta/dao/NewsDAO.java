package johnsonyue.newsserverbeta.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;


public class NewsDAO {
	
	private DBHelper helper;
	
	public ArrayList<HashMap<String,Object>> getNewsList(){
		helper=new DBHelper();
		Connection conn=helper.getConnection();
		ArrayList<HashMap<String,Object>> news=new ArrayList<HashMap<String,Object>>();
		
		String sql="select * from news";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs=ps.executeQuery();
			while(rs.next()){
				HashMap<String,Object> t=new HashMap<String,Object>();
				t.put("id",Integer.parseInt(rs.getString("id")));
				t.put("category",Integer.parseInt(rs.getString("category")));
				t.put("title",rs.getString("title"));
				t.put("snap_shot",rs.getString("snap_shot"));
				t.put("date",rs.getString("date"));
				t.put("source",rs.getString("source"));
				t.put("body",rs.getString("body"));
				t.put("pic_url",rs.getString("pic_url"));
				news.add(t);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		helper.closeDB();
		return news;
	}
	
	public ArrayList<HashMap<String,Object>> getSpecCatNews(String category){
		helper=new DBHelper();
		Connection conn=helper.getConnection();
		ArrayList<HashMap<String,Object>> news=new ArrayList<HashMap<String,Object>>();
		
		String sql="select * from news where category="+category;
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs=ps.executeQuery();
			while(rs.next()){
				HashMap<String,Object> t=new HashMap<String,Object>();
				t.put("id",Integer.parseInt(rs.getString("id")));
				t.put("category",Integer.parseInt(rs.getString("category")));
				t.put("title",rs.getString("title"));
				t.put("snap_shot",rs.getString("snap_shot"));
				t.put("date",rs.getString("date"));
				t.put("source",rs.getString("source"));
				t.put("body",rs.getString("body"));
				t.put("pic_url",rs.getString("pic_url"));
				news.add(t);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		helper.closeDB();
		
		return news;
	}
	
	public String register(String nickName, String password, String mailbox){
		DBHelper helper=new DBHelper();
		Connection conn=helper.getConnection();
		
		String sql="select * from users "
				+ "where name=\""+nickName+"\" or mailbox=\""+mailbox+"\"";
		String ret="MySql error.";
		try {
			PreparedStatement ps=conn.prepareStatement(sql);
			ResultSet rs=ps.executeQuery();
			if(rs.next()){
				ret= "User Already Exists.";
			}
			else{
				String sql2="insert into users value"
						+ "(null,"+"\""+nickName+"\",\"null\","+"\""+password+"\","+"\""+mailbox+"\")";
				PreparedStatement ps2=conn.prepareStatement(sql2);
				ps2.execute();
				ret= "Successfully Registered";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		helper.closeDB();
		return ret;
	}
	
	public String login(String nickName,String password){
		DBHelper helper=new DBHelper();
		Connection conn=helper.getConnection();
		
		String sql="select * from users "
				+ "where name=\""+nickName+"\" and password=\""+password+"\"";
		String ret="-1";
		try {
			PreparedStatement ps=conn.prepareStatement(sql);
			ResultSet rs=ps.executeQuery();
			if(rs.next()){
				ret= rs.getString("id");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		helper.closeDB();
		return ret;
	}
	
	public String comment(String user_id, String news_id, String date, String comment){
		DBHelper helper=new DBHelper();
		Connection conn=helper.getConnection();
		String sql="insert into comments value(null,"+"\""
				+user_id+"\",\""+news_id+"\",\""+date+"\",\""+comment+"\",0,0)";
		String ret="-1";
		try {
			PreparedStatement ps=conn.prepareStatement(sql);
			ps.execute();
			ret="0";
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		helper.closeDB();
		return ret;
	}
	
	public ArrayList<HashMap<String, Object>> getComments(String newsId){
		helper=new DBHelper();
		Connection conn=helper.getConnection();
		ArrayList<HashMap<String,Object>> comments=new ArrayList<HashMap<String,Object>>();
		
		String sql="select * from comments where news_id="+newsId;
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs=ps.executeQuery();
			while(rs.next()){
				HashMap<String,Object> t=new HashMap<String,Object>();
				
				String sql2="select * from users where id="+rs.getInt("user_id");
				PreparedStatement ps2 = conn.prepareStatement(sql2);
				ResultSet rs2=ps2.executeQuery();
				String name="unknown";
				if(rs2.next()){
					name=rs2.getString("name");
				}
				
				t.put("name",name);
				t.put("comment",rs.getString("comment"));
				t.put("date",rs.getString("date"));
				t.put("liked",rs.getInt("liked"));
				t.put("disliked",rs.getInt("disliked"));
				comments.add(t);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		helper.closeDB();
		
		return comments;
	}
	
	public ArrayList<HashMap<String,Object>> getSearchResult(String keyword){
		helper=new DBHelper();
		Connection conn=helper.getConnection();
		ArrayList<HashMap<String,Object>> result=new ArrayList<HashMap<String,Object>>();
		
		String sql="select * from news where title like "+"\"%"+keyword+"%\"";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs=ps.executeQuery();
			while(rs.next()){
				HashMap<String,Object> t=new HashMap<String,Object>();
				t.put("id",Integer.parseInt(rs.getString("id")));
				t.put("category",Integer.parseInt(rs.getString("category")));
				t.put("title",rs.getString("title"));
				t.put("snap_shot",rs.getString("snap_shot"));
				t.put("date",rs.getString("date"));
				t.put("source",rs.getString("source"));
				t.put("body",rs.getString("body"));
				t.put("pic_url",rs.getString("pic_url"));
				result.add(t);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		helper.closeDB();
		return result;
	}
}
