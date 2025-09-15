package commerce.eshop.core.config;

import commerce.eshop.core.util.email.properties.EmailBrandProps;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootConfiguration
@EnableConfigurationProperties(EmailBrandProps.class)
public class BrandConfig {
}
