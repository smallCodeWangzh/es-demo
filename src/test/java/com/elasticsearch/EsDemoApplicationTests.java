package com.elasticsearch;

import com.elasticsearch.bean.Goods;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EsDemoApplicationTests {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Test
    public void createIndex() {
        // 传入class对象，根据配置的@Document信息创建索引
        elasticsearchTemplate.createIndex(Goods.class);
        // 根据字符串创建索引
        //elasticsearchTemplate.createIndex("goods");
    }

    @Test
    public void createMapping() {
        elasticsearchTemplate.putMapping(Goods.class);
    }

    @Test
    public void deleteMapping() {
        elasticsearchTemplate.deleteIndex(Goods.class);
    }
}
