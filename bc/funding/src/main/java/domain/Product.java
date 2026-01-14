package domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class Product {
    @Id
    private Long id;
	private Long userId;
	private String name;
	private int price;
	private int stock;
	// private ProductStatus status;

	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
}
