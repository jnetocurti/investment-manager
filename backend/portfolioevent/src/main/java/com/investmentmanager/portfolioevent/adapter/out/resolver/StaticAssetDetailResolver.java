package com.investmentmanager.portfolioevent.adapter.out.resolver;

import com.investmentmanager.commons.domain.model.AssetType;
import com.investmentmanager.portfolioevent.domain.port.out.AssetDetailResolverPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.investmentmanager.commons.domain.model.AssetType.*;

/**
 * Implementação estática do resolver de detalhes de ativos.
 * Mapeia descrições de notas de corretagem para ticker + tipo.
 */
@Slf4j
@Component
class StaticAssetDetailResolver implements AssetDetailResolverPort {

    private static final Map<String, AssetDetail> ASSET_MAP = Map.ofEntries(
            // Ações
            entry("BRADESCO ON EJ N1",      "BBDC3",  STOCKS_BRL),
            entry("BRADESCO ON N1",          "BBDC3",  STOCKS_BRL),
            entry("ITAUUNIBANCO ON ED N1",   "ITUB3",  STOCKS_BRL),
            entry("ITAUUNIBANCO ON N1",      "ITUB3",  STOCKS_BRL),
            entry("ITAUUNIBANCO ON EJ N1",   "ITUB3",  STOCKS_BRL),
            entry("ENGIE BRASIL ON NM",      "EGIE3",  STOCKS_BRL),
            entry("ENGIE BRASIL ON",         "EGIE3",  STOCKS_BRL),
            entry("PETR4 PN N2",             "PETR4",  STOCKS_BRL),
            entry("PETR4 PN EDR N2",         "PETR4",  STOCKS_BRL),
            entry("PETR4 PN ATZ N2",         "PETR4",  STOCKS_BRL),
            entry("MGLU3 ON NM",             "MGLU3",  STOCKS_BRL),
            entry("MGLU3 ON EG NM",             "MGLU3",  STOCKS_BRL),

            // Fundos Imobiliários
            entry("HSML11 CI",   "HSML11", REAL_ESTATE_FUND_BRL),
            entry("MALL11 CI",   "MALL11", REAL_ESTATE_FUND_BRL),
            entry("FLMA11 CI",   "FLMA11", REAL_ESTATE_FUND_BRL),
            entry("XPLG11 CI",   "XPLG11", REAL_ESTATE_FUND_BRL),
            entry("FOFT11 CI",   "FOFT11", REAL_ESTATE_FUND_BRL),
            entry("SDIP11 CI",   "SDIP11", REAL_ESTATE_FUND_BRL),
            entry("BCFF11 CI",   "BCFF11", REAL_ESTATE_FUND_BRL),
            entry("HFOF11 CI",   "HFOF11", REAL_ESTATE_FUND_BRL),
            entry("VISC11 CI",   "VISC11", REAL_ESTATE_FUND_BRL),
            entry("VILG11 CI",   "VILG11", REAL_ESTATE_FUND_BRL),
            entry("BRCR11 CI",   "BRCR11", REAL_ESTATE_FUND_BRL),
            entry("GGRC11 CI",   "GGRC11", REAL_ESTATE_FUND_BRL),
            entry("JSRE11 CI",   "JSRE11", REAL_ESTATE_FUND_BRL),
            entry("HGRE11 CI",   "HGRE11", REAL_ESTATE_FUND_BRL),
            entry("KNRI11 CI",   "KNRI11", REAL_ESTATE_FUND_BRL),
            entry("KNRI12 DM 162,74",   "KNRI12", REAL_ESTATE_FUND_BRL),
            entry("CPTS11 CI",   "CPTS11", REAL_ESTATE_FUND_BRL),
            entry("CPTS11 CI ERS", "CPTS11", REAL_ESTATE_FUND_BRL),
            entry("CPTS11 CI EB", "CPTS11", REAL_ESTATE_FUND_BRL),
            entry("MXRF11 CI",   "MXRF11", REAL_ESTATE_FUND_BRL),
            entry("MXRF12 DM 10,29",   "MXRF12", REAL_ESTATE_FUND_BRL),
            entry("MXRF12 DM 10,36",   "MXRF12", REAL_ESTATE_FUND_BRL),
            entry("HGBS11 CI",   "HGBS11", REAL_ESTATE_FUND_BRL),
            entry("XPML11 CI",   "XPML11", REAL_ESTATE_FUND_BRL),
            entry("BRCO11 CI",   "BRCO11", REAL_ESTATE_FUND_BRL),
            entry("PVBI11 CI",   "PVBI11", REAL_ESTATE_FUND_BRL),
            entry("KNCR11 CI",   "KNCR11", REAL_ESTATE_FUND_BRL),
            entry("KNCR12 DM 103,54",   "KNCR12", REAL_ESTATE_FUND_BRL),
            entry("CNES11 CI",   "CNES11", REAL_ESTATE_FUND_BRL),
            entry("TEPP11 CI",   "TEPP11", REAL_ESTATE_FUND_BRL),
            entry("NDIV11 CI",   "NDIV11", EXCHANGE_TRADED_FUND_BRL),

            // ETFs
            entry("HASH11 CI",  "HASH11", EXCHANGE_TRADED_FUND_BRL),
            entry("BOVA11 CI",  "BOVA11", EXCHANGE_TRADED_FUND_BRL),

            // BDRs
            entry("AMZO34 DRN",  "AMZO34", BRAZILIAN_DEPOSITARY_RECEIPT_BRL),
            entry("NFLX34 DRN",  "NFLX34", BRAZILIAN_DEPOSITARY_RECEIPT_BRL),
            entry("ROXO34 DRN",  "ROXO34", BRAZILIAN_DEPOSITARY_RECEIPT_BRL),
            entry("XP INC DR1",  "XPBR31", BRAZILIAN_DEPOSITARY_RECEIPT_BRL)
            );

    @Override
    public AssetDetail resolve(String assetDescription) {
        AssetDetail detail = ASSET_MAP.get(assetDescription);
        if (detail == null) {
            log.warn("Ativo não mapeado para descrição: '{}'", assetDescription);
            return new AssetDetail(assetDescription, null);
        }
        return detail;
    }

    private static Map.Entry<String, AssetDetail> entry(String description, String ticker, AssetType type) {
        return Map.entry(description, new AssetDetail(ticker, type));
    }
}
