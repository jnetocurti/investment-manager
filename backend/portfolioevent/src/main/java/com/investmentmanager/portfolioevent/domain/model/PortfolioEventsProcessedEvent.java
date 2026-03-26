package com.investmentmanager.portfolioevent.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Evento trigger publicado no RabbitMQ após processar eventos de portfólio.
 * Contém apenas os nomes dos ativos afetados — o módulo downstream
 * lê os dados completos diretamente do MongoDB.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioEventsProcessedEvent {

    private List<AssetKey> assetKeys;
    private String brokerName;
    private String brokerDocument;
    private String sourceType;
    private String sourceReferenceId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetKey {
        private String assetName;
        private String assetType;
    }
}
