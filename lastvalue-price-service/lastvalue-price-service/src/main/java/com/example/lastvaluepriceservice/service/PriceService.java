package com.example.lastvaluepriceservice.service;

import com.example.lastvaluepriceservice.model.BatchStatus;
import com.example.lastvaluepriceservice.model.PriceRecord;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for:
 *  - accepting producer batch uploads (start/upload/complete/cancel)
 *  - maintaining a committed price map visible to consumers
 *  - ensuring thread safety and isolation
 */
@Service
public class PriceService {

    private final Map<String, PriceRecord> committedPrices = new ConcurrentHashMap<>();
    private final Map<String, List<PriceRecord>> batchData = new ConcurrentHashMap<>();
    private final Map<String, BatchStatus> batchStatus = new ConcurrentHashMap<>();

    // --- Producer Operations ---

    public synchronized void startBatch(String batchId) {
        if (batchStatus.containsKey(batchId)) {
            throw new IllegalStateException("Batch with ID " + batchId + " already exists.");
        }

        batchData.put(batchId, new ArrayList<>());
        batchStatus.put(batchId, BatchStatus.STARTED);
    }

    public synchronized void uploadData(String batchId, List<PriceRecord> records) {
        BatchStatus status = batchStatus.get(batchId);
        if (status == null) {
            throw new IllegalStateException("Batch does not exist. Start the batch first.");
        }
        if (status != BatchStatus.STARTED) {
            throw new IllegalStateException("Cannot upload to batch in state: " + status);
        }

        batchData.get(batchId).addAll(records);
    }

    public synchronized void completeBatch(String batchId) {
        BatchStatus status = batchStatus.get(batchId);
        if (status == null) {
            throw new IllegalStateException("Batch does not exist.");
        }
        if (status != BatchStatus.STARTED) {
            throw new IllegalStateException("Batch must be STARTED to complete.");
        }

        List<PriceRecord> records = batchData.get(batchId);
        if (records != null) {
            for (PriceRecord record : records) {
                PriceRecord current = committedPrices.get(record.getInstrumentId());
                if (current == null || record.getAsOf() > current.getAsOf()) {
                    committedPrices.put(record.getInstrumentId(), record);
                }
            }
        }

        batchStatus.put(batchId, BatchStatus.COMPLETED);
    }

    public synchronized void cancelBatch(String batchId) {
        BatchStatus status = batchStatus.get(batchId);
        if (status == null) {
            throw new IllegalStateException("Batch does not exist.");
        }

        batchData.remove(batchId);
        batchStatus.put(batchId, BatchStatus.CANCELLED);
    }

    // --- Consumer Operations ---

    public PriceRecord getLastPrice(String instrumentId) {
        return committedPrices.get(instrumentId);
    }

    public Map<String, PriceRecord> getAllPricesSnapshot() {
        return Map.copyOf(committedPrices);
    }
}
