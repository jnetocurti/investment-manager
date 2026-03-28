package com.investmentmanager.tradingnote.adapter.out.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extrator de notas de corretagem da Clear Corretora (Grupo XP).
 *
 * Layout Clear (PDFBox text extraction):
 * - Operações: "1-BOVESPA C FRACIONARIO ENGIE BRASIL ON NM 1 42,29 42,29 D"
 *   Formato: {bolsa} {C/V} {mercado} {especificação} {obs?} {qtd} {preço} {valor} {D/C}
 * - Resumo Financeiro: formato invertido "{valor}{descrição} {D/C}"
 *   Ex: "0,01Taxa de liquidação D"
 * - Líquido: "{valor}Líquido para {data} {D/C}"
 */
class ClearNoteExtractor implements NoteExtractor {

    private static final String CLEAR_CNPJ_PREFIX = "02.332.886";
    private static final String CLEAR_CNPJ_PREFIX_NEW = "15.107.963";
    private static final String CURRENCY = "BRL";

    // --- Identificação ---
    private static final Pattern CNPJ_PATTERN = Pattern.compile(
            "C\\.?N\\.?P\\.?J\\.?:?\\s*(?<doc>[\\d./-]+)");

    // --- Cabeçalho ---
    private static final Pattern NOTE_NUMBER_PATTERN = Pattern.compile(
            "Nr\\.?\\s*nota\\s*\\n\\s*(?<noteNumber>\\d+)");

    private static final Pattern TRADING_DATE_PATTERN = Pattern.compile(
            "Data\\s+preg[aã]o\\s*\\n\\s*(?<tradingDate>\\d{2}/\\d{2}/\\d{4})");

    // --- Operações ---
    // Captura: bolsa C/V mercado especificação quantidade preço valor D/C
    // A especificação pode ter múltiplas palavras. Usamos a quantidade (dígitos puros)
    // como âncora para separar especificação de observação.
    private static final Pattern OPERATION_PATTERN = Pattern.compile(
            "(?:1-BOVESPA|B3 RV LISTADO)\\s+(?<cv>[CV])\\s+(?:VISTA|FRACIONARIO)\\s+" +
            "(?<spec>.+?)\\s+" +
            "(?<qty>\\d+)\\s+(?<unitPrice>[\\d.,]+)\\s+(?<totalValue>[\\d.,]+)\\s+[DC]");

    // --- Resumo Financeiro (formato Clear: valor + descrição + D/C) ---
    private static final Pattern FEE_CLEAR_PATTERN = Pattern.compile(
            "(?<value>[\\d.,]+)(?<desc>Taxa\\s+de\\s+liquida[çc][aã]o|" +
            "Taxa\\s+de\\s+Registro|Emolumentos|Taxa\\s+de\\s+termo/?\\s*op[çc][õo]es|" +
            "Taxa\\s+A\\.N\\.A\\.?|Taxa\\s+Operacional|Execu[çc][aã]o|" +
            "Taxa\\s+de\\s+Cust[oó]dia|Taxa\\s+de\\s+Transfer[eê]ncia\\s+de\\s+Ativos|" +
            "Impostos)\\s*[DC]?",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern IRRF_CLEAR_PATTERN = Pattern.compile(
            "(?<value>[\\d.,]+)I\\.?R\\.?R\\.?F\\.?\\s+s/\\s+opera[çc][õo]es",
            Pattern.CASE_INSENSITIVE);

    // --- Líquido (formato Clear: valor + "Líquido para" + data + D/C) ---
    private static final Pattern SETTLEMENT_PATTERN = Pattern.compile(
            "(?<totalNote>[\\d.,]+)L[ií]quido\\s+para\\s+(?<settlementDate>\\d{2}/\\d{2}/\\d{4})\\s+(?<dc>[DC])");

    @Override
    public boolean supports(String normalizedText) {
        Matcher m = CNPJ_PATTERN.matcher(normalizedText);
        return m.find() && (m.group("doc").startsWith(CLEAR_CNPJ_PREFIX) ||
                m.group("doc").startsWith(CLEAR_CNPJ_PREFIX_NEW));
    }

    @Override
    public RawNoteData extract(String normalizedText) {
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

        return new RawNoteData("CLEAR", documentId, noteNumber, tradingDate, settlementDate,
                totalNote, CURRENCY, extractFees(normalizedText), extractOperations(normalizedText));
    }

    private List<RawNoteData.RawOperation> extractOperations(String text) {
        List<RawNoteData.RawOperation> ops = new ArrayList<>();
        Matcher m = OPERATION_PATTERN.matcher(text);
        while (m.find()) {
            String cv = m.group("cv");
            String spec = cleanAssetName(m.group("spec").trim());
            String opType = "C".equals(cv) ? "BUY" : "SELL";
            int qty = Integer.parseInt(m.group("qty"));
            ops.add(new RawNoteData.RawOperation(spec, opType, qty,
                    m.group("unitPrice"), m.group("totalValue")));
        }
        return ops;
    }

    private List<RawNoteData.RawFee> extractFees(String text) {
        List<RawNoteData.RawFee> fees = new ArrayList<>();

        Matcher m = FEE_CLEAR_PATTERN.matcher(text);
        while (m.find()) {
            String value = m.group("value");
            if (!isZeroValue(value)) {
                fees.add(new RawNoteData.RawFee(normalizeFeeDescription(m.group("desc").trim()), value));
            }
        }

        Matcher irrf = IRRF_CLEAR_PATTERN.matcher(text);
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
     * Colunas do PDF: Especificação do título | Obs. (*) | Quantidade
     * Observações possíveis: #, *, @, 2, 8, D, F, C, H, P, L, I, B, A, T, X, Y
     * Também remove sufixo fracionário (ex: PETR4F -> PETR4).
     */
    private String cleanAssetName(String name) {
        // Remove trailing observation markers (single chars: #, *, @, digits, or known codes)
        String cleaned = name.replaceAll("\\s+[#*@2-9A-Z]{1,2}$", "").trim();
        // Remove any remaining trailing # or * symbols
        cleaned = cleaned.replaceAll("[#*@]+$", "").trim();
        // Remove fractional market suffix (e.g., PETR4F -> PETR4)
        cleaned = cleaned.replaceFirst("^([A-Z]{4}\\d{1,2})F\\b", "$1");
        // Collapse multiple spaces (from PDF column alignment)
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
