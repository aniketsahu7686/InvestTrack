package com.investtrack.marketdata.kite;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Kite Connect API integration.
 * Values are loaded from application.yml under the 'kite' prefix.
 */
@Configuration
@ConfigurationProperties(prefix = "kite")
@Getter
@Setter
public class KiteConfig {

    /**
     * Kite Connect API key (obtained from Kite developer portal).
     */
    private String apiKey;

    /**
     * Kite Connect API secret (obtained from Kite developer portal).
     */
    private String apiSecret;

    /**
     * Redirect URL for Kite OAuth callback.
     */
    private String redirectUrl;

    /**
     * Kite login URL for initiating OAuth flow.
     */
    private String loginUrl;

    /**
     * Generates the full Kite login URL with the API key parameter.
     *
     * @return the Kite login URL with api_key query parameter
     */
    public String getFullLoginUrl() {
        return loginUrl + "?api_key=" + apiKey + "&v=3";
    }
}
