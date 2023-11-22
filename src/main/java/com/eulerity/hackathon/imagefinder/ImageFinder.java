package com.eulerity.hackathon.ImageFinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eulerity.hackathon.Scraper.ScrapeResults;
import com.eulerity.hackathon.Scraper.Scraper;
import com.eulerity.hackathon.ScraperManager.ScraperManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

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

	/*
	 * const postBody = {
	 * url: urlInput.value,
	 * maxImgs: maxImgsInput.value,
	 * maxPages: maxPagesInput.value,
	 * maxDepth: maxDepthInput.value,
	 * }
	 */
	@Override
	protected final void doPost(HttpServletRequest aReq, HttpServletResponse aRes)
			throws ServletException, IOException {
		String myPath = aReq.getServletPath();
		String myBody = aReq.getReader().lines().collect(Collectors.joining(""));
		System.out.println(String.format("Got request to: %s with body: %s", myPath, myBody));

		JsonObject myBodyJson = GSON.fromJson(myBody, JsonObject.class);
		System.out.println(myBodyJson);
		String myUrl = myBodyJson.get("url").getAsString();
		int myMaxImgs = myBodyJson.get("maxImgs").getAsInt();
		int myMaxPages = myBodyJson.get("maxPages").getAsInt();
		int myMaxDepth = myBodyJson.get("maxDepth").getAsInt();

		Set<String> myResults = new ScraperManager(myUrl, myMaxDepth, myMaxImgs, myMaxPages).crawlAndScrape();

		aRes.setContentType("text/json");
		aRes.getWriter().print(GSON.toJson(new ArrayList<>(myResults)));
	}
}
