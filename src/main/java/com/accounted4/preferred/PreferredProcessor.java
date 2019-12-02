package com.accounted4.preferred;

import com.accounted4.commons.finance.QuoteMediaQuoteDao;
import com.accounted4.commons.finance.QuoteMediaService;
import com.accounted4.commons.google.api.SheetsServiceUtil;
import com.accounted4.commons.io.FilePersistenceService;
import com.accounted4.commons.io.PersistenceService;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;



public class PreferredProcessor {


    private static final String JSON_DATA_SOURE = "/home/glenn/code/preferred/data/Preferred.json";

    private static final String ACTION_CSV = "csv";
    private static final String ACTION_TO_GOOGLE_SHEET = "toGoogleSheet";
    private static final String ACTION_UPDATE_QUOTES = "updateQuotes";

    private static final long QUOTE_SERVICE_THROTTLE_TIME_IN_MS = 20000l;


    private final PersistenceService<Preferred> persistence = new FilePersistenceService<>(Preferred.class);
    //private final AlphaVantageService quoteService = new AlphaVantageService();
    private final QuoteMediaService quoteService = new QuoteMediaService();


    public static void main(String[] args) throws IOException, InterruptedException, GeneralSecurityException {

        //final String action = args[0];
        final String action = ACTION_TO_GOOGLE_SHEET;

        PreferredProcessor processor = new PreferredProcessor();
        processor.process(ACTION_UPDATE_QUOTES);
        processor.process(ACTION_TO_GOOGLE_SHEET);

    }


    private void process(String action) throws IOException, GeneralSecurityException {


        List<Preferred> preferreds = persistence.loadFromJson(JSON_DATA_SOURE);

        switch (action) {


            case ACTION_CSV:
                toCsv(preferreds);
                break;


            case ACTION_TO_GOOGLE_SHEET:
                toGoogleSheet(preferreds);
                break;
                

            case ACTION_UPDATE_QUOTES:
                persistence.backup(JSON_DATA_SOURE);
                addQuotes(preferreds);
                persistence.persistAsJson(preferreds, JSON_DATA_SOURE);
                toCsv(preferreds);
                break;


            default:
                System.out.println("Unknown action: " + action);
                System.out.println(String.format("Available actions: %s | %s", ACTION_CSV, ACTION_UPDATE_QUOTES));

        }


    }




    private TreeSet<Preferred> sortBySymbol(List<Preferred> preferreds) {
        Comparator<Preferred> byDescription = Comparator.comparing(p -> p.getSymbol());
        return preferreds.stream()
                .collect(Collectors.toCollection(() -> new TreeSet<>(byDescription)));
    }



    private void addQuotes(List<Preferred> preferreds) {

        List<String> lookupList = getLookupList();
        
        for (Preferred preferred : preferreds) {
            
            String symbol = preferred.getSymbol();
            
//            if (lookupList.contains(symbol)) {
//                continue;
//            }
            
            try {
                QuoteMediaQuoteDao quote = quoteService.getQuote(symbol);
                if (isGoodQuote(quote, preferred.getSymbol())) {
                    preferred.setLastPrice(quote.getClose());
                    preferred.setLastPriceDate(LocalDate.parse(quote.getDate()));
                    System.out.println("Updated quote for: " + symbol);
                } else {
                    System.out.println("Failed to retrieve good quote for: " + symbol);
                }
            } catch (IOException ioe) {
                System.out.println(symbol + ": " + ioe.getMessage());
            }
            
            throttle();
            
        }

    }



    private boolean isGoodQuote(QuoteMediaQuoteDao quote, String symbol) {
        if (null == quote || null == quote.getClose() ) {
            System.out.println("Skipping quote for: " + symbol);
            return false;
        }
        return true;
    }


    private void throttle() {
        try {
            Thread.sleep(QUOTE_SERVICE_THROTTLE_TIME_IN_MS);
        } catch (InterruptedException ie) {
        }
    }


    private void toCsv(List<Preferred> localPreferred) {
        localPreferred.stream().forEach(p -> System.out.println(p.toCsv()));
    }


    //private static final String DEBENTURE_SHEET = "1sT49fKDtIVzrcAiLojnAgtB1jkXCicVEEHStEOTJKoU";
    private static final String PREFERRED_SHEET = "1ZDMamvajIjQf31SUYbqoi2REKP3ZOzk80WAZ1HbPRhU";
    private static final String PASTER_START_CELL = "A4";

    private void toGoogleSheet(List<Preferred> preferreds) throws IOException, GeneralSecurityException {

        List<List<Object>> range = preferreds.stream()
                .map(d -> new ArrayList<Object>(Arrays.asList(d.toCsv().split("~"))))
                .collect(Collectors.toList());


        Sheets sheetsService = SheetsServiceUtil.getSheetsService();

        ValueRange body = new ValueRange().setValues(range);

        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(PREFERRED_SHEET, PASTER_START_CELL, body)
                .setValueInputOption("RAW")
                .execute();

    }

    
    // These work on AlphaVantage
    private List<String> getLookupList() {
        ArrayList<String> list = new ArrayList<>();
        list.add("BAM.PF.E");
        list.add("BAM.PF.F");
        list.add("BAM.PF.G");
        list.add("CF.PR.A");
        list.add("CWB.PR.B");
        list.add("EMA.PR.F");
        list.add("ENB.PF.A");
        list.add("ENB.PF.U");
        list.add("ENB.PF.V");
        list.add("ENB.PF.C");
        list.add("ENB.PF.E");
        list.add("ENB.PF.G");
        list.add("IAG.PR.G");
        list.add("MFC.PR.J");
        list.add("RY.PR.J");
        list.add("RY.PR.M");
        list.add("TD.PF.A");
        list.add("TD.PF.C");
        list.add("TD.PF.D");
        list.add("TD.PF.G");
        list.add("TD.PF.H");
        list.add("TD.PF.I");
        list.add("TD.PF.J");
        list.add("TD.PF.K");
        return list;
    }
    
}
