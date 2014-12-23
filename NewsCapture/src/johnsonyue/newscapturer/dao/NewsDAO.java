package johnsonyue.newscapturer.dao;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
	
	private static final int CATEGORY_NUM=8;
	private static final double SIMILARITY_BOUNDARY=0.5;
	
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
			msg+=e.getMessage()+","+rss+"\n";
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
			//e.printStackTrace();
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
			//e.printStackTrace();
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
	
	public double calcSimilarity(String str1,String str2){
		Map<String,int[]> vectorSpace=new HashMap<String,int[]>();
		//System.out.println(str1+"\n"+str2);
		
		char[] terms=str1.toCharArray();
		int[] termNum;
		for(int i=0;i<terms.length;i++){
			if(vectorSpace.containsKey(String.valueOf(terms[i]))){
				vectorSpace.get(String.valueOf(terms[i]))[0]++;
			}
			else{
				termNum=new int[]{1,0};
				vectorSpace.put(String.valueOf(terms[i]),  termNum);
			}
		}
		
		terms=str2.toCharArray();
		for(int i=0;i<terms.length;i++){
			if(vectorSpace.containsKey(String.valueOf(terms[i]))){
				vectorSpace.get(String.valueOf(terms[i]))[1]++;
			}
			else{
				termNum=new int[]{0,1};
				vectorSpace.put(String.valueOf(terms[i]),  termNum);
			}
		}
		
		double str1Module=0,str2Module=0,product=0;
		Iterator iter=vectorSpace.entrySet().iterator();
		
		while(iter.hasNext()){
			Map.Entry entry=(Map.Entry)iter.next();
			termNum=(int[]) entry.getValue();
			//System.out.println(entry.getKey()+" "+termNum[0]+","+termNum[1]);
			str1Module+=termNum[0]*termNum[0];
			str2Module+=termNum[1]*termNum[1];
			product+=termNum[0]*termNum[1];
		}
		str1Module=Math.sqrt(str1Module);
		str2Module=Math.sqrt(str2Module);
		
		return product/(str1Module*str2Module);
	}
	
	public void updateRelated(){
		helper=new DBHelper();
		Connection conn=helper.getConnection();
		
		ArrayList<Map<String,String>> relatedList;
		for(int i=0;i<CATEGORY_NUM;i++){
			//Initialization.
			String sql="select id,title from news where category="+(i+1);
			System.out.println(sql);
			relatedList=new ArrayList<Map<String,String>>();
			try{
				PreparedStatement ps=conn.prepareStatement(sql);
				ResultSet rs=ps.executeQuery();
				while(rs.next()){
					Map<String,String> t=new HashMap<String,String>();
					t.put("id", rs.getString("id"));
					t.put("title", rs.getString("title"));
					t.put("related","-1");
					relatedList.add(t);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			
			//Calculate similarity.
			for(int j=0;j<relatedList.size();j++){
				for(int k=j+1;k<relatedList.size();k++){
					double s=calcSimilarity(relatedList.get(j).get("title"),relatedList.get(k).get("title"));
					if(s>=SIMILARITY_BOUNDARY){
						String r1=relatedList.get(j).get("related");
						String r2=relatedList.get(k).get("related");
						
						r1+=","+relatedList.get(k).get("id");
						relatedList.get(j).put("related", r1);
						
						r2+=","+relatedList.get(j).get("id");
						relatedList.get(k).put("related", r2);
					}
				}
			}
			
			//Update to dataBase.
			for(int n=0;n<relatedList.size();n++){
				String id=relatedList.get(n).get("id");
				String related=relatedList.get(n).get("related");
				if(related.equals("-1")){
					continue;
				}
				
				String sql2="update news set related='"+related+"' where id="+id;
				System.out.println(sql2);
				try{
					PreparedStatement ps2=conn.prepareStatement(sql2);
					ps2.execute();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
		helper.closeDB();
	}
}
