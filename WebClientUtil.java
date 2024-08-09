package com.stanbic.bua.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import javax.net.ssl.SSLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class WebClientUtil {
    @Value("${isOverProxy}")
    private Boolean useProxy;
    @Value("${proxy.host}")
    private String proxyHost;
    @Value("${proxy.port}")
    private String proxyPort;

private HttpClient httpClientNoProxyAndCrtValidation() {

    return HttpClient.create().secure(sslContextSpec -> {
        try {
            sslContextSpec.sslContext(
                    SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build());
        } catch (SSLException e) {
            e.printStackTrace();
        }
    });

}

    public String httpPostRequest(String url, Object request, String operation, Boolean useProxy, HashMap<String, String> httpHeaders) throws ServerErrorException, JsonProcessingException, JsonProcessingException {
        log.info("Making HTTP Call for {} to: {}", operation, url);
//        log.info("Making HTTP Call module_id, auth {} to: {}", httpHeaders.get("module_id"), httpHeaders.get("Authorization"));
//        log.info("Request body: {}", objectMapper.writeValueAsString(request));
        try {

//            SslContext sslContext = SslContextBuilder.forClient()
//                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
//                    .build();
//            HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
//            HttpClient httpClient = null;
//            if (useProxy){httpClient = withProxy();}
//            else{httpClient = withoutProxy();}

            WebClient client = WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClientNoProxyAndCrtValidation()))
                    .defaultHeader("serviceToken", httpHeaders.get("serviceToken"))
                    .defaultHeader("requestSourceIp", httpHeaders.get("requestSourceIp"))
                    .defaultHeader("module_id", httpHeaders.get("module_id"))
                    .defaultHeader("Authorization", httpHeaders.get("Authorization"))
                    .build();

            WebClient.UriSpec<WebClient.RequestBodySpec> uriSpec = client.method(HttpMethod.POST);
            WebClient.RequestBodySpec bodySpec = uriSpec.uri(url);
            WebClient.RequestHeadersSpec<?> headersSpec = bodySpec.bodyValue(request);

            headersSpec.header( "Content-Type","application/json");
            WebClient.ResponseSpec responseSpec = headersSpec.header(HttpHeaders.CONTENT_TYPE)
                    .accept(MediaType.APPLICATION_JSON)
                    .ifNoneMatch("*")
                    .retrieve();

            Mono<String> messageResponse = headersSpec.exchangeToMono(response -> response.bodyToMono(String.class));
            String response = messageResponse.block();
            log.info("Response body: {}", response);
            return response;
        } catch (Exception e) {
            log.info("Error encountered connecting to " + url + " for this reason " + e);
            throw new ServerErrorException("Internal server occurred");
        }
    }

    public String httpPostRequestxfer(String url, Object request, String operation, Boolean useProxy, HashMap<String, String> httpHeaders) throws ServerErrorException, JsonProcessingException {
        log.info("Making HTTP Call for {} to: {}", operation, url);
//        log.info("Request body: {}", objectMapper.writeValueAsString(request));
        try {
            HttpClient httpClient = null;

            if (useProxy){httpClient = withProxy();}
            else{httpClient = withoutProxy();}
            System.out.println("1");
            WebClient client = WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
//                    .defaultHeader("serviceToken", httpHeaders.get("serviceToken"))
//                    .defaultHeader("requestSourceIp", httpHeaders.get("requestSourceIp"))
//                    .defaultHeader("module_id", httpHeaders.get("module_id"))
//                    .defaultHeader("authorization", httpHeaders.get("authorization"))
                    .build();
            System.out.println("2");
            WebClient.UriSpec<WebClient.RequestBodySpec> uriSpec = client.method(HttpMethod.POST);
            WebClient.RequestBodySpec bodySpec = uriSpec.uri(url);
            WebClient.RequestHeadersSpec<?> headersSpec = bodySpec.bodyValue(request);
            System.out.println("3");
            headersSpec.header( "Content-Type","application/json");
            WebClient.ResponseSpec responseSpec = headersSpec.header(HttpHeaders.CONTENT_TYPE)
                    .accept(MediaType.APPLICATION_JSON)
                    .ifNoneMatch("*")
                    .retrieve();
            System.out.println("4");
            Mono<String> messageResponse = headersSpec.exchangeToMono(response -> response.bodyToMono(String.class));
            System.out.println("5"+messageResponse);
            String response = messageResponse.block();
            log.info("Response body: {}", response);
            return response;
        } catch (Exception e) {
            log.info("Error encountered connecting to " + url + " for this reason " + e);
            throw new ServerErrorException("Internal server occurred");
        }
    }

    public String serviceTokenxfer(String url, Object request, Boolean useProxy, HashMap<String, String> httpHeaders) {
//        ServiceAuthenticationRequest serviceTokenBody = new ServiceAuthenticationRequest();
//        serviceTokenBody.setToken(serviceToken);
//        serviceTokenBody.setSourceIpAddress(serviceIpAddress);
//        log.info("Authentication Request Body: " + serviceTokenBody);
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000)
                .responseTimeout(Duration.ofSeconds(60))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(60, TimeUnit.SECONDS)));
        WebClient client = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        WebClient.UriSpec<WebClient.RequestBodySpec> uriSpec = client.method(HttpMethod.POST);
