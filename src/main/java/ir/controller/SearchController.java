package ir.controller;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
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
	private static Long indexSize;
	private static String indexCreationTime;
	
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
		
		InputStream fileIn = context.getResourceAsStream("index.ser");
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
		} else {
			index = new TermIndexer(context.getResourceAsStream("/WEB-INF/classes/dump"));
			index.initialize();

			// serialize index
			FileOutputStream fileOut; 
			try {
				String indexFilePath = context.getRealPath("/")+"index.ser"; 
				fileOut = new FileOutputStream(indexFilePath);
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(index);
				out.close();
				fileOut.close();
				
				// these two lines needed to make sure that file is persisted before reading it later.
//				fileOut = new FileOutputStream(indexFilePath);
//				fileOut.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		

		// reading INDEX file size and time of creation.
		Map<String,Object> fileAttributes = new HashMap<String,Object>();
		
			try {
				String indexFilePath = context.getRealPath("/")+"index.ser"; 
				Path indexPath = Paths.get(indexFilePath);
				fileAttributes = Files.readAttributes(indexPath, "size,creationTime");
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		indexSize = ((Long)fileAttributes.get("size"))/1024;
		FileTime indexFileTime = (FileTime) fileAttributes.get("creationTime");
		indexCreationTime = DateFormat.getDateTimeInstance().format(indexFileTime.toMillis());
		
		
		
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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String requestPath = request.getServletPath();
		if (requestPath.isEmpty()){
			request.setAttribute("indexSize", indexSize);
			request.setAttribute("indexCreationTime", indexCreationTime);
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
			dispatcher.include(request, response);
			
		} else if (requestPath.equals("/search")){
		
			ArrayList<Document> searchResults = null;
			Map<String, String[]> requestDataMap = request.getParameterMap();
			QueryProcessor queryProcessor = new QueryProcessor(index);
			queryProcessor.processQuery(request.getParameter("query"));
			
			if (requestDataMap.containsKey("refineQuery")){
				String[] docIds = requestDataMap.get("docId");
				String[] relevance = requestDataMap.get("relevanceHidden");
				searchResults = queryProcessor.generateUpdatedResults(docIds, relevance);
			}
			else {
				searchResults  = queryProcessor.generateResults();
			}
			
			request.setAttribute("results", searchResults);
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/result.jsp");
			dispatcher.include(request, response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}