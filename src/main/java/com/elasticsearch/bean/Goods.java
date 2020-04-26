package com.elasticsearch.bean;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


@Data
@Document(indexName = "goods",type = "docs",shards = 3,replicas = 2)
public class Goods {
    /**
     *  编号
     */
    @Id
    private int id;
    /**
     *  标题
     */
    @Field(type = FieldType.Text,analyzer = "ik_max_word",store = false,index = true)
    private String title;
    /**
     * 价格
     */
    @Field(type = FieldType.Double)
    private double price;
    /**
     * 品牌
     */
    @Field(type = FieldType.Keyword)
    private String  brand;

    /**
     * 封面图片地址
     */
    @Field(type = FieldType.Keyword,index = false)
    private String images;
}
