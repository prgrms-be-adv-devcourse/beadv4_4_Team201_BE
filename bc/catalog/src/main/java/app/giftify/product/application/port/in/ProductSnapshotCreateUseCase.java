package app.giftify.product.application.port.in;

import app.giftify.product.adapter.inbound.web.requestDto.ProductSnapshotRequestDto;
import app.giftify.product.adapter.inbound.web.responseDto.ProductSnapshotDto;

import java.util.List;

public interface ProductSnapshotCreateUseCase {
    List<ProductSnapshotDto> createProductSnapshots(ProductSnapshotRequestDto requestDto);
}
