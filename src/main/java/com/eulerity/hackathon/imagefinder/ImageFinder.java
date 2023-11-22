package com.eulerity.hackathon.ImageFinder;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eulerity.hackathon.Scraper.ScrapeResults;
import com.eulerity.hackathon.Scraper.Scraper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet(name = "ImageFinder", urlPatterns = { "/main" })
public class ImageFinder extends HttpServlet {
	private static final long SERIAL_VERSION_UID = 1L;

	protected static final Gson GSON = new GsonBuilder().create();

	// This is just a test array
	public static final String[] TEST_IMAGES = {
			"https://images.pexels.com/photos/545063/pexels-photo-545063.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/464664/pexels-photo-464664.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/406014/pexels-photo-406014.jpeg?auto=compress&format=tiny",
			"https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&format=tiny"
	};

	@Override
	protected final void doPost(HttpServletRequest aReq, HttpServletResponse aRes)
			throws ServletException, IOException {
		aRes.setContentType("text/json");
		String myPath = aReq.getServletPath();
		String myUrl = aReq.getParameter("url");
		System.out.println(String.format("Got request of: %s with query param:%s", myPath, myUrl));
		ScrapeResults myResults = new Scraper(myUrl).scrape();
		aRes.getWriter().print(GSON.toJson(myResults.getImageUrls()));
	}
}
