package nur.kg.exchangeservice;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Map;

@Getter
@Setter
public class BybitResponse {

    private Map<String, Object> result;
    private Integer retCode;
    private String retMsg;
    private Map<String, Object> retExtInfo;
    private Long time;

    private class BybitResponseResult {
        String symbol;
        String category;
        ArrayList<String> list;
    }
}
