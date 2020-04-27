package com.elasticsearch;

import com.elasticsearch.bean.Goods;
import com.elasticsearch.dao.GoodsRepository;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EsDemoApplicationTests {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private GoodsRepository goodsRepository;


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
    public void deleteIndex() {
        elasticsearchTemplate.deleteIndex(Goods.class);
    }

    @Test
    public void createDocument() {
        Goods goods = new Goods();
        goods.setId(1);
        goods.setTitle("小米10");
        goods.setBrand("小米");
        goods.setPrice(3200.0);
        goods.setImages("http://www.baidu.com");
        goodsRepository.save(goods);
    }

    @Test
    public void createDocumentBatch() {
        List<Goods> list = new ArrayList<>();
        for(int i = 2; i <= 11; i++) {
            Goods goods = new Goods();
            goods.setId(i);
            goods.setBrand("华为");
            goods.setImages("http://www.huawei.com/" + i + ".jpg");
            goods.setPrice(3300.0 * i);
            goods.setTitle("华为荣耀 " + i);
            list.add(goods);
        }
        goodsRepository.saveAll(list);
    }

    @Test
    public void updateDocument() {
        Goods goods = new Goods();
        goods.setId(1);
        goods.setTitle("小米11");
        goods.setBrand("小米");
        goods.setPrice(6400.0);
        goods.setImages("http://www.mi.com");
        goodsRepository.save(goods);
    }


    @Test
    public void selectAll() {
        Iterable<Goods> all = goodsRepository.findAll();
        all.forEach(t -> System.out.println(t));
    }

    @Test
    public void selectAllSort() {
        Iterable<Goods> all = goodsRepository.findAll(Sort.by(Sort.Direction.DESC,"price"));
        all.forEach(t -> System.out.println(t));
    }

    @Test
    public void selectAllPage() {
        PageRequest pageRequest = PageRequest.of(2, 3, Sort.Direction.ASC, "price");
        Iterable<Goods> all = goodsRepository.findAll(pageRequest);
        all.forEach(t -> System.out.println(t));
    }

    @Test
    public void customerQuery() {
        List<Goods> list = goodsRepository.findAllByTitleOrderByPriceDesc("荣耀");
        list.forEach(System.out :: println);
    }

    @Test
    public void customerQuery2() {
        List<Goods> list = goodsRepository.findAllByTitleOrderByPriceDesc("苹果荣耀");
        list.forEach(System.out :: println);
    }

    @Test
    public void customerQuer3() {
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("title", "苹果荣耀");
        Iterable<Goods> iterable = goodsRepository.search(queryBuilder);
        iterable.forEach(System.out::println);
    }

    @Test
    public void customerQuer4() {
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title","苹果荣耀");
        Iterable<Goods> iterable = goodsRepository.search(matchQueryBuilder);
        iterable.forEach(System.out :: println);
    }

    @Test
    public void customerQuer5() {
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title","苹果荣耀");
        PageRequest pageRequest = PageRequest.of(2, 3, Sort.Direction.DESC, "price");
        Page<Goods> page = goodsRepository.search(matchQueryBuilder,pageRequest);
        page.forEach(System.out :: println);
    }

    @Test
    public void customerQuer6() {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        // 添加查询条件
        nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("title","苹果荣耀"));
        // 添加排序
        nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
        // 过滤显示的字段
        nativeSearchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","title","price"},null));

        // 增加分页操作
        PageRequest pageRequest = PageRequest.of(2, 3);
        nativeSearchQueryBuilder.withPageable(pageRequest);

        // 查询
        Page<Goods> search = goodsRepository.search(nativeSearchQueryBuilder.build());

        search.forEach(System.out :: println);

        System.out.println("----------");
        System.out.println(search.getTotalElements()); // 总条数
        System.out.println(search.getTotalPages()); // 总页数
        System.out.println(search.getContent()); // 获取当前内容
        System.out.println(search.getNumber()); // 当前页数，页码从 0 开始
    }

    @Test
    public void aggs() {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        // 不查询任何结果，相当于设置 size = 0
        nativeSearchQueryBuilder.withSourceFilter(new FetchSourceFilter(null,null));
        // 添加聚合
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("brand_aggs").field("brand"));
        // 查询 得到分页对象
        Page<Goods> page = goodsRepository.search(nativeSearchQueryBuilder.build());
        // 将分页对象强转成 聚合分页对象(AggregationPage)
        AggregatedPage<Goods> aggregatedPage = (AggregatedPage<Goods>) page;
        // 获取所有聚合，因为聚合里面也可以嵌套聚合
      //  System.out.println(aggregatedPage.getAggregations());
        // 根据名字获取聚合
        Aggregation aggregation = aggregatedPage.getAggregation("brand_aggs");
        //System.out.println(aggregation);
        /**
         *  获取聚合里面的桶
         *      因为我们去进行聚合的时候，是根据 String 类型  brand字段去进行terms聚合
         *      所以我们需要强转成 StringTerms对象才能获取里面的桶，当然除了该类型对象以外，当然还有其他的类型对象
          */
        StringTerms stringTerms = (StringTerms) aggregation;
        // 得到所有的桶
        List<StringTerms.Bucket> buckets = stringTerms.getBuckets();
        buckets.forEach(t -> {
            System.out.print(t.getKeyAsString() + ":"); // 得到桶里面的key
            System.out.print(t.getDocCount()); // 得到桶里面的 doc_count
            System.out.println();
        });


    }

    @Test
    public void aggsTemplate() {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("brand_aaa").field("brand"));
        AggregatedPage<Goods> page = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), Goods.class);
        Aggregation aggregation = page.getAggregation("brand_aaa");
        StringTerms stringTerms = (StringTerms) aggregation;
        // 得到所有的桶
        List<StringTerms.Bucket> buckets = stringTerms.getBuckets();
        buckets.forEach(t -> {
            System.out.print(t.getKeyAsString() + ":"); // 得到桶里面的key
            System.out.print(t.getDocCount()); // 得到桶里面的 doc_count
            System.out.println();
        });
    }

    @Test
    public void metirc() {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        // 不含任何结果，相当于设置 size = 0
        nativeSearchQueryBuilder.withSourceFilter(new FetchSourceFilter(null,null));
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("brand_aggs").field("brand")
                                                .subAggregation(AggregationBuilders.avg("price_avg").field("price")));

        AggregatedPage<Goods> aggregatedPage = (AggregatedPage<Goods>) goodsRepository.search(nativeSearchQueryBuilder.build());
        /**
         *  通过kibana访问我们知道：
         *      brand_aggs 聚合里面包含了
         *             price_avg 聚合
         *      因此我们先要获取 brand_aggs 再获取 price_avg聚合
         */
        StringTerms stringTerms = (StringTerms) aggregatedPage.getAggregation("brand_aggs");

        List<StringTerms.Bucket> buckets = stringTerms.getBuckets();
        buckets.forEach(t -> {
            System.out.print(t.getKeyAsString() + ":"); // 得到桶里面的key
            System.out.print(t.getDocCount()); // 得到桶里面的 doc_count
            System.out.println();

            // 桶里面还包含着指标计算的结果
            Aggregation aggregation = t.getAggregations().asMap().get("price_avg");
            // 将该聚合转换成 InternalAvg
            InternalAvg internalAvg = (InternalAvg) aggregation;
            System.out.println(internalAvg.getValue());

        });

    }

}
