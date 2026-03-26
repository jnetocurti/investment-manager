package com.investmentmanager.portfolioevent.adapter.out.persistence.impact;

import com.investmentmanager.portfolioevent.domain.model.PositionImpactEvent;
import com.investmentmanager.portfolioevent.domain.port.out.PositionImpactEventRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PositionImpactEventPersistenceAdapter implements PositionImpactEventRepositoryPort {

    private final PositionImpactEventMongoRepository repository;

    @Override
    public List<PositionImpactEvent> saveAll(List<PositionImpactEvent> impacts) {
        if (impacts.isEmpty()) {
            return impacts;
        }

        List<PositionImpactEventDocument> persisted = new ArrayList<>();
        for (PositionImpactEvent impact : impacts) {
            if (existsByUniqueKey(impact.getOriginalEventId(), impact.getImpactType().name(), impact.getSequence())) {
                continue;
            }
            try {
                PositionImpactEvent deterministic = impact.toBuilder()
                        .id(buildDeterministicId(impact.getOriginalEventId(), impact.getTicker(), impact.getImpactType().name(), impact.getSequence()))
                        .build();
                persisted.add(repository.save(PositionImpactEventDocumentMapper.toDocument(deterministic)));
            } catch (DuplicateKeyException ignored) {
                // corrida de idempotência: já persistido por outro fluxo
            }
        }

        return persisted.stream().map(PositionImpactEventDocumentMapper::toDomain).toList();
    }

    @Override
    public boolean existsByUniqueKey(String originalEventId, String impactType, int sequence) {
        return repository.existsByOriginalEventIdAndImpactTypeAndSequence(originalEventId, impactType, sequence);
    }

    private String buildDeterministicId(String originalEventId, String ticker, String impactType, int sequence) {
        String value = originalEventId + ":" + ticker + ":" + impactType + ":" + sequence;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
