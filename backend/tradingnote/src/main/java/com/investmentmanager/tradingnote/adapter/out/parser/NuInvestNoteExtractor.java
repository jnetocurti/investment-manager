package com.investmentmanager.tradingnote.adapter.out.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extrator de notas de corretagem da NuInvest / Easynvest.
 *
 * Layout NuInvest (PDFBox text extraction):
 * - Cabeçalho: "Mercado C/V Tipo de Mercado Especificação do Título Observação Quantidade ..."
 * - Operação: "BOVESPA C VISTA KNCR11 CI # 2 97,20 194,40 D"
 *   Formato: {bolsa} {C/V} {mercado} {especificação} {obs?} {qtd} {preço} {valor} {D/C}
 * - Resumo Financeiro: formato padrão "{descrição} {-?valor}"
 *   Ex: "Taxa de Liquidação -0,40"
 * - Líquido: "Líquido para {data} {-?valor}"
 *
 * Variações ao longo dos anos:
 * - 2019: Easynvest, mercado D2S
 * - 2021+: NuInvest, mercado VISTA/FRACIONARIO
 * - 2025: Observações @, ER, ES; IRRF com formato "Base X.XXX,XX Y,YY"
 */
class NuInvestNoteExtractor implements NoteExtractor {

    private static final String NUINVEST_CNPJ_PREFIX = "62.169.875";
    private static final String CURRENCY = "BRL";

    // --- Identificação ---
    private static final Pattern CNPJ_PATTERN = Pattern.compile(
            "CNPJ:\\s*(?<doc>[\\d./-]+)");

    // --- Cabeçalho ---
    private static final Pattern NOTE_NUMBER_PATTERN = Pattern.compile(
            "N[uú]mero\\s+da\\s+nota\\s*\\n\\s*(?<noteNumber>\\d+)");

    private static final Pattern TRADING_DATE_PATTERN = Pattern.compile(
            "Data\\s+Preg[aã]o\\s*\\n\\s*(?<tradingDate>\\d{2}/\\d{2}/\\d{4})");

    // --- Operações ---
    // Captura: bolsa C/V mercado especificação {obs?} quantidade preço valor D/C
    // Mercados: VISTA, FRACIONARIO, D2S (EasyInvest 2019-2021)
    // Quantidade pode ter separador de milhar (ex: 2.280 = 2280)
    private static final Pattern OPERATION_PATTERN = Pattern.compile(
            "BOVESPA\\s+(?<cv>[CV])\\s+(?:VISTA|FRACIONARIO|D2S)\\s+" +
            "(?<spec>.+?)\\s+" +
            "(?<qty>\\d[\\d.]*)\\s+(?<unitPrice>\\d[\\d.,]*)\\s+(?<totalValue>\\d[\\d.,]*)\\s+[DC]");

    // --- Resumo Financeiro (formato NuInvest: descrição + valor) ---
    // Seção "Resumo Financeiro" contém blocos: Clearing (CBLC), Bolsa, Corretagem/Despesas
    // Taxas padrão B3: Liquidação, Registro, Emolumentos, Termo/Opções, A.N.A.
    // Taxas corretora: Corretagem, ISS, Outras
    private static final Pattern FEE_PATTERN = Pattern.compile(
            "(?<desc>Taxa\\s+de\\s+Liquida[çc][aã]o(?:/CCP)?|" +
            "Taxa\\s+de\\s+Registro|" +
            "Emolumentos|" +
            "Taxa\\s+de\\s+Termo\\s*/\\s*Op[çc][õo]es|" +
            "Taxa\\s+A\\.N\\.A\\.?|" +
            "Taxa\\s+de\\s+Transfer[eê]ncia\\s+de\\s+Ativos|" +
            "Corretagem|" +
            "ISS[^\\d]*?|" +
            "Outras)\\s+[-]?(?<value>[\\d.,]+)",
            Pattern.CASE_INSENSITIVE);

    // IRRF tem formato especial: "I.R.R.F. s/ operações. Base {base} {valor}"
    // O valor da base vem antes do valor real do imposto
    private static final Pattern IRRF_PATTERN = Pattern.compile(
            "I\\.R\\.R\\.F\\.?\\s+s/\\s+opera[çc][õo]es\\.?\\s+Base\\s+[\\d.,]+\\s+(?<value>[\\d.,]+)",
            Pattern.CASE_INSENSITIVE);

