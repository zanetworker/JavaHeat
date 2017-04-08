package com.javaheat.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaheat.client.models.authentication.*;
import com.javaheat.client.models.authenticationv3.AuthenticationDataV3;
import com.javaheat.client.models.authenticationv3.CatalogItem;
import com.javaheat.client.models.authenticationv3.EndpointItem;
import com.javaheat.client.models.configuration.Config;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicStatusLine;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;


public class JavaStackCore {

    public enum Constants {
//        HEAT_PORT("8004"),
//        IMAGE_PORT("9292"),
//        COMPUTE_PORT("8774"),
//        HEAT_VERSION("v1"),
//        IMAGE_VERSION("v2"),
//        COMPUTE_VERSION("v2"),
        AUTH_PORT("5000"),
        AUTHTOKEN_HEADER("X-AUTH-TOKEN"),
        AUTH_URI_V2("/v2.0/tokens"),
        AUTH_URI_V3("/v3/auth/tokens");

        private final String constantValue;

        Constants(String constantValue) {
            this.constantValue = constantValue;
        }

        @Override
        public String toString() {
            return this.constantValue;
        }
    }

    private static class SingeltonJavaStackCoreHelper {
        private static final JavaStackCore _javaStackCore = new JavaStackCore();
    }

    private static JavaStackCore _javaStackCore;

    private String endpoint;
    private String username;
    private String password;
    private String tenant_id;
    private ObjectMapper mapper;
    private String token_id;
//    private String image_id;
    private boolean isAuthenticated = false;

    private static class Identity {
        static String PORT;
        static String VERSION;

        public static String getPORT() {
            return PORT;
        }

        public static void setPORT(String PORT) {
            Identity.PORT = PORT;
        }

        public static String getVERSION() {
            return VERSION;
        }

        public static void setVERSION(String VERSION) {
            Identity.VERSION = VERSION;
        }
    }

    private static class Compute {
        static String PORT;
        static String VERSION;

        public static String getPORT() {
            return PORT;
        }

        public static void setPORT(String PORT) {
            Compute.PORT = PORT;
        }

        public static String getVERSION() {
            return VERSION;
        }

        public static void setVERSION(String VERSION) {
            Compute.VERSION = VERSION;
        }
    }

    private static class Image {
        static String PORT;
        static String VERSION;

        public static String getPORT() {
            return PORT;
        }

        public static void setPORT(String PORT) {
            Image.PORT = PORT;
        }

        public static String getVERSION() {
            return VERSION;
        }

        public static void setVERSION(String VERSION) {
            Image.VERSION = VERSION;
        }
    }

    private static class Orchestration {
        static String PORT;
        static String VERSION;

        public static String getPORT() {
            return PORT;
        }

        public static void setPORT(String PORT) {
            Orchestration.PORT = PORT;
        }

        public static String getVERSION() {
            return VERSION;
        }

        public static void setVERSION(String VERSION) {
            Orchestration.VERSION = VERSION;
        }
    }


    private JavaStackCore() {
    }

    public static JavaStackCore getJavaStackCore() {
        return SingeltonJavaStackCoreHelper._javaStackCore;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTenant_id() {
        return this.tenant_id;
    }

    public void setTenant_id(String tenant_id) {
        this.tenant_id = tenant_id;
    }

    public String getToken_id() {
        return this.token_id;
    }


    public void authenticateClientV3(String endpoint) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post;
        HttpResponse response = null;
        HashMap <String, String> endpoint_details = new HashMap<>();

        if (!isAuthenticated) {
            StringBuilder buildUrl = new StringBuilder();
            buildUrl.append("http://");
            buildUrl.append(endpoint);
            buildUrl.append(":");
            buildUrl.append(Constants.AUTH_PORT.toString());
            buildUrl.append(Constants.AUTH_URI_V3.toString());

            post = new HttpPost(buildUrl.toString());
            String body = String.format("{\n" +
                    "    \"auth\": {\n" +
                    "        \"identity\": {\n" +
                    "            \"methods\": [\n" +
                    "                \"password\"\n" +
                    "            ],\n" +
                    "            \"password\": {\n" +
                    "                \"user\": {\n" +
                    "                    \"name\": \"%s\",\n" +
                    "                    \"domain\": {\n" +
                    "                        \"name\": \"%s\"\n" +
                    "                    },\n" +
                    "                    \"password\": \"%s\"\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n" +
                    "}", this.username, this.tenant_id, this.password);

            post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));

