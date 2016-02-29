package ir.controller;

import java.io.IOException;
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
		index = new TermIndexer(config.getServletContext().getResourceAsStream("/WEB-INF/classes/dump"));
		index.initialize();
		ServletContext context = config.getServletContext();
		String jwnlPropPath = context.getInitParameter("jwnl.properties");

		try {
			URL jwnlPropURL = context.getResource(jwnlPropPath);
			String jwnlProp = Paths.get(jwnlPropURL.toURI()).toString();
			System.setProperty("jwnlProp", jwnlProp);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
