package johnsonyue.newscapturerbeta.dao;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public void capture(int category, String rss){
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
		
		helper=new DBHelper();
		Connection conn=helper.getConnection();
		String lastDate="";
		String sql3="select * from news where category="+category+" order by date desc limit 1";
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
			String date=parseDate(item.getDate());
			if(date.compareTo(lastDate)<=0){
				continue;
			}
			
			t.put("title", item.getTitle());
			t.put("category", category);
			t.put("date",date);
			t.put("source", item.getAuthor());
			t.put("body", parseHtml(item.getLink()));
			t.put("link",item.getLink());
			
			newsList.add(t);
		}
		
		update(newsList);
	}
	
	public String captureNews(){

		String[] rss={
				"http://rss.sina.com.cn/news/china/focus15.xml",
				"http://rss.sina.com.cn/news/world/focus15.xml",
				"http://rss.sina.com.cn/roll/sports/hot_roll.xml",
				"http://rss.sina.com.cn/ent/hot_roll.xml",
				"http://rss.sina.com.cn/games/djyx.xml",
				"http://rss.sina.com.cn/news/allnews/tech.xml",
				"http://rss.sina.com.cn/roll/finance/hot_roll.xml",
				"http://rss.sina.com.cn/roll/mil/hot_roll.xml"
		};
		
		for(int i=0;i<rss.length;i++){
			capture(i+1,rss[i]);
		}
		
		return msg;
	}
	
	private String parseHtml(String url){
		String text="";
		
		Document doc = null;
		try {
			doc = Jsoup.connect(url).timeout(5000).get();
		} catch (Exception e) {
			e.printStackTrace();
			return "Error.";
		}
		Elements article=doc.select("div[id=artibody] > p");
		
		if(article==null){
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
		
		for(Map<String, Object> t : newsList){
			String sql="set names \"utf8\"";
			String body=(String) t.get("body");
			if(body==null||body.equals("")){
				continue;
			}
			String sql2="insert into news value(null,"+t.get("category")+",\""+t.get("title")+"\","+"\"china\",\""
						+t.get("date")+"\",\""+t.get("source")+"\",\""+body+"\",\"null\")";
			
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
	
	private String parseDate(String date){
		String result="";
		String[] months={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
		Map<String,String> monthsMap=new HashMap<String,String>();
		for(int i=0;i<months.length;i++){
			monthsMap.put(months[i],""+(i+1));
		}
		
		String[] t=date.split("[ ]");
		result+=t[3]+"-"+monthsMap.get(t[2])+"-"+t[1]+" "+t[4];
		
		return result;
	}
}