            response = httpClient.execute(post);
            this.token_id = response.getFirstHeader("X-Subject-Token").getValue();
            mapper = new ObjectMapper();

           AuthenticationDataV3 auth = mapper.readValue(
                    JavaStackUtils.convertHttpResponseToString(response),
                    AuthenticationDataV3.class
            );

            ArrayList <CatalogItem> catalogItems = auth.getToken().getCatalog();
            for (CatalogItem catalogItem : catalogItems ) {
                String type = catalogItem.getType();
                String id = catalogItem.getId();

                for (EndpointItem endpointItem: catalogItem.getEndpoints()) {
                    if (endpointItem.getIface().equals("public")) {
                        String [] path_port = endpointItem.getUrl().split(":");;
                        String []  path = path_port[2].split("/");
                        String version, port;

                        switch (type) {
                            case "identity":
                                port = path[0];
                                version = path[1];
                                Identity.setPORT(port);
                                Identity.setVERSION(version);
                                break;

                            case "orchestration":
                                port = path[0];
                                version = path[1];
                                Orchestration.setPORT(port);
                                Orchestration.setVERSION(version);
                                break;

                            case "image":
                                port = path[0];
                                version = "v2";
                                Image.setPORT(port);
                                Image.setVERSION(version);
                                break;

                            case "compute":
                                port = path[0];
                                version = path[1];
                                Compute.setPORT(port);
                                Compute.setVERSION(version);
                                break;

                            case "network":
                                break;

                            case "cloudformation":
                                break;

                            default:
                                System.out.println("Invalid Type");
                        }
                    }
                }
            }

