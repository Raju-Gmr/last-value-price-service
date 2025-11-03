package com.example.lastvaluepriceservice;

import com.example.lastvaluepriceservice.model.PriceRecord;
import com.example.lastvaluepriceservice.service.PriceService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Comprehensive unit tests for PriceService covering:
 * - Valid batch lifecycle
 * - Invalid flows (no start, double complete, cancel, etc.)
 * - Latest price retrieval logic
 * - Concurrency and timestamp ordering
 */
class PriceServiceTest {

    private PriceService service;

    @BeforeEach
    void setup() {
        service = new PriceService();
    }

    @Test
    void testValidBatchLifecycle() {
        String batchId = "B1";
        service.startBatch(batchId);

        List<PriceRecord> records = List.of(
                new PriceRecord("ABC", 3500.5, System.currentTimeMillis()),
                new PriceRecord("DEF", 1400.75, System.currentTimeMillis())
        );

        service.uploadData(batchId, records);
        service.completeBatch(batchId);

        Assertions.assertEquals(3500.5, service.getLastPrice("ABC").getPrice());
        Assertions.assertEquals(1400.75, service.getLastPrice("DEF").getPrice());
    }

    // Upload before batch start should throw exception
    @Test
    void testUploadWithoutStartingBatch() {
        String batchId = "B2";
        List<PriceRecord> records = List.of(new PriceRecord("ABC", 3500.5, System.currentTimeMillis()));
        Assertions.assertThrows(IllegalStateException.class, () -> service.uploadData(batchId, records));
    }

    // Complete before upload should not commit data
    @Test
    void testCompleteWithoutUploadingData() {
        String batchId = "B3";
        service.startBatch(batchId);
        service.completeBatch(batchId);
        Assertions.assertNull(service.getLastPrice("XYZ")); // no data uploaded
    }

    // Cancel batch should discard data
    @Test
    void testCancelBatchShouldDiscardData() {
        String batchId = "B4";
        service.startBatch(batchId);

        List<PriceRecord> records = List.of(
                new PriceRecord("ABC", 1000.0, System.currentTimeMillis())
        );
        service.uploadData(batchId, records);
        service.cancelBatch(batchId);

        Assertions.assertNull(service.getLastPrice("ABC"));
    }

    // Multiple batches in parallel (only completed ones should be visible)
    @Test
    void testMultipleBatchesParallel() {
        String b1 = "B5";
        String b2 = "B6";

        service.startBatch(b1);
        service.uploadData(b1, List.of(new PriceRecord("AAPL", 100.0, System.currentTimeMillis())));

        service.startBatch(b2);
        service.uploadData(b2, List.of(new PriceRecord("AAPL", 200.0, System.currentTimeMillis())));

        service.completeBatch(b2); // only B2 visible

        Assertions.assertEquals(200.0, service.getLastPrice("AAPL").getPrice());
    }

    // Ensure latest timestamp overwrites older one
    @Test
    void testLatestTimestampWins() {
        String b7 = "B7";
        service.startBatch(b7);

        long now = System.currentTimeMillis();
        List<PriceRecord> records = List.of(
                new PriceRecord("RELIANCE", 2500.0, now - 1000),
                new PriceRecord("RELIANCE", 2600.0, now)
        );

        service.uploadData(b7, records);
        service.completeBatch(b7);

        Assertions.assertEquals(2600.0, service.getLastPrice("RELIANCE").getPrice());
    }

    // Upload after batch completion should fail
    @Test
    void testUploadAfterBatchCompleteShouldFail() {
        String batchId = "B8";
        service.startBatch(batchId);
        service.uploadData(batchId, List.of(new PriceRecord("XYZ", 1200.0, System.currentTimeMillis())));
        service.completeBatch(batchId);

        Assertions.assertThrows(IllegalStateException.class, () ->
                service.uploadData(batchId, List.of(new PriceRecord("XYZ", 1300.0, System.currentTimeMillis()))));
    }

    // Start same batch twice should throw error
    @Test
    void testDuplicateBatchStartShouldThrowError() {
        String batchId = "B9";
        service.startBatch(batchId);
        Assertions.assertThrows(IllegalStateException.class, () -> service.startBatch(batchId));
    }

    // Get price for unknown instrument should return null
    @Test
    void testGetUnknownInstrument() {
        Assertions.assertNull(service.getLastPrice("GOOGLE"));
    }

    // Concurrency scenario (simulate multiple threads)
    @Test
    void testConcurrentBatchUpdate() throws InterruptedException {
        String batchId = "B10";
        service.startBatch(batchId);

        Runnable r1 = () -> service.uploadData(batchId, List.of(
                new PriceRecord("TSLA", 100.0, System.currentTimeMillis())));

        Runnable r2 = () -> service.uploadData(batchId, List.of(
                new PriceRecord("TSLA", 200.0, System.currentTimeMillis() + 10)));

        Thread t1 = new Thread(r1);
        Thread t2 = new Thread(r2);

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        service.completeBatch(batchId);

        Assertions.assertEquals(200.0, service.getLastPrice("TSLA").getPrice());
    }
}
