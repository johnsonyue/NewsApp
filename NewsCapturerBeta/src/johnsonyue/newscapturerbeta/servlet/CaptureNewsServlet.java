package johnsonyue.newscapturerbeta.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import johnsonyue.newscapturerbeta.dao.NewsDAO;

public class CaptureNewsServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7658953382583962157L;

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
		
		//response.setIntHeader("Refresh", 600);
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		NewsDAO newsDAO =new NewsDAO();
		String msg=newsDAO.captureNews();
		
		Calendar calendar = new GregorianCalendar();
	    String am_pm;
	    int hour = calendar.get(Calendar.HOUR);
	    int minute = calendar.get(Calendar.MINUTE);
	    int second = calendar.get(Calendar.SECOND);
	    if(calendar.get(Calendar.AM_PM) == 0)
	      am_pm = "AM";
	    else
	      am_pm = "PM";
	 
	    String CT = hour+":"+ minute +":"+ second +" "+ am_pm;
		
		String title = "Msg: "+msg;
		
		out.println(
		        "<html>\n" +
		        "<body bgcolor=\"#f0f0f0\">\n" +
		        "<h1 align=\"center\">" + title + "</h1>\n" +
		        "<p>" + newsDAO.getCount() + " added.</p>\n"+
		        "<p>" + newsDAO.getFailed() + " failed.</p>\n"+
		        "<p>Current Time is: " + CT + "</p>\n");
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

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.flush();
		out.close();
	}

}
