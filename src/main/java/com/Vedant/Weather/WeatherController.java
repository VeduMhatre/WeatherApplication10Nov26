package com.Vedant.Weather;

import org.springframework.web.bind.annotation.*;
import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Mono;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@CrossOrigin(origins="*")

public class WeatherController{

	private WeatherService service;
	
	public WeatherController(WeatherService service){
		this.service=service;
	}
	
	private final Map<String, Long> requestTimestamps = new ConcurrentHashMap<>();
	@PostMapping("/get-weather")
	public Mono<Map<String,String>> getWeather(@RequestParam double latitude, @RequestParam double longitude, HttpServletRequest request){


		String forwarded = request.getHeader("X-Forwarded-For");	
		String clientIp = (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
		long currentTime = System.currentTimeMillis();


		if(requestTimestamps.containsKey(clientIp)){
			long lastRequestTime = requestTimestamps.get(clientIp);
			if(currentTime - lastRequestTime < 2000){	
				Map<String, String> errorResult = new HashMap<>();
            			errorResult.put("status", "error");
           	 		errorResult.put("errmsg", "Too many requests. Please wait a moment before clicking again.");
            			return Mono.just(errorResult);
			}
		}
		requestTimestamps.put(clientIp, currentTime);
		
		if(latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180){
			Map<String, String> errorResult = new HashMap<>();
			errorResult.put("status", "error");
        		errorResult.put("errmsg", "Invalid coordinates. Latitude must be between -90 and 90, and longitude between -180 and 180.");
			return Mono.just(errorResult);
		}
		return service.getWeather(latitude,longitude);
	}

}