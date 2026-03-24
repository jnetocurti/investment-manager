package com.investmentmanager.assetposition.domain.port.out;

import com.investmentmanager.assetposition.domain.model.PortfolioEventData;

import java.util.List;

/**
 * Port de leitura de eventos de portfólio.
 * O domínio não sabe de onde vêm os dados — pode ser leitura direta
 * do MongoDB (monolito) ou HTTP/gRPC (microserviço).
 */
public interface PortfolioEventQueryPort {

    List<PortfolioEventData> findByAssetNameAndBrokerDocumentOrderByEventDate(
            String assetName, String brokerDocument);
}
