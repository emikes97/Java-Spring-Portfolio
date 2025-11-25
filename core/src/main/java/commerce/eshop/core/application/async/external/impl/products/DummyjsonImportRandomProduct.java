package commerce.eshop.core.application.async.external.impl.products;

import commerce.eshop.core.application.async.external.contracts.ImportClientProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class DummyjsonImportRandomProduct implements ImportClientProduct {

    // == Fields ==
    private static final String RANDOM_PRODUCT_URL = "https://dummyjson.com/products/";
    private final RestTemplate restTemplate;

    // == Constructors ==
    @Autowired
    public DummyjsonImportRandomProduct(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // == Public methods ==

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getProduct() {
        int randomId = ThreadLocalRandom.current().nextInt(1, 194);
        String url = RANDOM_PRODUCT_URL + randomId;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null){
                log.info("DummyJSON random product imported successfully");
                return (Map<String, Object>) response.getBody();
            }

            throw new IllegalStateException("DummyJSON returned empty or invalid response");

        } catch (RestClientException exception){
            log.error("Failed to fetch product from DummyJSON: {}", exception.getMessage());
            throw new IllegalStateException("External product import failed", exception);
        }
    }
}
