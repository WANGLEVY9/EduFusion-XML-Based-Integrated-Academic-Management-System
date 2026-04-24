package edu.fusion.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class IntegrationXmlHttpClient {

    private IntegrationXmlHttpClient() {
    }

    public static String postXml(String serviceUrl, String xmlRequest) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(serviceUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/xml; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/xml");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(15000);

            OutputStream outputStream = connection.getOutputStream();
            try {
                outputStream.write(xmlRequest.getBytes("UTF-8"));
                outputStream.flush();
            } finally {
                outputStream.close();
            }

            InputStream inputStream = connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream();
            if (inputStream == null) {
                return "";
            }
            return readAll(inputStream);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to call integration service: " + serviceUrl, ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readAll(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        } finally {
            reader.close();
        }
        return builder.toString();
    }
}
