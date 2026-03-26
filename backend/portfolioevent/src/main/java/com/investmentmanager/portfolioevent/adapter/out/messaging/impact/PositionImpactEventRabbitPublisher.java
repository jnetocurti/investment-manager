package com.investmentmanager.portfolioevent.adapter.out.messaging.impact;

import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;
import com.investmentmanager.portfolioevent.domain.port.out.PositionImpactEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PositionImpactEventRabbitPublisher implements PositionImpactEventPublisherPort {

    private static final String EXCHANGE = "portfolioevent.exchange";
    private static final String ROUTING_KEY = "portfolioevent.impact.created";
    private static final int MAX_ATTEMPTS = 3;

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishAll(List<PositionImpactEvent> impacts) {
        for (PositionImpactEvent impact : impacts) {
            publishWithRetry(impact);
        }
        if (!impacts.isEmpty()) {
            log.info("Publicados {} eventos de impacto de posição", impacts.size());
        }
    }

    private void publishWithRetry(PositionImpactEvent impact) {
        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, impact);
                return;
            } catch (RuntimeException ex) {
                lastError = ex;
                log.warn("Falha ao publicar impacto id={} tentativa {}/{}", impact.getId(), attempt, MAX_ATTEMPTS, ex);
            }
        }
        throw new IllegalStateException("Não foi possível publicar impacto após tentativas: " + impact.getId(), lastError);
    }
}
