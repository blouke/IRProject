package ir.controller;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ir.indexer.TermIndexer;
import ir.query.Document;
import ir.query.QueryProcessor;

/**
 * Servlet implementation class SearchController
 */
//@WebServlet("/search")
public class SearchController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static TermIndexer index;
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SearchController() {
		super();

		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		super.init(config);
		ServletContext context = config.getServletContext();

		InputStream fileIn = context.getResourceAsStream("/WEB-INF/classes/index");
		
		if (fileIn!=null){
			try {
				ObjectInputStream in = new ObjectInputStream(fileIn);
				index = (TermIndexer)in.readObject();
				in.close();
				fileIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		
		else {
			index = new TermIndexer(context.getResourceAsStream("/WEB-INF/classes/dump"));
			index.initialize();

			// serialize index
			FileOutputStream fileOut;
			try {
				fileOut = new FileOutputStream("/WEB-INF/classes/index");
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(index);
				out.close();
				fileOut.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}


			String jwnlPropPath = context.getInitParameter("jwnl.properties");
			try {
				URL jwnlPropURL = context.getResource(jwnlPropPath);
				String jwnlProp = Paths.get(jwnlPropURL.toURI()).toString();
				System.setProperty("jwnlProp", jwnlProp);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		ArrayList<Document> searchResults = new QueryProcessor(index).processQuery(request.getParameter("query"));
		request.setAttribute("results", searchResults);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/result.jsp");
		dispatcher.include(request, response);

		//		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
