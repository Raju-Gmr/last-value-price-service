# Last Value Price Service (Spring Boot)

This is a simple Spring Boot project implementing an in-memory "last value" price service
that supports batch uploads from producers and consistent reads for consumers.

Run with:
  mvn spring-boot:run

Or build:
  mvn clean package
  java -jar target/lastvalue-price-service-0.0.1-SNAPSHOT.jar

APIs:
  POST /api/prices/batch/{batchId}/start
  POST /api/prices/batch/{batchId}/upload  (JSON array of PriceRecord)
  POST /api/prices/batch/{batchId}/complete
  POST /api/prices/batch/{batchId}/cancel
  GET  /api/prices/{instrumentId}
  GET  /api/prices
