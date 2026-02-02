package app.giftify.facade.vo;

import java.util.List;

public record GetOrdersResult(
        List<GetOrderResult> orderResult
){
}
