package app.giftify.product.adapter.outbound.elasticsearch;

import app.giftify.product.adapter.outbound.elasticsearch.document.ProductDocument;
import app.giftify.product.adapter.outbound.elasticsearch.repository.ProductEsRepository;
import app.giftify.product.adapter.outbound.jpa.ProductMapper;
import app.giftify.product.adapter.outbound.jpa.repository.ProductRepository;
import app.giftify.product.application.port.out.ProductEsPort;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.replica.member.Member;
import app.giftify.replica.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductEsAdapter implements ProductEsPort {

    private final ProductEsRepository productEsRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final MemberRepository memberRepository;

    @Override
    public void save(Product product) {
        String sellerNickname = memberRepository.findById(product.getSellerId())
                .map(Member::getNickname)
                .orElse("UNKNOWN");
        ProductDocument document = ProductEsMapper.toDocument(product, sellerNickname);

        productEsRepository.save(document);
    }

    @Override
    public int syncAll() {
        List<Product> products = productRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE || p.getStatus() == ProductStatus.INACTIVE)
                .map(productMapper::toDomain)
                .toList();

        Map<Long, String> sellerNicknameMap = memberRepository.findAll().stream()
                .collect(Collectors.toMap(Member::getId, Member::getNickname));

        List<ProductDocument> documents = products.stream().map(
                product -> {
                    String sellerNickname = sellerNicknameMap.getOrDefault(product.getSellerId(), "UNKNOWN");
                    return ProductEsMapper.toDocument(product, sellerNickname);
                }
        ).toList();

        productEsRepository.saveAll(documents);
        return documents.size();
    }
}
