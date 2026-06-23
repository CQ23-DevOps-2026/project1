package com.yas.product;

import com.yas.commonlibrary.config.CorsConfig;
import com.yas.commonlibrary.config.ServiceUrlConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"com.yas.product", "com.yas.commonlibrary"})
@EnableConfigurationProperties({ServiceUrlConfig.class, CorsConfig.class})
public class ProductApplication {

    public static void main(String[] args) {
        // CI Note: Test Jenkins SonarQube connection and analysis
        // CI Note: Test Jenkins SonarQube in VPS
        SpringApplication.run(ProductApplication.class, args);
    }

}
