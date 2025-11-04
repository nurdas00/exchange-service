package nur.kg.exchangeservice.runner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data @JsonIgnoreProperties(ignoreUnknown = true)
public class BybitOrdersResponse {
    private int retCode;
    private String retMsg;
    private Result result;
    private Map<String, Object> retExtInfo;
    private long time;

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private String nextPageCursor;
        private String category;
        private List<Order> list;
    }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Order {
        private String symbol;
        private String side;
        private String orderType;
        private String orderId;
        private String orderLinkId;     // ← make it String
        private String orderStatus;
        private String qty;
        private String price;
        private String avgPrice;
        private String createdTime;
        private String updatedTime;
        // optional extras that sometimes vary in type
        private String leavesQty;
        private String leavesValue;
        private String cumExecQty;
        private String cumExecValue;
        private String cumExecFee;
        private Map<String, String> cumFeeDetail;
    }
}
