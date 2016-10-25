package com.javaheat.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaheat.client.models.authentication.AuthenticationData;
import com.javaheat.client.models.stacks.StackData;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
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
        HEAT_PORT("8004"),
        HEAT_VERSION("v1"),
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
    private static String tenant_id;
    private static String token_id;

    private static boolean isAuthenticated = false;

    private static HttpResponse constructAuthenticationRequest(String endpoint) throws IOException {

        StringBuilder buildUrl = new StringBuilder();
        buildUrl.append("http://");
        buildUrl.append(endpoint);
        buildUrl.append(":");
        buildUrl.append(Constants.AUTH_PORT.toString());
        buildUrl.append(Constants.AUTH_URI.toString());

        HttpPost post =  new HttpPost(buildUrl.toString());

        String body = String.format(
                "{\"auth\": {\"tenantName\": \"%s\", \"passwordCredentials\": {\"username\": \"%s\", \"password\": \"%s\"}}}",
                Constants.TENANT_ID.toString(),
                Constants.USERNAME.toString(),
                Constants.PASSWORD.toString());

        post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        HttpClient httpClient = HttpClientBuilder.create().build();

        isAuthenticated = true;
        return httpClient.execute(post);

    }


    private static HttpResponse listStacksRequest(String endpoint) throws IOException {

        HttpGet getStack;
        HttpClient httpClient = HttpClientBuilder.create().build();

        if (isAuthenticated){

            StringBuilder buildUrl = new StringBuilder();
            buildUrl.append("http://");
            buildUrl.append(endpoint);
            buildUrl.append(":");
            buildUrl.append(Constants.HEAT_PORT.toString());
            buildUrl.append(String.format("/%s/%s/stacks", Constants.HEAT_VERSION.toString(),tenant_id));

            System.out.println(buildUrl);
            System.out.println(token_id);

            getStack = new HttpGet(buildUrl.toString());
            getStack.addHeader(Constants.AUTHTOKEN_HEADER.toString(), token_id);

            return httpClient.execute(getStack);

        } else {
            throw new IOException("You must Authenticate before issuing this request, please re-authenticate. ");
        }

    }

    private static HttpResponse createStack(String endpoint, String template) throws IOException {

        HttpPost createStack;
        HttpClient httpClient = HttpClientBuilder.create().build();

        if (isAuthenticated) {

            StringBuilder buildUrl = new StringBuilder();
            buildUrl.append("http://");
            buildUrl.append(endpoint);
            buildUrl.append(":");
            buildUrl.append(Constants.HEAT_PORT.toString());
            buildUrl.append(String.format("/%s/%s/stacks", Constants.HEAT_VERSION.toString() ,tenant_id));

            createStack = new HttpPost(buildUrl.toString());
            createStack.setEntity(new StringEntity(template, ContentType.APPLICATION_JSON));
            createStack.addHeader(Constants.AUTHTOKEN_HEADER.toString(), token_id);

            return httpClient.execute(createStack);
        } else {
            throw new IOException("You must Authenticate before issuing this request, please re-authenticate. ");
        }
    }

    private static HttpResponse deleteStack(String endpoint,  String stackName, String stackId) throws IOException {

        HttpDelete deleteStack ;
        HttpClient httpClient = HttpClientBuilder.create().build();
        if (isAuthenticated) {

            StringBuilder buildUrl = new StringBuilder();
            buildUrl.append("http://");
            buildUrl.append(endpoint);
            buildUrl.append(":");
            buildUrl.append(Constants.HEAT_PORT.toString());
            buildUrl.append(String.format("/%s/%s/stacks/%s/%s",
                                            Constants.HEAT_VERSION.toString(),
                                            tenant_id,
                                            stackName,
                                            stackId));

            deleteStack = new HttpDelete(buildUrl.toString());
            deleteStack.addHeader(Constants.AUTHTOKEN_HEADER.toString(), token_id);

            return httpClient.execute(deleteStack);
        } else {
            throw new IOException("You must Authenticate before issuing this request, please re-authenticate. ");
        }
    }

    private static HttpResponse findStack(String endpoint, String stackIdentity) throws  IOException {
        HttpGet findStack;
        HttpClient httpClient = HttpClientBuilder.create().build();

        if (isAuthenticated){

            StringBuilder buildUrl = new StringBuilder();
            buildUrl.append("http://");
            buildUrl.append(endpoint);
            buildUrl.append(":");
            buildUrl.append(Constants.HEAT_PORT.toString());
            buildUrl.append(String.format("/%s/%s/stacks/%s", Constants.HEAT_VERSION.toString(),tenant_id, stackIdentity));

            System.out.println(buildUrl);
            System.out.println(token_id);

            findStack = new HttpGet(buildUrl.toString());
            findStack.addHeader(Constants.AUTHTOKEN_HEADER.toString(), token_id);

            return httpClient.execute(findStack);

        } else {
            throw new IOException("You must Authenticate before issuing this request, please re-authenticate. ");
        }

    }

    public static String sendRequest(String messageType, String endpoint) {

        HttpResponse response = null;
        StringBuilder sb = null ;
        String stackName= "helloHeat";

        if (messageType != null) {
            try {
                switch (messageType) {
                    case "authenticate":
                         response = constructAuthenticationRequest(endpoint);
                        break;
                    case "listStacks":
                        response = listStacksRequest(endpoint);
                        break;
                    case "createStack":
                        String heatTemplate = JavaHeatUtils.readFile("./test.yml");
                        response = createStack(endpoint, heatTemplate);
                        break;
                    case "deleteStack":
                        response = deleteStack(endpoint, stackName, "86155aba-b65c-4008-a739-75d05586b86f");
                        break;
                    case "findStack":
                        response = findStack(endpoint,stackName);
                        break;
                    case "checkStatus":
                        break;
                }

                // TODO add an if condition for delete stack since it does not return any response
                sb = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                String line ;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        } else {
            return "";
        }
    }



    public static void main(String [] args ) throws IOException, InterruptedException {

        ObjectMapper mapper = new ObjectMapper();
        String authRequest = sendRequest("authenticate", Constants.ENDPOINT.toString());

        AuthenticationData auth = mapper.readValue(authRequest , AuthenticationData.class);

        tenant_id = auth.getAccess().getToken().getTenant().getId();
        token_id = auth.getAccess().getToken().getId();

/*        String createStackRequest = sendRequest("createStack", Constants.ENDPOINT.toString());
        StackData stack = mapper.readValue(createStackRequest, StackData.class);
        System.out.println(stack.getStack().getId());*/


        String findStackRequest = sendRequest("findStack", Constants.ENDPOINT.toString());
        StackData stack_two = mapper.readValue(findStackRequest, StackData.class);
        System.out.println(stack_two.getStack().getStack_status());
//        System.out.println(sendRequest("deleteStack", Constants.ENDPOINT.toString()));

        //TODO Create the rest of the API Methods
    }


}
