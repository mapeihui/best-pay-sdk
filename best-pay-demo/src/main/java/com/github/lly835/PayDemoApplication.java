package com.github.lly835;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用外置的tomcat启动
 *       默认的启动类要继承SpringBootServletInitiailzer类，并复写configure()方法。
 */
@SpringBootApplication
public class PayDemoApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(PayDemoApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return super.configure(builder);
	}

}
