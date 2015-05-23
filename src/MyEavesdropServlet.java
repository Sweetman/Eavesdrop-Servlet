import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Servlet implementation class MyEavesdropServlet
 */
@WebServlet("/MyEavesdropServlet")
public class MyEavesdropServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	HashMap<String, ArrayList<String>> sessions = new HashMap<String, ArrayList<String>>();
	
	String host = "http://eavesdrop.openstack.org/";
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MyEavesdropServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// parsing the query parameters
		String username = request.getParameter("username");
		System.out.println("username: " + username);
		String session = request.getParameter("session");
		System.out.println("session: " + session);
		String type = request.getParameter("type");
		System.out.println("type: " + type);
		String project = request.getParameter("project");
		System.out.println("project: " + project);
		String year = request.getParameter("year");
		System.out.println("year: " + year);
		int yearInt = 0;
		try{
			yearInt = Integer.parseInt(year);
		}catch(NumberFormatException nfe){  
		    yearInt = 0;
		} 
		
		Cookie[] cookies = request.getCookies();
		String userCookie = null;
		if(cookies != null){
			for(Cookie cookie : cookies){
			    if("username".equals(cookie.getName())){
			        userCookie = cookie.getValue();
			        System.out.println("username found");
			    }
			}
		}
		System.out.println("userCookie: " + userCookie);
		
		try{
			if((session != null || username != null) && (type != null || project != null || year != null)){
				response.getWriter().println("Session/username request must be separate from type/project/year request.");
			}else if(!type.equalsIgnoreCase("meetings") && !type.equalsIgnoreCase("irclogs")){
				response.getWriter().println("Disallowed value specified for parameter type");
			}else if(yearInt < 2010 || yearInt > 2015){
				response.getWriter().println("Disallowed value specified for parameter year");
			}else if(session != null && username != null){
				if(session.equals("start")){
					if(username.trim().length() == 0){
						response.getWriter().println("Can't have a username with all whitespace");
					}else if(userCookie == null){
						response.getWriter().println("starting session");
						startSession(username, sessions, response);
					}else{
						response.getWriter().println("Session already in progress.");
					}
				}
				if(session.equals("end")){
					if(userCookie != null){
						response.getWriter().println("Ending session.");
						endSession(username, sessions, response);
					} else {
						response.getWriter().println("No session to end.");
					}
				}else{
					response.getWriter().println("Session parameter is either start or end.");
				}
			}else if(type != null || project != null || year != null){
				// checks if username cookie is not null
				System.out.println("userCookie: " + userCookie);
				if(userCookie != null){
					// if it's not null then print out visited urls
					response.getWriter().println("Visited URLs");
					if(sessions.get(userCookie) != null){
						for(String url : sessions.get(userCookie))
							response.getWriter().println(url);
					}
					// get the new visited url and add it to the url list for that user
					StringBuffer requestURL = request.getRequestURL();
					if (request.getQueryString() != null) {
					    requestURL.append("?").append(request.getQueryString());
					}
					String completeURL = requestURL.toString();
					System.out.println("complete url: " + completeURL);
					if(sessions.get(userCookie) == null){
						System.out.println("Doing a lazy session start for an old cookie in the browser");
						startSession(userCookie, sessions, response);
					}
					if(!sessions.get(userCookie).contains(completeURL))
						sessions.get(userCookie).add(completeURL);
					response.getWriter().println("");
				}
				
				//querying the eavesdrop website
				StringBuilder newHost = new StringBuilder(host);
				if(type != null)
					newHost.append(type + "/");
				if(project != null)
					newHost.append(project + "/");
				if(year != null)
					newHost.append(year + "/");
				System.out.println("host: " + newHost);
				
				// make a connection and check if we get a 400, if 400 then return bad url error
				URL url = new URL(newHost.toString());
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				if(connection.getResponseCode() == 200 || connection.getResponseCode() == 304){
					response.getWriter().println("URL Data");
					Document doc = Jsoup.connect(newHost.toString()).get();
					Elements links = doc.select("body a");
				
					ListIterator<Element> iter = links.listIterator();
					while(iter.hasNext()){
						Element e = (Element) iter.next();
						String s = e.html();
						s = s.replace("#", "%23");
						if(!s.equals("Name") && !s.equals("Last modified") && !s.equals("Size") && !s.equals("Description") && !s.equals("Parent Directory"))
							response.getWriter().println(s);
					}
				} else{
					response.getWriter().println("The url you entered does not seem to be working.");
				}
			}
		} catch(MalformedURLException e){
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	private void startSession(String username, HashMap<String, ArrayList<String>> sessions, HttpServletResponse response) {
		Cookie cookie = new Cookie("username", username);
		cookie.setPath("/assignment2");
		response.addCookie(cookie);
		sessions.put(username, new ArrayList<String>());
		System.out.println("state of the arraylist: " + sessions.get(username).toString());
	}

	private void endSession(String username, HashMap<String, ArrayList<String>> sessions, HttpServletResponse response) {
		sessions.remove(username);
		Cookie cookie = new Cookie("username", username);
		cookie.setPath("/assignment2");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
