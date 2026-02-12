package app.giftify.product.adapter.outbound.document;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Document(indexName = "products")
@Getter
public class ProductDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String name;
    @Field(type = FieldType.Text)
    private String description;
    @Field(type = FieldType.Integer)
    private int price;
    @Field(type = FieldType.Keyword)
    private String category;
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createdAt;

    public ProductDocument(String name, String description, int price, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    public ProductDocument(String id, String name, String description, int price, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }
}
