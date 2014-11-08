package johnsonyue.newscapturerbeta.dao;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
	
	public String captureNews(){
		ArrayList<Map<String, Object>> newsList=new ArrayList<Map<String, Object>>();
		
		String rss="http://rss.sina.com.cn/roll/sports/hot_roll.xml";
		
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
			return msg;
		}
		
		RSSChannel channel=handler.getRSSChannel();
		List<RSSItem> list=channel.getItems();
		for(RSSItem item : list){
			Map<String, Object> t = new HashMap<String, Object>();
			
			t.put("title",item.getTitle());
			t.put("date",item.getDate());
			t.put("source", item.getAuthor());
			t.put("body", parseHtml(item.getLink()));
			//t.put("body", item.getLink());
			
			newsList.add(t);
		}
		
		update(newsList);
		
		return msg;
	}
	
	private String parseHtml(String url){
		String text="";
		
		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (Exception e) {
			e.printStackTrace();
			return "Error.";
		}
		Elements article=doc.select("div[id=artibody]");
		for(Element t : article){
			text+=t.text();
		}
		
		return text;
	}
	
	private void update(ArrayList<Map<String, Object>> newsList){
		helper=new DBHelper();
		Connection conn=helper.getConnection();
		
		for(Map<String, Object> t : newsList){
			String sql="set names \"utf8\"";
			String sql2="insert into news value(null,1,\""+t.get("title")+"\","+"\"china\",\""
						+t.get("date")+"\",\""+t.get("source")+"\",\""+t.get("body")+"\",\"null\")";
			msg+="\nsql: "+sql2;
			try {
				PreparedStatement ps=conn.prepareStatement(sql);
				ps.execute();
				PreparedStatement ps2=conn.prepareStatement(sql2);
				ps2.execute();
			} catch (Exception e) {
				e.printStackTrace();
				msg+=e.getMessage();
			}
		}
		
		helper.closeDB();
	}
}
