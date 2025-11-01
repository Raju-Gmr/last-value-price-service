package com.example.lastvaluepriceservice.model;

/*
 Simple POJO representing a price for an instrument.
 Fields:
  - instrumentId : unique identifier of the instrument
  - price        : numeric price value
  - asOf         : millis representing when this price was valid
*/
public class PriceRecord {
    private String instrumentId;
    private double price;
    private long asOf;

    // Default constructor 
    public PriceRecord() {}

    // Convenience constructor for tests and quick creation
    public PriceRecord(String instrumentId, double price, long asOf) {
        this.instrumentId = instrumentId;
        this.price = price;
        this.asOf = asOf;
    }

    // Getter for instrumentId
    public String getInstrumentId() {
        return instrumentId;
    }

    // Getter for price
    public double getPrice() {
        return price;
    }

    // Getter for asOf timestamp
    public long getAsOf() {
        return asOf;
    }
}
