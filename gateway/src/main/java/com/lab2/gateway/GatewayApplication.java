package com.lab2.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@SpringBootApplication
@EnableZuulProxy
@EnableEurekaClient
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	@Component
	public class MyFallback implements FallbackProvider {

		@Override
		public String getRoute(){
			return "*" ;
		}

		@Override
		public ClientHttpResponse fallbackResponse(String route, Throwable cause){
			return new ClientHttpResponse(){
				@Override
				public HttpStatus getStatusCode() throws IOException{
					return HttpStatus.OK;
				}
				@Override
				public int getRawStatusCode() throws IOException{
					return 200;
				}
				@Override
				public String getStatusText() throws IOException{
					return "OK";
				}
				@Override
				public void close(){

				}

				@Override
				public InputStream getBody() throws IOException{
					String msg ="Service unavailable";
					return new ByteArrayInputStream(msg.getBytes());
				}

				@Override
				public HttpHeaders getHeaders(){
					HttpHeaders httpHeaders = new HttpHeaders();
					MediaType mediaType = new MediaType("application","json", Charset.forName("utf-8"));
					httpHeaders.setContentType(mediaType);
					return httpHeaders;
				}

			};
		}
	}

}
