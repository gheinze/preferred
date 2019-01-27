package com.accounted4.preferred;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;



@Getter @Setter
public class Preferred {

    private String symbol;
    private String company;
    private BigDecimal currentRate;

    private BigDecimal lastPrice;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate lastPriceDate;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate resetDate;

    private BigDecimal resetPremium;

    private String notes;


    private static final String SEPARATOR = "~";
    
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,###.00");
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("#,###.000");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public String toCsv() {

        String discount = getDiscount();
        
        StringBuilder sb = new StringBuilder();

        sb.append(symbol).append(SEPARATOR);
        sb.append(company).append(SEPARATOR);
        sb.append(null == currentRate ? "" : PERCENT_FORMAT.format(currentRate)).append(SEPARATOR);
        sb.append(null == lastPrice ? "" : MONEY_FORMAT.format(lastPrice)).append(SEPARATOR);
        sb.append(null == lastPriceDate ? "" : DATE_FORMAT.format(lastPriceDate)).append(SEPARATOR);
        sb.append(discount).append(SEPARATOR);
        sb.append(getEffectiveRate(discount)).append(SEPARATOR);
        sb.append(null == resetDate ? "" : DATE_FORMAT.format(resetDate)).append(SEPARATOR);
        sb.append(null == resetPremium ? "" : PERCENT_FORMAT.format(resetPremium)).append(SEPARATOR);
        sb.append(null == notes ? "" : notes);

        return sb.toString();
        
    }


    private static final BigDecimal BASE_PRICE = new BigDecimal("25");
    
    
    private String getDiscount() {
        if (null == lastPrice) {
            return "";
        }
        BigDecimal discount = BASE_PRICE.subtract(lastPrice).divide(BASE_PRICE);
        return PERCENT_FORMAT.format(discount);        
    }
    

    //=IF(ISBLANK(E24),"", C24 - ((F24 - 100) /  (DAYS(E24,TODAY())/365)))
    private String getEffectiveRate(String discount) {

        if (null == currentRate || discount.length() == 0) {
            return "";
        }

        BigDecimal bdDiscount = new BigDecimal(discount);
        BigDecimal effectiveRate = currentRate.add(currentRate.multiply(bdDiscount));

        return PERCENT_FORMAT.format(effectiveRate);

    }



    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Preferred other = (Preferred) obj;
        if (!Objects.equals(this.symbol, other.symbol)) {
            return false;
        }
        return true;
    }


}
