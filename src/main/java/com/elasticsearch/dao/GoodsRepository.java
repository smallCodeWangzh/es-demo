package com.elasticsearch.dao;

import com.elasticsearch.bean.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * ElasticsearchRepository : 第一个参数为POJO，第二个参数为 ID的类型
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Integer> {
    List<Goods> findAllByTitleOrderByPriceDesc(String title);
}