//        WebClient.UriSpec<WebClient.RequestBodySpec> uriSpec = client.method(operation);
        WebClient.RequestBodySpec bodySpec = uriSpec.uri(url);
        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec.bodyValue(request);
        headersSpec.header( "Content-Type","application/json");
        WebClient.ResponseSpec responseSpec = headersSpec.header(
                        HttpHeaders.CONTENT_TYPE)
                .accept(MediaType.APPLICATION_JSON)
                .ifNoneMatch("*")
                .retrieve();

//        Mono<Object> messageResponse = headersSpec.exchangeToMono(response -> {
//            if (response.statusCode().equals(HttpStatus.OK)) {
//                return response.bodyToMono(Object.class);
//            } else if (response.statusCode().is4xxClientError()) {
//                return response.bodyToMono(Object.class);
//            } else {
//                return response.bodyToMono(Object.class);
//            }
//        });
        Mono<String> messageResponse = headersSpec.exchangeToMono(response -> response.bodyToMono(String.class));
//        Object message = messageResponse.block();
        String response = messageResponse.block();
        return response;
    }


    public String httpGetRequest(String url, String operation, Boolean useProxy, HttpHeaders httpHeaders) throws ServerErrorException {
        log.info("Making HTTP Call for {} to: {}", operation, url);
        log.info("Request body: {}", operation);
        try {

            HttpClient httpClient = null;
            if (useProxy){httpClient = withProxy();}
            else{httpClient = withoutProxy();}

            WebClient client = WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();
            String response = client.get()
                    .uri(url)
                    .headers(h -> h.addAll(httpHeaders))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("Response body: {}", response);
            return response;
        } catch (Exception e) {
            log.info("Error encountered connecting to " + url + " for this reason " + e);
            throw new ServerErrorException("Internal server occurred");
        }
    }

    public String apiCall(String apiURL) throws ServerErrorException {
        log.info("Making HTTP Call to: {}", apiURL);

        try {
            HttpClient httpClient = null;
            if (useProxy){
                httpClient = withProxy();
            }
            else{
                httpClient = withoutProxy();
            }
            WebClient client = WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();
            String response = client.get()
                    .uri(apiURL)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("Clikatel response : {}", response);
            return response;
        } catch (Exception e) {
            log.info("Error encountered connecting to " + apiURL + " for this reason " + e);
            throw new ServerErrorException("Internal server occurred");
        }
    }

    private HttpClient withoutProxy(){
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
                .responseTimeout(Duration.ofSeconds(60))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(60, TimeUnit.SECONDS)));
        return httpClient;
    }

    private HttpClient withProxy(){
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
                .responseTimeout(Duration.ofSeconds(180))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(150, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(150, TimeUnit.SECONDS)))
                .proxy(proxy -> proxy.type(ProxyProvider.Proxy.HTTP)
                        .host(proxyHost)
                        .port(Integer.parseInt(proxyPort)));
        return httpClient;
    }


//    public String httpPostRequest2(String url, Object request, String operation, Boolean useProxy, HashMap<String, String> httpHeaders) throws ServerErrorException, JsonProcessingException, JsonProcessingException {
//        log.info("Making HTTP Call for {} to: {}", operation, url);
//        try {
//            WebClient client = WebClient.builder()
//                    .clientConnector(new ReactorClientHttpConnector(httpClientNoProxyAndCrtValidation()))
//                    .defaultHeader("serviceToken", httpHeaders.get("serviceToken"))
//                    .defaultHeader("requestSourceIp", httpHeaders.get("requestSourceIp"))
//                    .defaultHeader("module_id", httpHeaders.get("module_id"))
//                    .defaultHeader("Authorization", httpHeaders.get("Authorization"))
//                    .build().;
//
//            WebClient.UriSpec<WebClient.RequestBodySpec> uriSpec = client.method(HttpMethod.POST);
//            WebClient.RequestBodySpec bodySpec = uriSpec.uri(url);
//            WebClient.RequestHeadersSpec<?> headersSpec = bodySpec.bodyValue(request);
//
//            headersSpec.header( "Content-Type","application/json");
//            WebClient.ResponseSpec responseSpec = headersSpec.header(HttpHeaders.CONTENT_TYPE)
//                    .accept(MediaType.APPLICATION_JSON)
//                    .ifNoneMatch("*")
//                    .retrieve().onStatus(
//                            HttpStatus.SC_OK::equals,
//                            response -> response.bodyToMono(String.class).map(Exception::new));
//
//
//            WebClient
//                    .builder()
//                    .build()
//                    .post()
//                    .uri("/some-resource")
//                    .retrieve()
//                    .onStatus(
//                            HttpStatus.INTERNAL_SERVER_ERROR::equals,
//                            response -> response.bodyToMono(String.class).map(Exception::new))
//
//
//
//            Mono<String> messageResponse = headersSpec.exchangeToMono(response -> response.bodyToMono(String.class));
//            String response = messageResponse.block();
//            log.info("Response body: {}", response);
//            log.info("header: {}", headersSpec);
//            return response;
//        } catch (Exception e) {
//            log.info("Error encountered connecting to " + url + " for this reason " + e);
//            throw new ServerErrorException("Internal server occurred");
//        }
//    }

}
