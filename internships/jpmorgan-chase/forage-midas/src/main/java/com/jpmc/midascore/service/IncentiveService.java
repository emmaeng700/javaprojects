package com.jpmc.midascore.service;

import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IncentiveService {

    private static final Logger logger = LoggerFactory.getLogger(IncentiveService.class);
    private static final String INCENTIVE_API_URL = "http://localhost:8080/incentive";

    private final RestTemplate restTemplate;

    public IncentiveService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public float getIncentive(Transaction transaction) {
        try {
            IncentiveResponse response = restTemplate.postForObject(
                    INCENTIVE_API_URL, transaction, IncentiveResponse.class);
            if (response != null) {
                return response.getAmount();
            }
        } catch (Exception e) {
            logger.debug("Incentive API not available: {}", e.getMessage());
        }
        return 0f;
    }

    static class IncentiveResponse {
        private float amount;

        public float getAmount() {
            return amount;
        }

        public void setAmount(float amount) {
            this.amount = amount;
        }
    }
}