    // --- Líquido ---
    private static final Pattern SETTLEMENT_PATTERN = Pattern.compile(
            "L[ií]quido\\s+para\\s+(?<settlementDate>\\d{2}/\\d{2}/\\d{4})\\s+[-]?(?<totalNote>[\\d.,]+)");

    @Override
    public boolean supports(String normalizedText) {
        Matcher m = CNPJ_PATTERN.matcher(normalizedText);
        return m.find() && m.group("doc").startsWith(NUINVEST_CNPJ_PREFIX);
    }

    @Override
    public RawNoteData extract(String normalizedText) {
        String brokerName = detectBrokerName(normalizedText);
        String documentId = extractGroup(CNPJ_PATTERN, normalizedText, "doc");
        String noteNumber = extractGroup(NOTE_NUMBER_PATTERN, normalizedText, "noteNumber");
        String tradingDate = extractGroup(TRADING_DATE_PATTERN, normalizedText, "tradingDate");

        String settlementDate = "";
        String totalNote = "";
        Matcher sm = SETTLEMENT_PATTERN.matcher(normalizedText);
        if (sm.find()) {
            settlementDate = sm.group("settlementDate");
            totalNote = sm.group("totalNote");
        }

        return new RawNoteData(brokerName, documentId, noteNumber, tradingDate, settlementDate,
                totalNote, CURRENCY, extractFees(normalizedText), extractOperations(normalizedText));
    }

    private String detectBrokerName(String text) {
        if (text.contains("NuInvest") || text.contains("Nu Investimentos")) return "NUINVEST";
        if (text.contains("Easynvest") || text.contains("EasyInvest")) return "EASYNVEST";
        return "NUINVEST";
    }

    private List<RawNoteData.RawOperation> extractOperations(String text) {
        List<RawNoteData.RawOperation> ops = new ArrayList<>();
        Matcher m = OPERATION_PATTERN.matcher(text);
        while (m.find()) {
            String cv = m.group("cv");
            String spec = cleanAssetName(m.group("spec").trim());
            String opType = "C".equals(cv) ? "BUY" : "SELL";
            int qty = Integer.parseInt(m.group("qty").replace(".", ""));
            ops.add(new RawNoteData.RawOperation(spec, opType, qty,
                    m.group("unitPrice"), m.group("totalValue")));
        }
        return ops;
    }

    private List<RawNoteData.RawFee> extractFees(String text) {
        List<RawNoteData.RawFee> fees = new ArrayList<>();

        Matcher m = FEE_PATTERN.matcher(text);
        while (m.find()) {
            String value = m.group("value");
            if (!isZeroValue(value)) {
                fees.add(new RawNoteData.RawFee(normalizeFeeDescription(m.group("desc").trim()), value));
            }
        }

        Matcher irrf = IRRF_PATTERN.matcher(text);
        if (irrf.find()) {
            String value = irrf.group("value");
            if (!isZeroValue(value)) {
                fees.add(new RawNoteData.RawFee("I.R.R.F.", value));
            }
        }

        return fees;
    }

    /**
     * Remove da especificação do título os marcadores de observação.
     * Colunas do PDF: Especificação do Título | Observação | Quantidade
     * Observações possíveis: #, *, @, ER (exercício de resgate), ES (exercício de subscrição),
     * e combinações como @#, @ER, etc.
     * Também remove sufixo fracionário (ex: PETR4F -> PETR4).
     */
    private String cleanAssetName(String name) {
        // Remove trailing observation markers: @, #, *, ER, ES and combinations
        String cleaned = name.replaceAll("\\s+[#*@]+(?:\\s*[#*@]*)*$", "").trim();
        cleaned = cleaned.replaceAll("\\s+(?:ER|ES)$", "").trim();
        // Remove any remaining trailing single-char observation codes
        cleaned = cleaned.replaceAll("\\s+[#*@]$", "").trim();
        // Remove fractional market suffix (e.g., PETR4F -> PETR4, MGLU3F -> MGLU3)
        cleaned = cleaned.replaceFirst("^([A-Z]{4}\\d{1,2})F\\b", "$1");
        // Collapse multiple spaces
        cleaned = cleaned.replaceAll("\\s{2,}", " ");
        return cleaned.trim();
    }

    private String normalizeFeeDescription(String desc) {
        return desc.replaceAll("\\s+", " ").trim();
    }

    private boolean isZeroValue(String value) {
        return value.replace(".", "").replace(",", "").matches("^0+$");
    }

    private String extractGroup(Pattern pattern, String text, String group) {
        Matcher m = pattern.matcher(text);
        return m.find() ? m.group(group) : "";
    }
}
