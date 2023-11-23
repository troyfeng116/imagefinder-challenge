package com.eulerity.hackathon.ImageFinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eulerity.hackathon.Crawler.CrawlerConfig;
import com.eulerity.hackathon.Crawler.CrawlerResults;
import com.eulerity.hackathon.Crawler.Crawlers.ParallelBFSCrawler;

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
		String myBody = extractPostBody(aReq);
		System.out.println(String.format("Got request to: %s with body: %s", myPath, myBody));

		// TODO: handle invalid json
		JsonObject myBodyJson = GSON.fromJson(myBody, JsonObject.class);
		CrawlerConfig myCrawlerConfig = CrawlerConfig.of(myBodyJson);
		System.out.println(myCrawlerConfig.toString());

		CrawlerResults myResults = new ParallelBFSCrawler(myCrawlerConfig).crawlAndScrape();
		System.out.printf("time elapsed (ms): %d\n", myResults.getCrawlTimeMs());

		aRes.setContentType("text/json");
		aRes.getWriter().print(GSON.toJson(new ArrayList<>(myResults.getImgSrcs())));
	}

	private String extractPostBody(HttpServletRequest aReq) throws IOException {
		return aReq.getReader().lines().collect(Collectors.joining(""));
	}
}
