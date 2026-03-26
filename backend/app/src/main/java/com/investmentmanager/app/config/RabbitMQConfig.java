package com.investmentmanager.app.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TRADINGNOTE_EXCHANGE = "tradingnote.exchange";
    public static final String TRADINGNOTE_CREATED_QUEUE = "tradingnote.created.queue";
    public static final String TRADINGNOTE_CREATED_ROUTING_KEY = "tradingnote.created";

    public static final String PORTFOLIOEVENT_EXCHANGE = "portfolioevent.exchange";
    public static final String PORTFOLIOEVENT_PROCESSED_QUEUE = "portfolioevent.processed.queue";
    public static final String PORTFOLIOEVENT_PROCESSED_ROUTING_KEY = "portfolioevent.processed";
    public static final String PORTFOLIOEVENT_IMPACT_QUEUE = "portfolioevent.impact.queue";
    public static final String PORTFOLIOEVENT_IMPACT_ROUTING_KEY = "portfolioevent.impact.created";

    public static final String ASSETPOSITION_EXCHANGE = "assetposition.exchange";
    public static final String ASSETPOSITION_DLQ = "assetposition.calculated.dlq";
    public static final String ASSETPOSITION_DLQ_ROUTING_KEY = "assetposition.calculated.dlq";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // --- TradingNote exchange/queue/binding ---

    @Bean
    public TopicExchange tradingNoteExchange() {
        return new TopicExchange(TRADINGNOTE_EXCHANGE);
    }

    @Bean
    public Queue tradingNoteCreatedQueue() {
        return new Queue(TRADINGNOTE_CREATED_QUEUE, true);
    }

    @Bean
    public Binding tradingNoteCreatedBinding(Queue tradingNoteCreatedQueue,
                                              TopicExchange tradingNoteExchange) {
        return BindingBuilder.bind(tradingNoteCreatedQueue)
                .to(tradingNoteExchange)
                .with(TRADINGNOTE_CREATED_ROUTING_KEY);
    }

    // --- PortfolioEvent exchange/queue/binding ---

    @Bean
    public TopicExchange portfolioEventExchange() {
        return new TopicExchange(PORTFOLIOEVENT_EXCHANGE);
    }

    @Bean
    public Queue portfolioEventProcessedQueue() {
        return new Queue(PORTFOLIOEVENT_PROCESSED_QUEUE, true);
    }

    @Bean
    public Binding portfolioEventProcessedBinding(Queue portfolioEventProcessedQueue,
                                                 TopicExchange portfolioEventExchange) {
        return BindingBuilder.bind(portfolioEventProcessedQueue)
                .to(portfolioEventExchange)
                .with(PORTFOLIOEVENT_PROCESSED_ROUTING_KEY);
    }

    @Bean
    public Queue portfolioEventImpactQueue() {
        return new Queue(PORTFOLIOEVENT_IMPACT_QUEUE, true);
    }

    @Bean
    public Binding portfolioEventImpactBinding(Queue portfolioEventImpactQueue,
                                               TopicExchange portfolioEventExchange) {
        return BindingBuilder.bind(portfolioEventImpactQueue)
                .to(portfolioEventExchange)
                .with(PORTFOLIOEVENT_IMPACT_ROUTING_KEY);
    }

    // --- AssetPosition exchange/queue/binding (DLQ) ---

    @Bean
    public TopicExchange assetPositionExchange() {
        return new TopicExchange(ASSETPOSITION_EXCHANGE);
    }

    @Bean
    public Queue assetPositionDlq() {
        return new Queue(ASSETPOSITION_DLQ, true);
    }

    @Bean
    public Binding assetPositionDlqBinding(Queue assetPositionDlq,
                                           TopicExchange assetPositionExchange) {
        return BindingBuilder.bind(assetPositionDlq)
                .to(assetPositionExchange)
                .with(ASSETPOSITION_DLQ_ROUTING_KEY);
    }
}
