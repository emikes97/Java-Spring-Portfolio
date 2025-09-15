package commerce.eshop.core.util.email.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.brand")
public record EmailBrandProps(
        String companyName,
        String supportEmail,
        String baseUrl
) {}
