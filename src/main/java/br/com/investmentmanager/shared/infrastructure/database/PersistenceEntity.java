package br.com.investmentmanager.shared.infrastructure.database;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Data
public abstract class PersistenceEntity {
    @Id
    private UUID id;
}
