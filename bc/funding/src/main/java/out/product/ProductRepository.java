package out.product;

import org.springframework.data.jpa.repository.JpaRepository;

import domain.product.Product;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductQueryRepository {
}
