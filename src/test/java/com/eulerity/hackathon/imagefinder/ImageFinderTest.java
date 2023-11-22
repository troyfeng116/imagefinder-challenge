package com.eulerity.hackathon.ImageFinder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.mockito.Mockito;

import com.google.gson.Gson;

public class ImageFinderTest {

	private HttpServletRequest theRequest;
	private HttpServletResponse theResponse;
	private StringWriter theStringWriter;
	private HttpSession theSession;

	@Before
	public void setUp() throws Exception {
		theRequest = Mockito.mock(HttpServletRequest.class);
		theResponse = Mockito.mock(HttpServletResponse.class);
		theStringWriter = new StringWriter();
		PrintWriter myPrintWriter = new PrintWriter(theStringWriter);
		Mockito.when(theResponse.getWriter()).thenReturn(myPrintWriter);
		Mockito.when(theRequest.getRequestURI()).thenReturn("/foo/foo/foo");
		Mockito.when(theRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/foo/foo/foo"));
		theSession = Mockito.mock(HttpSession.class);
		Mockito.when(theRequest.getSession()).thenReturn(theSession);
	}

	@Test
	public void test() throws IOException, ServletException {
		Mockito.when(theRequest.getServletPath()).thenReturn("/main");
		// new ImageFinder().doPost(theRequest, theResponse);
		// Assert.assertEquals(new Gson().toJson(ImageFinder.TEST_IMAGES),
		// theStringWriter.toString());
	}
}
