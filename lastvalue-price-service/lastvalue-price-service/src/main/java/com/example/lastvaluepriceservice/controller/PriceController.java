package com.example.lastvaluepriceservice.controller;

import com.example.lastvaluepriceservice.model.PriceRecord;
import com.example.lastvaluepriceservice.service.PriceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/*
 REST controller exposing producer and consumer APIs.

 Producer endpoints:
  - POST /api/prices/batch/{batchId}/start
  - POST /api/prices/batch/{batchId}/upload
  - POST /api/prices/batch/{batchId}/complete
  - POST /api/prices/batch/{batchId}/cancel

 Consumer endpoints:
  - GET  /api/prices/{instrumentId}
  - GET  /api/prices
*/
@RestController
@RequestMapping("/api/prices")
public class PriceController {

    private final PriceService service;

    // Constructor injection of the service (Spring will autowire).
    public PriceController(PriceService service) {
        this.service = service;
    }

    // Start a batch
    @PostMapping("/batch/{batchId}/start")
    public String startBatch(@PathVariable String batchId) {
        service.startBatch(batchId);
        return "Batch " + batchId + " started.";
    }

    // Upload records to an existing batch (JSON array of PriceRecord)
    @PostMapping("/batch/{batchId}/upload")
    public String uploadData(@PathVariable String batchId, @RequestBody List<PriceRecord> records) {
        service.uploadData(batchId, records);
        return "Uploaded " + records.size() + " records for batch " + batchId;
    }

    // Complete and commit batch
    @PostMapping("/batch/{batchId}/complete")
    public String completeBatch(@PathVariable String batchId) {
        service.completeBatch(batchId);
        return "Batch " + batchId + " completed successfully.";
    }

    // Cancel batch
    @PostMapping("/batch/{batchId}/cancel")
    public String cancelBatch(@PathVariable String batchId) {
        service.cancelBatch(batchId);
        return "Batch " + batchId + " cancelled.";
    }

    // Get last price for an instrument
    @GetMapping("/{instrumentId}")
    public PriceRecord getLastPrice(@PathVariable String instrumentId) {
        return service.getLastPrice(instrumentId);
    }

    // Get all committed prices snapshot
    @GetMapping
    public Map<String, PriceRecord> getAllPrices() {
        return service.getAllPricesSnapshot();
    }
}
