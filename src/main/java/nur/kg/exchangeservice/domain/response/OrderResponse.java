package nur.kg.exchangeservice.domain.response;

public record OrderResponse(String exchangeOrderId, String status, String idempotencyKey) {}