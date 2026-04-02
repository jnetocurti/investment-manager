package com.investmentmanager.assetposition.adapter.out.persistence;

import com.investmentmanager.assetposition.domain.port.out.SplitFractionMetadataPort;
import com.investmentmanager.commons.domain.model.MonetaryValue;
import com.investmentmanager.portfolioevent.adapter.out.persistence.PortfolioEventDocument;
import com.investmentmanager.portfolioevent.adapter.out.persistence.PortfolioEventMongoRepository;
import org.springframework.stereotype.Component;

@Component
public class SplitFractionMetadataPersistenceAdapter implements SplitFractionMetadataPort {

    private static final String PENDING_SETTLEMENT = "PENDING_SETTLEMENT";

    private final PortfolioEventMongoRepository portfolioEventMongoRepository;

    public SplitFractionMetadataPersistenceAdapter(PortfolioEventMongoRepository portfolioEventMongoRepository) {
        this.portfolioEventMongoRepository = portfolioEventMongoRepository;
    }

    @Override
    public void updateSplitFractionMetadata(String splitEventId,
                                            MonetaryValue splitFractionResidualBookValue,
                                            String splitFractionSourceReferenceId) {
        if (splitEventId == null || splitEventId.isBlank() || splitFractionResidualBookValue == null) {
            return;
        }

        portfolioEventMongoRepository.findById(splitEventId).ifPresent(splitEvent -> {
            PortfolioEventDocument.MetadataDocument metadata = splitEvent.getMetadata() != null
                    ? splitEvent.getMetadata()
                    : new PortfolioEventDocument.MetadataDocument();

            metadata.setSplitFractionResidualBookValue(splitFractionResidualBookValue.toBigDecimal());
            metadata.setSplitFractionFlowStatus(PENDING_SETTLEMENT);
            metadata.setSplitFractionSourceReferenceId(splitFractionSourceReferenceId);
            splitEvent.setMetadata(metadata);
            portfolioEventMongoRepository.save(splitEvent);
        });
    }
}
