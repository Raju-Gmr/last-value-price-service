package com.example.lastvaluepriceservice.model;

/*
 Enum tracking lifecycle of a batch upload.
 STARTED  - batch has been created and is receiving uploads
 COMPLETED- batch was atomically committed (visible to consumers)
 CANCELLED- batch was cancelled and its temporary data discarded
*/
public enum BatchStatus {
    STARTED, COMPLETED, CANCELLED
}
