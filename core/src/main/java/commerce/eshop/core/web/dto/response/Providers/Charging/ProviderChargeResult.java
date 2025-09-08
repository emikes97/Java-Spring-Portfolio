package commerce.eshop.core.web.dto.response.Providers.Charging;

public record ProviderChargeResult(String providerReference,
                                   boolean successful,
                                   String errorCode,
                                   String errorMessage) {}
