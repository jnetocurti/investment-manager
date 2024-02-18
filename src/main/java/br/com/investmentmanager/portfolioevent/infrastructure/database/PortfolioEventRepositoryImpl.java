package br.com.investmentmanager.portfolioevent.infrastructure.database;

import br.com.investmentmanager.portfolioevent.domain.PortfolioEvent;
import br.com.investmentmanager.portfolioevent.domain.PortfolioEventRepository;
import br.com.investmentmanager.portfolioevent.infrastructure.database.model.PersistencePortfolioEvent;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PortfolioEventRepositoryImpl implements PortfolioEventRepository {

    private final PortfolioEventDAO portfolioEventDAO;

    @Override
    public void save(@NonNull List<PortfolioEvent> portfolioEvents) {
        portfolioEventDAO.saveAll(portfolioEvents.stream()
                .map(p -> new ModelMapper().map(p, PersistencePortfolioEvent.class))
                .toList());
    }
}
