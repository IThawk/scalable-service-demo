package com.scalable.c3shardingjdbc;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.datasource")
@Getter
@Setter
public class YmlConfig {

	private Map<String, String> test0;
	private Map<String, String> test1;

}