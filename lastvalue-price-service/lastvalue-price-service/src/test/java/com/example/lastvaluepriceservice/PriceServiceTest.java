package com.example.lastvaluepriceservice;

import com.example.lastvaluepriceservice.model.PriceRecord;
import com.example.lastvaluepriceservice.service.PriceService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/*
 Basic unit test that ensures the batch lifecycle (start -> upload -> complete)
 properly commits data visible to consumers.
*/
class PriceServiceTest {

    private final PriceService service = new PriceService();

    @Test
    void testBatchLifecycle() {
        String batchId = "B1";
        service.startBatch(batchId);

        List<PriceRecord> records = List.of(
                new PriceRecord("ABC", 3500.5, System.currentTimeMillis()),
                new PriceRecord("DEF", 1400.75, System.currentTimeMillis())
        );

        service.uploadData(batchId, records);
        service.completeBatch(batchId);

        Assertions.assertNotNull(service.getLastPrice("ABC"));
        Assertions.assertEquals(1400.75, service.getLastPrice("DEF").getPrice());
    }
}
