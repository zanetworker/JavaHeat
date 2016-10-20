package com.javaheat.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaheat.client.models.authentication.AuthenticationData;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class JavaHeatCore {

    public enum Constants {
        ENDPOINT("10.10.243.1"),
        AUTH_PORT("5000"),
        USERNAME("admin"),
        PASSWORD("sonata"),
        TENANT_ID("admin"),
        AUTHTOKEN_HEADER("X-AUTH-TOKEN"),
        AUTH_URI("/v2.0/tokens");

        private final String constantValue;

        Constants(String constantValue) {
            this.constantValue=constantValue;
        }

        @Override
        public String toString() {
            return this.constantValue;
        }
    }

    private static HttpEntityEnclosingRequestBase constructRequest(String endpoint,
                                                                   String port,
                                                                   String action) {
        StringBuilder buildUrl = new StringBuilder();
        buildUrl.append("http://");
        buildUrl.append(endpoint);
        buildUrl.append(":");
        buildUrl.append(port);

        /* TODO : Use regex on the switch statement */
        switch(action) {
            case "authenticate":
                buildUrl.append(Constants.AUTH_URI.toString());
                break;
            case "createStack":
                break;
            case "checkStatus":
                break;
            default:
                buildUrl.append(Constants.AUTH_URI.toString());
        }

        System.out.println(buildUrl.toString());
        return new HttpPost(buildUrl.toString());
    }


    private static String createBody(String action) {
        String body = null;

        switch(action){

        }
        return String.format(
                "{\"auth\": {\"tenantName\": \"%s\", \"passwordCredentials\": {\"username\": \"%s\", \"password\": \"%s\"}}}",
                Constants.TENANT_ID.toString(),
                Constants.USERNAME.toString(),
                Constants.PASSWORD.toString()
        );
    }

    public static String sendRequest(String messageType){

        if (messageType != null) {
            StringBuilder sb = null;
            HttpClient httpClient = HttpClientBuilder.create().build();

            HttpPost post = (HttpPost) constructRequest(
                    Constants.ENDPOINT.toString(),
                    Constants.AUTH_PORT.toString(), messageType);

            StringEntity requestEntity = new StringEntity(createBody(messageType), ContentType.APPLICATION_JSON);
            post.setEntity(requestEntity);
            try {

                HttpResponse response = httpClient.execute(post);

                sb = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }


        return "";
    }


    public static void main(String [] args ) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        AuthenticationData auth = mapper.readValue(sendRequest("authenticate"), AuthenticationData.class);
        String Token = auth.getAccess().getToken().getId();
        String Tenant = auth.getAccess().getToken().getTenant().getId();
        System.out.println(Token);
        System.out.println(Tenant);

        //TODO Create the rest of the API Methods
    }


}