          this.tenant_id = auth.getToken().getProject().getId();
          this.isAuthenticated = true;

        }  else{
            System.out.println("You are already authenticated");
        }

    }
    public void authenticateClientV2(String endpoint) throws IOException {

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post;
        HttpResponse response = null;

        if (!isAuthenticated) {
            StringBuilder buildUrl = new StringBuilder();
            buildUrl.append("http://");
            buildUrl.append(endpoint);
            buildUrl.append(":");
            buildUrl.append(Constants.AUTH_PORT.toString());
            buildUrl.append(Constants.AUTH_URI_V2.toString());

            post = new HttpPost(buildUrl.toString());


            String body = String.format(
                    "{\"auth\": {\"tenantName\": \"%s\", \"passwordCredentials\": {\"username\": \"%s\", \"password\": \"%s\"}}}",
                    this.tenant_id,
                    this.username,
                    this.password);

            post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));

            response = httpClient.execute(post);
            mapper = new ObjectMapper();

             AuthenticationData auth = mapper.readValue(
                    JavaStackUtils.convertHttpResponseToString(response),
                    AuthenticationData.class
            );
            this.token_id = auth.getAccess().getToken().getId();
            this.tenant_id = auth.getAccess().getToken().getTenant().getId();
            this.isAuthenticated = true;

        } else {
            System.out.println("You are already authenticated");
        }
    }

    public HttpResponse listStacksRequest(String endpoint) throws IOException {

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponseFactory factory = new DefaultHttpResponseFactory();
        HttpResponse response = null;
        HttpGet listStacks = null;

        if (this.isAuthenticated) {
            StringBuilder buildUrl = new StringBuilder();
            buildUrl.append("http://");
            buildUrl.append(endpoint);
            buildUrl.append(":");
            buildUrl.append(Orchestration.getPORT());
            buildUrl.append(String.format("/%s/%s/stacks", Orchestration.getVERSION(), this.tenant_id));

            System.out.println(buildUrl);
            System.out.println(this.token_id);

            listStacks = new HttpGet(buildUrl.toString());
            listStacks.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

            response = httpClient.execute(listStacks);
            int status_code = response.getStatusLine().getStatusCode();

            return (status_code == 200) ? response : factory.newHttpResponse(
                    new BasicStatusLine(
                            HttpVersion.HTTP_1_1,
                            status_code,
                            "List Failed with Status: " + status_code),
                    null);
        } else {
            throw new IOException("You must Authenticate before issuing this request, please re-authenticate. ");
        }
    }

    public HttpResponse showResourceData(String stackName, String stackId, String resourceName) throws IOException, URISyntaxException {
        HttpResponseFactory factory = new DefaultHttpResponseFactory();
        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpGet showResourceData = null;
        HttpResponse response = null;

        if (isAuthenticated) {
            URIBuilder builder = new URIBuilder();
            String path = String.format("/%s/%s/stacks/%s/%s/resources/%s",
                    Orchestration.getVERSION(),
                    this.tenant_id, stackName,stackId, resourceName);

            builder.setScheme("http")
                    .setHost(endpoint)
                    .setPort(Integer.parseInt(Orchestration.getPORT()))
                    .setPath(path);

            URI uri = builder.build();

            showResourceData = new HttpGet(uri);
            showResourceData.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

            response = httpclient.execute(showResourceData);
            int status_code = response.getStatusLine().getStatusCode();

            return (status_code == 200) ? response : factory.newHttpResponse(
                    new BasicStatusLine(
                            HttpVersion.HTTP_1_1,
                            status_code,
                            "List Failed with Status: " + status_code),
                    null);

        } else {
            throw new IOException("You must Authenticate before issuing this request, please re-authenticate. ");
        }
    }



    public HttpResponse listStackResources(String stackName, String stackId, ArrayList<String> resources) throws IOException, URISyntaxException {
        HttpResponseFactory factory = new DefaultHttpResponseFactory();
        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpGet listResources = null;
        HttpResponse response = null;

        if (isAuthenticated) {
            URIBuilder builder = new URIBuilder();
            String path = String.format("/%s/%s/stacks/%s/%s/resources",
                    Orchestration.getVERSION(),
                    this.tenant_id, stackName, stackId);

            builder.setScheme("http")
                    .setHost(endpoint)
                    .setPort(Integer.parseInt(Orchestration.getPORT()))
                    .setPath(path);

            URI uri = builder.build();

            listResources = new HttpGet(uri);
            listResources.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);


            response = httpclient.execute(listResources);
            int status_code = response.getStatusLine().getStatusCode();

            return (status_code == 200) ? response : factory.newHttpResponse(
                    new BasicStatusLine(
                            HttpVersion.HTTP_1_1,
                            status_code,
                            "List Failed with Status: " + status_code),
                    null);

        } else {
            throw new IOException("You must Authenticate before issuing this request, please re-authenticate. ");
        }
    }

    public HttpResponse createImage(String template,
                                    String containerFormat,
                                    String diskFormat,
                                    String name) throws IOException {
        HttpPost createImage;
        HttpClient httpClient = HttpClientBuilder.create().build();

        if (this.isAuthenticated) {
            StringBuilder buildUrl = new StringBuilder();
            buildUrl.append("http://");
            buildUrl.append(this.endpoint);
            buildUrl.append(":");
            buildUrl.append(Image.getVERSION());
            buildUrl.append(String.format("/%s/images", Image.getPORT()));

            createImage = new HttpPost(buildUrl.toString());
            String requestBody = String.format("{ \"container_format\": \"bare\"," +
                    "\"disk_format\": \"raw\"," +
                    " \"name\": \"%s\"}", name);

            createImage.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
            createImage.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

        } else {
            throw new IOException("You must Authenticate before issuing this request, please re-authenticate. ");
        }
        return httpClient.execute(createImage);
    }




    public HttpResponse uploadBinaryImageData(String endpoint, String imageId, String binaryImage) throws IOException {

        HttpPut uploadImage;
        HttpClient httpClient = HttpClientBuilder.create().build();

        if (this.isAuthenticated) {
            StringBuilder buildUrl = new StringBuilder();
            buildUrl.append("http://");
            buildUrl.append(endpoint);
            buildUrl.append(":");
            buildUrl.append(Image.getPORT());
            buildUrl.append(String.format("/%s/images/%s/file", Image.getVERSION(), imageId));

            uploadImage = new HttpPut(buildUrl.toString());
            uploadImage.setHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);
            uploadImage.setHeader("Content-Type", "application/octet-stream");
            uploadImage.setEntity(new FileEntity(new File(binaryImage)));

        } else {
            throw new IOException("You must Authenticate before issuing this request, please re-authenticate. ");
        }
        return httpClient.execute(uploadImage);
    }

    public HttpResponse createStack(String template, String stackName) throws IOException {

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponseFactory factory = new DefaultHttpResponseFactory();
        HttpPost createStack = null;
        HttpResponse response = null;

        String jsonTemplate = JavaStackUtils.convertYamlToJson(template);
        JSONObject modifiedObject = new JSONObject();
        modifiedObject.put("stack_name", stackName);
        modifiedObject.put("template", new JSONObject(jsonTemplate));

        if (this.isAuthenticated) {

            StringBuilder buildUrl = new StringBuilder();
            buildUrl.append("http://");
            buildUrl.append(this.endpoint);
            buildUrl.append(":");
            buildUrl.append(Orchestration.getVERSION());
            buildUrl.append(String.format("/%s/%s/stacks", Orchestration.getVERSION(), tenant_id));

            System.out.println(buildUrl.toString());
            createStack = new HttpPost(buildUrl.toString());
            createStack.setEntity(new StringEntity(modifiedObject.toString(), ContentType.APPLICATION_JSON));
            System.out.println(this.token_id);
            createStack.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

            response = httpClient.execute(createStack);
            int status_code = response.getStatusLine().getStatusCode();

            return (status_code == 201) ? response : factory.newHttpResponse(
                    new BasicStatusLine(
                            HttpVersion.HTTP_1_1,
                            status_code,
                            "List Failed with Status: " + status_code),
                    null);
        } else {
            throw new IOException("You must Authenticate before issuing this request, please re-authenticate. ");
        }
    }

    public HttpResponse deleteStack(String stackName, String stackId) throws IOException {

        HttpDelete deleteStack;
        HttpClient httpClient = HttpClientBuilder.create().build();
        if (this.isAuthenticated) {

            StringBuilder buildUrl = new StringBuilder();
            buildUrl.append("http://");
            buildUrl.append(this.endpoint);
            buildUrl.append(":");
            buildUrl.append(Orchestration.getPORT());
            buildUrl.append(String.format("/%s/%s/stacks/%s/%s",
                    Orchestration.getVERSION(),
                    tenant_id,
                    stackName,
                    stackId));
            deleteStack = new HttpDelete(buildUrl.toString());
            deleteStack.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

            return httpClient.execute(deleteStack);
        } else {
            throw new IOException("You must Authenticate before issuing this request, please re-authenticate. ");
        }
    }

    public HttpResponse findStack(String stackIdentity) throws IOException {
        HttpGet findStack;
        HttpClient httpClient = HttpClientBuilder.create().build();

        if (this.isAuthenticated) {

            StringBuilder buildUrl = new StringBuilder();
            buildUrl.append("http://");
            buildUrl.append(this.endpoint);
            buildUrl.append(":");
            buildUrl.append(Orchestration.getPORT());
            buildUrl.append(String.format("/%s/%s/stacks/%s", Orchestration.getVERSION(), tenant_id, stackIdentity));

            System.out.println(buildUrl);
            System.out.println(token_id);

            findStack = new HttpGet(buildUrl.toString());
            findStack.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

            return httpClient.execute(findStack);

        } else {
            throw new IOException("You must Authenticate before issuing this request, please re-authenticate. ");
        }

    }

    //http://10.10.243.1:8774/v2/4c0cef53eb3141c3877a21fc86803f78/servers

    public HttpResponse listComputeLimits() throws IOException {
        HttpGet getLimits = null;
        HttpResponse response = null;

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponseFactory factory = new DefaultHttpResponseFactory();

        if (isAuthenticated) {
            StringBuilder buildUrl = new StringBuilder();
            buildUrl.append("http://");
            buildUrl.append(endpoint);
            buildUrl.append(":");
            buildUrl.append(Compute.getPORT());
            buildUrl.append(String.format("/%s/%s/limits", Compute.getVERSION(), this.tenant_id));

            getLimits = new HttpGet(buildUrl.toString());
            getLimits.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);
            System.out.println(buildUrl);

            response = httpClient.execute(getLimits);
            int status_code = response.getStatusLine().getStatusCode();
            return (status_code == 200) ? response : factory.newHttpResponse(
                    new BasicStatusLine(
                            HttpVersion.HTTP_1_1,
                            status_code,
                            "List Failed with Status: " + status_code),
                    null);
        }
        return response;
    }

    public HttpResponse listComputeFlavors() throws IOException {
        HttpGet getFlavors = null;
        HttpResponse response = null;

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponseFactory factory = new DefaultHttpResponseFactory();

        if (isAuthenticated) {
            StringBuilder buildUrl = new StringBuilder();
            buildUrl.append("http://");
            buildUrl.append(endpoint);
            buildUrl.append(":");
            buildUrl.append(Compute.getPORT());
            buildUrl.append(String.format("/%s/%s/flavors/detail", Compute.getVERSION(), this.tenant_id));

            getFlavors = new HttpGet(buildUrl.toString());
            getFlavors.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

            response = httpClient.execute(getFlavors);
            int status_code = response.getStatusLine().getStatusCode();
            return (status_code == 200) ? response : factory.newHttpResponse(
                    new BasicStatusLine(
                            HttpVersion.HTTP_1_1,
                            status_code,
                            "List Failed with Status: " + status_code),
                    null);
        }
        return response;
    }

    public HttpResponse listImages() throws IOException {

        HttpGet listImages = null;
        HttpResponse response = null;

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponseFactory factory = new DefaultHttpResponseFactory();

        if (isAuthenticated) {

            StringBuilder buildUrl = new StringBuilder();
            buildUrl.append("http://");
            buildUrl.append(endpoint);
            buildUrl.append(":");
            buildUrl.append(Image.getPORT());
            buildUrl.append(String.format("/%s/images", Image.getVERSION()));

            listImages = new HttpGet(buildUrl.toString());
            listImages.addHeader(Constants.AUTHTOKEN_HEADER.toString(), this.token_id);

            response = httpClient.execute(listImages);
            int status_code = response.getStatusLine().getStatusCode();
            return (status_code ==  200) ? response : factory.newHttpResponse(
                    new BasicStatusLine(
                            HttpVersion.HTTP_1_1,
                            status_code,
                            "BOOM! Listing Images Failed with Status: " + status_code),
                    null);
        }
        return response;
    }
    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {

        ObjectMapper mapper;
        String vim_config = JavaStackUtils.readFile("./vim_configuration.yml");
        String vim_config_json = JavaStackUtils.convertYamlToJson(vim_config);
        System.out.println(vim_config_json);

        mapper = new ObjectMapper();
        Config config = mapper.readValue(vim_config_json, Config.class);

        String endpoint = config.getVim().getEndpoint(),
                username = config.getVim().getUsername(),
                password = config.getVim().getPassword(),
                domain = config.getVim().getDomain();


        JavaStackCore javaStack = JavaStackCore.getJavaStackCore();
        javaStack.setEndpoint(endpoint);
        javaStack.setUsername(username);
        javaStack.setPassword(password);
        javaStack.setTenant_id(domain);
        javaStack.authenticateClientV3(endpoint);

        System.out.println(endpoint + username + password + domain);
        System.out.println(javaStack.getToken_id());
        //
//        String listImages = JavaStackUtils.convertHttpResponseToString(javaStack.listImages());
//        Images images = mapper.readValue(listImages, Images.class);
//        for (Image image : images.getImages()) {
//            System.out.println(image.getContainer_format() + ": " + image.getId() + ": " + image.getName());
//        }
//
//        String listLimits = JavaStackUtils.convertHttpResponseToString(javaStack.listComputeLimits());
//        String output = "{\"limits\": {\"rate\": [], \"absolute\": {\"maxServerMeta\": 128, \"maxPersonality\": 5, \"totalServerGroupsUsed\": 0, \"maxImageMeta\": 128, \"maxPersonalitySize\": 10240, \"maxTotalKeypairs\": 100, \"maxSecurityGroupRules\": 20, \"maxServerGroups\": 10, \"totalCoresUsed\": 8, \"totalRAMUsed\": 16384, \"totalInstancesUsed\": 7, \"maxSecurityGroups\": 10, \"totalFloatingIpsUsed\": 0, \"maxTotalCores\": 20, \"maxServerGroupMembers\": 10, \"maxTotalFloatingIps\": 10, \"totalSecurityGroupsUsed\": 1, \"maxTotalInstances\": 10, \"maxTotalRAMSize\": 51200}}}";
//        mapper = new ObjectMapper();
//        LimitsData data = mapper.readValue(output, LimitsData.class);
//        System.out.println(data.getLimits().getAbsolute().getTotalRAMUsed());
//
//
//        String listFlavors = JavaStackUtils.convertHttpResponseToString(javaStack.listComputeFlavors());
//        System.out.println(listFlavors);
//        FlavorsData flavors = mapper.readValue(listFlavors, FlavorsData.class);
//        for (Flavor flavor : flavors.getFlavors()) {
//            System.out.println(flavor.getId() + ": " + flavor.getRam());
//        }

//        String template = JavaStackUtils.readFile ("./test.yaml");
//        String createStackResponse = JavaStackUtils.convertHttpResponseToString(javaStack.createStack(template, "boo"));
//
//        System.out.println(createStackResponse);
//        StackData stack = mapper.readValue(createStackResponse, StackData.class);
//        String uuid = stack.getStack().getId();
//
//        System.out.println(uuid);
//
//        Thread.sleep(10000);
        // sonata-demo-19df6a98f-9e11-4cb7-b3c0-InAdUnitTest-01

//        //FindStack with name
//        String findStackResponse = JavaStackUtils.convertHttpResponseToString(javaStack.findStack("boo"));
//        String stackIdToDelete = mapper.readValue(findStackResponse, StackData.class).getStack().getId();
//
//        //List Stack Resources
//        String listResources = JavaStackUtils.convertHttpResponseToString(javaStack.listStackResources("boo", stackIdToDelete, null));
//        System.out.println(listResources);
//
//        ArrayList <Resource> resources = mapper.readValue(listResources, Resources.class).getResources();
//
//        //Output lists
//        ArrayList<HeatServer> servers = new ArrayList<>();
//        ArrayList<HeatPort> ports = new ArrayList<>();
//
//        //Helper lists
//        ArrayList<PortAttributes> portsAtts = new ArrayList<>();
//        ArrayList<FloatingIpAttributes> floatingIps = new ArrayList<>();
//
//        for (Resource resource : resources) {
//            HeatServer heatServer = new HeatServer();
//            HeatPort heatPort = new HeatPort();
//
//            System.out.println(resource.getResource_type());
//
//            //Show ResourceData
//            System.out.println(stackIdToDelete);
//            String showResourceData = JavaStackUtils.convertHttpResponseToString(javaStack.showResourceData("boo",stackIdToDelete,
//                    resource.getResource_name()));
//            System.out.println(showResourceData);
//
//            switch(resource.getResource_type()) {
//
//                case "OS::Nova::Server":
//                    ResourceData <ServerAttributes> serverResourceData = mapper.readValue(showResourceData,
//                            new TypeReference<ResourceData<ServerAttributes>>(){});
//
//                    //Set Server
//                    heatServer.setServerId(serverResourceData.getResource().getPhysical_resource_id());
//                    heatServer.setServerName(serverResourceData.getResource().getAttributes().getName());
//                    servers.add(heatServer);
//                    break;
//
//                case "OS::Neutron::Port":
//                    ResourceData <PortAttributes> portResourceData = mapper.readValue(showResourceData,
//                            new TypeReference<ResourceData<PortAttributes>>(){});
//
//                    portsAtts.add(portResourceData.getResource().getAttributes());
//                    //Set Port
//                    heatPort.setIpAddress(portResourceData.getResource().getAttributes().getFixed_ips().get(0).get("ip_address"));
//                    heatPort.setMacAddress(portResourceData.getResource().getAttributes().getMac_address());
//                    heatPort.setPortName(portResourceData.getResource().getAttributes().getName());
//                    ports.add(heatPort);
//                    break;
//
//                case "OS::Neutron::FloatingIP":
//                    ResourceData <FloatingIpAttributes> floatingIPResourceData = mapper.readValue(showResourceData,
//                            new TypeReference<ResourceData<FloatingIpAttributes>>(){});
//                    floatingIps.add(floatingIPResourceData.getResource().getAttributes());
//                    String floatingIP = floatingIPResourceData.getResource().getAttributes().getFloating_ip_address();
//                    System.out.println("FloatingIP Resource Address: " + floatingIP);
//                    break;
//
//                case "OS::Neutron::Net": break;
//                case "OS::Neutron::Router": break;
//                default:
//                    System.out.println("invalid Type");
//            }
//        }
//
//        for (int i=0; i< ports.size(); i++) {
//            for (FloatingIpAttributes floatingIP : floatingIps) {
//                if (portsAtts.get(i).getId().equals(floatingIP.getPort_id())){
//                    ports.get(i).setFloatinIp(floatingIP.getFloating_ip_address());
//                }
//            }
//        }
//
//
//        System.out.println(servers);
//        System.out.println(ports);

    }


}
