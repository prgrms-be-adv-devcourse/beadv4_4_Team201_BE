package domain;

import java.time.LocalDateTime;

public class Product {
	private Long id;
	private Long userId;
	private String name;
	private int price;
	private int stock;
	// private ProductStatus status;

	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
}
