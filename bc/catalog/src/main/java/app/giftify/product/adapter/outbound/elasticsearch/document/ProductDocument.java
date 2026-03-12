package app.giftify.product.adapter.outbound.elasticsearch.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Document(indexName = "products")
@Setting(settingPath = "/elasticsearch/product-settings.json")
@Getter
@AllArgsConstructor
public class ProductDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String sellerNickname;

    @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer"),
            otherFields = {
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer", searchAnalyzer = "ngram_analyzer"),
                    @InnerField(suffix = "jamo", type = FieldType.Text, analyzer = "jamo_analyzer")
            })
    private String name;

    @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer"),
            otherFields = {
                    @InnerField(suffix = "jamo", type = FieldType.Text, analyzer = "jamo_analyzer")
            })
    private String description;

    @Field(type = FieldType.Integer)
    private int price;
    @Field(type = FieldType.Keyword)
    private String category;
    @Field(type = FieldType.Keyword)
    private String status;
    @Field(type = FieldType.Keyword)
    private String imageKey;
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Dense_Vector, dims = 1024)
    private float[] embedding;
}
