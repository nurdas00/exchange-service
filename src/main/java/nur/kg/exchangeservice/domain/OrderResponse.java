package nur.kg.exchangeservice.domain;

public record OrderResponse(String exchangeOrderId, String status, String idempotencyKey) {}