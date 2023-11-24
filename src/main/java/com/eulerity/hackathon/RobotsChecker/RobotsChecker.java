package com.eulerity.hackathon.RobotsChecker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.eulerity.hackathon.Constants;

import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;

public class RobotsChecker {
    public static SimpleRobotRules checkRobots(URL aUrl) throws IOException {
        URL myUrl = new URL(aUrl.getProtocol() + "://" + aUrl.getHost() + "/robots.txt");
        SimpleRobotRulesParser myParser = new SimpleRobotRulesParser();
        HttpURLConnection myConnection = (HttpURLConnection) myUrl.openConnection();
        int myStatusCode = myConnection.getResponseCode();
        System.out.println(myStatusCode);
        SimpleRobotRules myRules;
        if (myStatusCode != 200) {
            myRules = myParser.failedFetch(myStatusCode);
        } else {
            InputStream myInputStream = myConnection.getInputStream();
            byte[] myInputBytes = readAllBytes(myInputStream);
            myRules = myParser.parseContent(aUrl.toString(), myInputBytes, "application/txt",
                    Constants.USER_AGENT);
        }
        return myRules;
    }

    private static byte[] readAllBytes(InputStream aInputStream) throws IOException {
        final int myBufferSz = 1024;
        byte[] myBuffer = new byte[myBufferSz];
        int myNRead;
        IOException myException = null;

        try {
            try (ByteArrayOutputStream myOutputStream = new ByteArrayOutputStream()) {
                while ((myNRead = aInputStream.read(myBuffer, 0, myBufferSz)) != -1)
                    myOutputStream.write(myBuffer, 0, myNRead);

                return myOutputStream.toByteArray();
            }
        } catch (IOException aException) {
            myException = aException;
            throw aException;
        } finally {
            if (myException == null)
                aInputStream.close();
            else
                try {
                    aInputStream.close();
                } catch (IOException aException) {
                    myException.addSuppressed(aException);
                }
        }
    }
}
