package johnsonyue.newscapturerbeta.dao;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;

import org.gnu.stealthp.rsslib.RSSChannel;
import org.gnu.stealthp.rsslib.RSSHandler;
import org.gnu.stealthp.rsslib.RSSItem;
import org.gnu.stealthp.rsslib.RSSParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class NewsDAO {
	
	private DBHelper helper;
	private String msg="done.";
	private int count=0;
	private int failed=0;
	
	public int getCount(){
		return count;
	}
	
	public int getFailed(){
		return failed;
	}
	
	public void capture(int category, String rss, String portal){
		ArrayList<Map<String, Object>> newsList=new ArrayList<Map<String, Object>>();
		
		RSSParser parser=new RSSParser();
		RSSHandler handler=new RSSHandler();
		parser.setHandler(handler);
		try{
			URL url=new URL(rss);
			parser.setXmlResource(url);
			parser.parse();
		}catch(Exception e){
			e.printStackTrace();
			msg+=e.getMessage();
			return;
		}
		
		RSSChannel channel=handler.getRSSChannel();
		List<RSSItem> list=channel.getItems();
		System.out.println("rss parsed, listsize=="+list.size());
		
		helper=new DBHelper();
		Connection conn=helper.getConnection();
		String lastDate="";
		String sql3="select * from news where category="+category+" and portal=\""+portal+"\""+" order by date desc limit 1";
		try{
			PreparedStatement ps3=conn.prepareStatement(sql3);
			ResultSet rs3=ps3.executeQuery();
			rs3.next();
			lastDate=rs3.getString("date");
		}catch(Exception e){
			e.printStackTrace();
		}
		
		for(RSSItem item : list){
			Map<String, Object> t = new HashMap<String, Object>();
			String date="";
			date=parseDate(item.getDate(),portal);
			
			if(date.compareTo(lastDate)<=0){
				continue;
			}
			
			String author=item.getAuthor();
			if(author==null||author.equals("null")){
				author=portal;
			}
			
			t.put("title", item.getTitle());
			t.put("category", category);
			t.put("date",date);
			t.put("source", author);
			t.put("body", parseHtml(item.getLink(),portal));
			t.put("portal",portal);
			
			newsList.add(t);
			System.out.println("News: "+item.getTitle()+" added.");
		}
		
		update(newsList);
	}
	
	public String captureNews(){
		String[] portals={};
		try{
			PropertyResourceBundle bundle = new PropertyResourceBundle(NewsDAO.class
				.getResourceAsStream("DBConfig.properties"));
			String t=bundle.getString("portals");
			portals=t.split("[|]");
		}catch(Exception e){
			e.printStackTrace();
		}
		
		for(int i=0;i<portals.length;i++){
			String[] rss={};
			try{
				PropertyResourceBundle bundle = new PropertyResourceBundle(NewsDAO.class
					.getResourceAsStream("DBConfig.properties"));
				String t=bundle.getString(portals[i]);
				rss=t.split("[|]");
			}catch(Exception e){
				e.printStackTrace();
			}
			System.out.println(portals[i]+":");
			for(int j=0;j<rss.length;j++){
				System.out.println("\t"+rss[j]);
				capture(j+1,rss[j],portals[i]);
			}
		}
		return msg;
	}
	
	private String parseHtml(String url, String portal){
		String text="";
		
		Document doc = null;
		try {
			doc = Jsoup.connect(url).timeout(5000).get();
		} catch (Exception e) {
			e.printStackTrace();
			failed++;
			return "";
		}
		
		Elements article=null;
		if(portal.equals("sina")){
			article=doc.select("div[id=artibody] > p");
		}else if(portal.equals("netease")){
			article=doc.select("div[id=endText] > p");
		}else if(portal.equals("baidu")){
			article=doc.select("div[id=p_content] > p");
		}else if(portal.equals("souhu")){
			article=doc.select("div[itemprop=articleBody] > p");
		}
		
		if(article==null){
			failed++;
			return "";
		}
		for(Element t : article){
			text+=t.text()+"\n";
		}
		
		return text;
	}
	
	private void update(ArrayList<Map<String, Object>> newsList){
		helper=new DBHelper();
		Connection conn=helper.getConnection();
		
		String[] snapShots={};
		try{
			PropertyResourceBundle bundle = new PropertyResourceBundle(NewsDAO.class
				.getResourceAsStream("DBConfig.properties"));
			String t=bundle.getString("categories");
			snapShots=t.split("[|]");
		}catch(Exception e){
			e.printStackTrace();
		}
		
		for(Map<String, Object> t : newsList){
			String sql="set names \"utf8\"";
			String body=(String) t.get("body");
			if(body==null||body.equals("")){
				continue;
			}
			String snapShot=snapShots[(Integer) t.get("category")-1];
			String sql2="insert into news value(null,"+t.get("category")+",\""+t.get("title")+"\","+"\""+snapShot+"\",\""
						+t.get("date")+"\",\""+t.get("source")+"\",\""+body+"\",\"null\","+"\""+t.get("portal")+"\","+"\"-1\")";
			System.out.println("News: "+t.get("title")+" inserted.");
			count++;
			try {
				PreparedStatement ps=conn.prepareStatement(sql);
				ps.execute();
				PreparedStatement ps2=conn.prepareStatement(sql2);
				ps2.execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		helper.closeDB();
	}
	
	private String parseDate(String date, String portal){
		String result="";
		if(portal.equals("sina")||portal.equals("netease")){
			String[] months={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
			Map<String,String> monthsMap=new HashMap<String,String>();
			for(int i=0;i<months.length;i++){
				monthsMap.put(months[i],""+(i+1));
			}
		
			String[] t=date.split("[ ]");
			String month=String.format("%02d", Integer.parseInt(monthsMap.get(t[2])));
			String day=String.format("%02d", Integer.parseInt(t[1]));
			result+=t[3]+"-"+month+"-"+day+" "+t[4];
		}
		else if(portal.equals("baidu")){
			String[] t=date.split("[A-Z]");
			String[] ymd=t[0].split("[-]");
			String[] hms=t[1].split("[.]");
			String month=String.format("%02d", Integer.parseInt(ymd[1]));
			String day=String.format("%02d", Integer.parseInt(ymd[2]));
			result+=ymd[0]+"-"+month+"-"+day+" "+hms[0];
		}
		else if(portal.equals("souhu")){
			String[] months={"一月","二月","三月","四月","五月","六月","七月","八月","九月","十月","十一月","十二月"};
			Map<String,String> monthsMap=new HashMap<String,String>();
			for(int i=0;i<months.length;i++){
				monthsMap.put(months[i],""+(i+1));
			}
			
			String[] t=date.split("[ ]");
			String month=String.format("%02d", Integer.parseInt(monthsMap.get(t[2])));
			String day=String.format("%02d", Integer.parseInt(t[1]));
			result+=t[3]+"-"+month+"-"+day+" "+t[4];
		}
		return result;
	}
}
