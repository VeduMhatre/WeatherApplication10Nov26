package com.Vedant.Weather;

import org.springframework.stereotype.*;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.*;
import org.springframework.cache.annotation.Cacheable;

@Service
public class WeatherService {
	
	WebClient wc = WebClient.create();

	@Cacheable(value = "weatherCache", key = "#latitude + '_' + #longitude")
	public Mono<Map<String,String>> getWeather(double latitude,double longitude){
		String url = "https://api.open-meteo.com/v1/forecast?latitude="
			+latitude
			+"&longitude="
			+longitude
			+"&current=temperature_2m";

		return wc.get()
			.uri(url)
			.retrieve()
			.bodyToMono(HashMap.class)
			.flatMap(response -> {
				System.out.println(response);
				
				Map current = (Map)response.get("current");
				if(current==null){
					Map<String,String> result = new HashMap<>();
					result.put("status","error");
					result.put("errmsg","Weather Not Found");
					return Mono.just(result);
				}	
				else{
					Double temperature = ((Number)current.get("temperature_2m")).doubleValue();
					Map<String, String> result = new HashMap<>();
					result.put("status","success");
					result.put("temperature", String.valueOf(temperature));
					return Mono.just(result);
				}		
			})
			.onErrorResume(err -> {
				System.out.println("Third-party API crashed: " + err.getMessage());
				Map<String,String> result = new HashMap<>();
				result.put("status","error");
				result.put("errmsg", "Weather service is currently unavailable. Please try again later.");
				return Mono.just(result);
			});

	}

}