package com.javaheat.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.http.HttpResponse;
import sun.misc.IOUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JavaStackUtils {

    public static String readFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    public static String convertYamlToJson(String yamlToConvert) throws IOException {
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Object obj = yamlReader.readValue(yamlToConvert, Object.class);

        ObjectMapper jsonWriter = new ObjectMapper();
        return jsonWriter.writeValueAsString(obj);
    }

    public static String convertHttpResponseToString(HttpResponse response) throws IOException {

        int status = response.getStatusLine().getStatusCode();
        String statusCode = Integer.toString(status);

        if (statusCode.startsWith("2") || statusCode.startsWith("3")) {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } else if (status == 403) {
            throw new IOException("Access forbidden, make sure you are using the correct credentials: " + statusCode);
        } else if (status == 409) {
            throw new IOException("Stack is already created, conflict detected " + statusCode);
        } else {
            throw new IOException("Failed Request with Status Code: " + statusCode);
        }
    }
}
