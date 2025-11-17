package com.webstore.service;

import com.webstore.dto.request.webhook.WebhookRequestDto;

public interface WebhookService {
    void processIncomingMessage(WebhookRequestDto webhookData);
}
