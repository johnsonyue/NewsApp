package johnsonyue.newsserverbeta.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import johnsonyue.newsserverbeta.dao.NewsDAO;

import org.json.JSONObject;

public class GetCommentsServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -566871676661914066L;

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html;charset=UTF-8");
		String newsId=request.getParameter("news_id");
		NewsDAO newsDAO=new NewsDAO();
		ArrayList<HashMap<String,Object>> comments=newsDAO.getComments(newsId);
		
		PrintWriter out = response.getWriter();
		JSONObject jObject=new JSONObject();
		JSONObject jObject2=new JSONObject();
		
		try{
			jObject2.put("comments", comments);
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

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		out.flush();
		out.close();
	}

}
