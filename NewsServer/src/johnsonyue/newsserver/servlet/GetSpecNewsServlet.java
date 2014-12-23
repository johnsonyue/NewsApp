package johnsonyue.newsserver.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import johnsonyue.newsserver.dao.NewsDAO;

public class GetSpecNewsServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1563659380794493051L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		resp.setContentType("text/html;charset=UTF-8");
		String category=req.getParameter("category");
		NewsDAO newsDAO=new NewsDAO();
		ArrayList<HashMap<String,Object>> newsList=newsDAO.getSpecCatNews(category);
		
		PrintWriter out = resp.getWriter();
		JSONObject jObject=new JSONObject();
		JSONObject jObject2=new JSONObject();
		
		try{
			jObject2.put("newslist", newsList);
			jObject.put("ret",0);
			jObject.put("msg","ok");
			jObject.put("data",jObject2);
		}catch(Exception e){
			try{
			jObject.put("ret", 1);
			jObject.put("msg", e.getMessage());
			jObject.put("data","");
			}catch(Exception e2){
				e2.printStackTrace();
			}
		}
		
		out.println(jObject);
		out.flush();
		out.close();
	}

}
