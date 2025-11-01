package com.example.lastvaluepriceservice.service;

import com.example.lastvaluepriceservice.model.BatchStatus;
import com.example.lastvaluepriceservice.model.PriceRecord;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*
 Service responsible for:
  - accepting producer batch uploads (start/upload/complete/cancel)
  - maintaining a committed price map that consumers read from
  - providing a read-snapshot view (immutable copy of committed map)

 This implementation uses in-memory concurrent maps and synchronized methods
 to simulate atomic batch commits and read isolation (consumers only see data
 after a batch completes).
*/
@Service
public class PriceService {

    // Holds the latest committed price per instrument (visible to consumers)
    private final Map<String, PriceRecord> committedPrices = new ConcurrentHashMap<>();

    // Temporary storage for per-batch uploads. Keyed by batchId.
    private final Map<String, List<PriceRecord>> batchData = new ConcurrentHashMap<>();

    // Tracks status (STARTED / COMPLETED / CANCELLED) for each batch.
    private final Map<String, BatchStatus> batchStatus = new ConcurrentHashMap<>();

    // --- Producer Operations ---

    // Start a new batch. synchronized to prevent races when multiple producers touch batch metadata.
    public synchronized void startBatch(String batchId) {
        // Initialize batch's temporary list and mark status started.
        batchData.put(batchId, new ArrayList<>());
        batchStatus.put(batchId, BatchStatus.STARTED);
    }

    // Upload data for an active batch. Multiple uploads can append to the batch list.
    public synchronized void uploadData(String batchId, List<PriceRecord> records) {
        // Validate that batch exists and is in STARTED state.
        if (batchStatus.get(batchId) != BatchStatus.STARTED) {
            throw new IllegalStateException("Batch is not active.");
        }
        // Append records to the batch's temporary list.
        batchData.get(batchId).addAll(records);
    }

    // Complete the batch: atomically merge batch records into committedPrices.
    public synchronized void completeBatch(String batchId) {
        if (batchStatus.get(batchId) == BatchStatus.STARTED) {
            List<PriceRecord> records = batchData.get(batchId);
            if (records != null) {
                for (PriceRecord record : records) {
                    // For each record, update the committed map only if the incoming 'asOf' is newer.
                    PriceRecord current = committedPrices.get(record.getInstrumentId());
                    if (current == null || record.getAsOf() > current.getAsOf()) {
                        committedPrices.put(record.getInstrumentId(), record);
                    }
                }
            }
            // Mark batch as completed and keep batchData (could be removed to free memory).
            batchStatus.put(batchId, BatchStatus.COMPLETED);
        } else {
            throw new IllegalStateException("Batch must be STARTED to complete.");
        }
    }

    // Cancel a batch: remove temporary data and mark as cancelled.
    public synchronized void cancelBatch(String batchId) {
        batchData.remove(batchId);
        batchStatus.put(batchId, BatchStatus.CANCELLED);
    }

    // --- Consumer Operations ---

    // Return the last committed price for an instrument (may be null).
    public PriceRecord getLastPrice(String instrumentId) {
        return committedPrices.get(instrumentId);
    }

    // Return an immutable snapshot (consistent view) of all committed prices.
    public Map<String, PriceRecord> getAllPricesSnapshot() {
        return Map.copyOf(committedPrices);
    }
}
