package com.accounted4.preferred;

import java.io.IOException;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

/**
 *
 * @author glenn
 */
public class PreferredsScraper {

    
    public static void main(String[] args) throws IOException {
        
        PreferredsScraper scraper = new PreferredsScraper();
        scraper.scrape();
    }

    
    private static final String SOURCE = "https://www.investingforme.com";
    
    private void scrape() throws IOException {
        
        Document doc = Jsoup.connect(SOURCE + "/data-room/rate-reset-preferred-shares").get();
        List<Node> rows = doc.getElementsByClass("data-block").get(1).child(1).child(1).childNodes();
        for (Node row : rows) {

            if (row instanceof TextNode) {
                continue;
            }

            String symbol = ((Element)(row.childNodes().get(1))).text();
        //    String symbol = ((TextNode)(row.childNodes().get(1).childNode(0).childNode(0))).text();
            String company = ((TextNode)(row.childNodes().get(3).childNode(0))).text();
            String href = row.childNodes().get(5).childNode(0).attr("href");
            System.out.println(symbol.substring(0, symbol.length() - 3) + "~" + company + "~" + SOURCE + href);
        }
// doc.getElementsByClass("data-block").get(1).child(1).child(1).childNodes().get(1).childNodes().get(1).childNode(0).childNode(0).text()
// ALA.PR.E:CA
// doc.getElementsByClass("data-block").get(1).child(1).child(1).childNodes().get(1).childNodes().get(3).text()
// AltaGas Limited
// doc.getElementsByClass("data-block").get(1).child(1).child(1).childNodes().get(1).childNodes().get(5).text()
// 5.393% Cumulative, Redeemable, Rate-Reset Preferred Shares, Series E
// doc.getElementsByClass("data-block").get(1).child(1).child(1).childNodes().get(1).childNodes().get(5).childNode(0).attr("href")
// /data-room/quote?s=ALA.PR.E

    }
    
    
}
