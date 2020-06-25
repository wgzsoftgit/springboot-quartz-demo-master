package com.quartz.cn.springbootquartzdemo.confg.esText;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
/**
 * 搜索数据
 * @author wgz
 * @date 创建时间：2020年6月18日 下午9:25:59
 */
public class SearchDemo {
    
    private static Logger logger = LogManager.getRootLogger();  
 
    public static void main(String[] args) {
        try (RestHighLevelClient client = InitDemo.getClient();) {
            
        	
            
            // 2、用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
            
            //构造QueryBuilder         "user":"kimchy", 查询user=kimchy   条件查询
//            QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("user", "kimchy")
//                    .fuzziness(Fuzziness.AUTO)
//                    .prefixLength(3)
//                    .maxExpansions(10);        
//            sourceBuilder.query(matchQueryBuilder);
//            QueryStringQueryBuilder queryBuilder = new QueryStringQueryBuilder("");
//            queryBuilder.analyzer("ik_smart");
//            queryBuilder.field("title").field("content");
//            sourceBuilder.query(queryBuilder);
            
   //       sourceBuilder.query(QueryBuilders.termQuery("user", "kimchy")); 
      //      sourceBuilder.query(QueryBuilders.prefixQuery("user", "kimchy"));
        //  sourceBuilder.query(QueryBuilders.wildcardQuery("user", "kimchy*")); //模糊查
           sourceBuilder.query(QueryBuilders.matchQuery("message", "out"));
            
         
         
          //范围查询
//            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
                             //这里可以根据字段进行搜索，must表示符合条件的，相反的mustnot表示不符合条件的
//            MatchQueryBuilder matchQueryBuilder1 = QueryBuilders.matchQuery("text","test");
//            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("Time");//范围查
//            //起始时间
//            rangeQueryBuilder.gte("2020-04-01T00:00:00+08:00");
//            //结束时间
//            rangeQueryBuilder.lte("2020-04-31T23:59:59+08:00");
//            boolBuilder.must(matchQueryBuilder1).must(rangeQueryBuilder);
//            sourceBuilder.query(boolBuilder);
 
     ////filter 效率比 must高的多
      //  boolQueryBuilder.filter(QueryBuilders.termQuery("routerDatabaseNo", query.getRouterDatabaseNo()));
     //boolQueryBuilder.filter(QueryBuilders.rangeQuery("createTime").from( query.getCreateTime()).to(query.getUpdateTime()));     
          
            sourceBuilder.from(0);  ////设置确定结果要从哪个索引开始搜索的from选项，默认为0
            sourceBuilder.size(10); //分页查询    0-10  //设置确定搜素命中返回数的size选项，默认为10
           sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS)); //设置一个可选的超时，控制允许搜索的时间。
           
            //是否返回_source字段     完全关闭_source检索
            //sourceBuilder.fetchSource(false);
         //第一个是获取字段，第二个是过滤的字段，默认获取全部
           //sourceBuilder.fetchSource(new String[] {"fields.port","fields.entity_id","fields.message"}, new String[] {});         
          
           //设置返回哪些字段          接受一个或多个通配符模式的数组，以控制以更精细的方式包含或排除哪些字段
            /*String[] includeFields = new String[] {"title", "user", "innerObject.*"};
            String[] excludeFields = new String[] {"_type"};
            sourceBuilder.fetchSource(includeFields, excludeFields);*/
            
            //指定排序  
              ////按_score降序排序（默认值）
//            sourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC)); 
           //也可以按_id字段进行升序排序
 //           sourceBuilder.sort(new FieldSortBuilder("_uid").order(SortOrder.ASC));
            
            // 设置返回 profile 
            //sourceBuilder.profile(true);
            
           
            
            // 可选的设置
            //searchRequest.routing("routing");
            
            // 高亮设置
           /* 
            HighlightBuilder highlightBuilder = new HighlightBuilder(); //创建一个新的HighlightBuilder。
            HighlightBuilder.Field highlightTitle =
                    new HighlightBuilder.Field("title"); //为title字段创建字段高光色。
            highlightTitle.highlighterType("unified");  // 设置字段高光色类型 
            highlightBuilder.field(highlightTitle);  //将字段高光色添加到高亮构建器。
            HighlightBuilder.Field highlightUser = new HighlightBuilder.Field("user");
            highlightBuilder.field(highlightUser);
            sourceBuilder.highlighter(highlightBuilder);
             */
         //   /*
              HighlightBuilder highlightBuilder = new HighlightBuilder().field("user")
            		     .requireFieldMatch(true);
            highlightBuilder.preTags("<span style=\"color:red\">");
            highlightBuilder.postTags("</span>");
            highlightBuilder.boundaryMaxScan(4);
            sourceBuilder.highlighter(highlightBuilder);
             //*/
            
            
            //加入聚合       多分组测试                  分组之后添加平均值
  //          /*
             TermsAggregationBuilder aggregation2 = AggregationBuilders.terms("by_user")
                    .field("user");
            aggregation2.subAggregation(AggregationBuilders.avg("average_age")
                    .field("age"));
            sourceBuilder.aggregation(aggregation2);
    //        */
            
   //         /*
            
            
      //按时间聚合，求TX的和
//DateHistogramInterval.minutes(5)是指按5分钟聚合
//format("yyyy-MM-dd HH:mm")是指聚合的结果的Time的格式
//BucketOrder.aggregation("tx_sum", false)对聚合结果的排序 true为正序 false为倒序
//		AggregationBuilder aggregation = AggregationBuilders.dateHistogram("time_count")
//				           .field("postDate")
//				           .fixedInterval(DateHistogramInterval.minutes(5))
//				           .format("yyyy-MM-dd HH:mm")
//			.order(BucketOrder.aggregation("tx_sum", false));
//		aggregation.subAggregation(AggregationBuilders.sum("tx_sum").field("postDate"));
//		sourceBuilder.aggregation(aggregation);
  
		
      //       */
      /**  aggs相当于group
       * "aggregations":{
"date_histogram#time_count":{
"buckets":[
{
"key_as_string":"2013-01-30 00:00",    
"key":1359504000000,
"doc_count":3,
"sum#tx_sum":{
"value":4.078512E12,
"value_as_string":"2099-03-30T00:00:00.000Z"
}}]}}      
       */
            
            
            
            
            //做查询建议        termSuggestion("user").text("kmichy");和查询条件一致    user=kmichy
        //    /*
            SuggestionBuilder termSuggestionBuilder =
                    SuggestBuilders.termSuggestion("user").text("kmichy"); 
                SuggestBuilder suggestBuilder = new SuggestBuilder();
                suggestBuilder.addSuggestion("suggest_user", termSuggestionBuilder); 
            sourceBuilder.suggest(suggestBuilder);
           // */
            /**
             * "suggest":{
					"suggest_user":[
					{
					"text":"kmichy",
					"offset":0,
					"length":6,
					"options":[
					{
					"text":"kimchy",
					"score":0.8333333,
					"freq":2
					}]}]}
             */
            
            
         // 1、创建search请求
            //SearchRequest searchRequest = new SearchRequest();
            SearchRequest searchRequest = new SearchRequest("mess"); 
            searchRequest.types("_doc");
            //将请求体加入到请求中
            searchRequest.source(sourceBuilder);
            
            //3、发送请求        
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            /**
             * {"took":4,"timed_out":false,"_shards":{"total":1,"successful":1,"skipped":0,"failed":0},"hits":{"total":{"value":2,"relation":"eq"},"max_score":0.18232156,"hits":[{"_index":"mess","_type":"_doc","_id":"1","_score":0.18232156,"_source":{"user":"kimchy","postDate":"2013-01-30","message":"trying out Elasticsearch"},"highlight":{"user":["<em>kimchy</em>"]}},{"_index":"mess","_type":"_doc","_id":"4","_score":0.18232156,"_source":{"user":"kimchy","age":"30","postDate":"2013-01-30","message":"trying out Elasticsearch"},"highlight":{"user":["<em>kimchy</em>"]}}]},"suggest":{"suggest_user":[{"text":"kmichy","offset":0,"length":6,"options":[{"text":"kimchy","score":0.8333333,"freq":2}]}]}}
             */
            
            //4、处理响应
            //搜索结果状态信息
            RestStatus status = searchResponse.status();
            TimeValue took = searchResponse.getTook();
            Boolean terminatedEarly = searchResponse.isTerminatedEarly();
            boolean timedOut = searchResponse.isTimedOut();
            
            //分片搜索情况
            int totalShards = searchResponse.getTotalShards();
            int successfulShards = searchResponse.getSuccessfulShards();
            int failedShards = searchResponse.getFailedShards();
            for (ShardSearchFailure failure : searchResponse.getShardFailures()) {
                // failures should be handled here
            	System.err.println(failure);
            }
            
            //处理搜索命中文档结果
            SearchHits hits = searchResponse.getHits();
            
            long totalHits =(long) hits.getMaxScore(); //hits.getTotalHits();//查询的总数(命中数)
            float maxScore = hits.getMaxScore();
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : searchHits) {
                // do something with the SearchHit
                
                String index = hit.getIndex();
                String type = hit.getType();
                String id = hit.getId();
                float score = hit.getScore();
                
                //取_source字段值
                String sourceAsString = hit.getSourceAsString(); //取成json串
                Map<String, Object> sourceAsMap = hit.getSourceAsMap(); // 取成map对象
                //从map中取字段值
                /*
                String documentTitle = (String) sourceAsMap.get("title"); 
                List<Object> users = (List<Object>) sourceAsMap.get("user");
                Map<String, Object> innerObject = (Map<String, Object>) sourceAsMap.get("innerObject");
                */
                System.err.println("index:" + index + "  type:" + type + "  id:" + id);
                System.err.println(sourceAsString);
                
                //取高亮结果
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                HighlightField highlight = highlightFields.get("user"); 
                Text[] fragments = highlight.fragments();  
                String fragmentString = fragments[0].string();
                System.err.println("高亮显示 "+fragmentString);
            }
            
            // 获取聚合结果               分组 和求平均值
          //  /*
   
            Terms terms1 = searchResponse.getAggregations().get("by_user");
            Avg terms2;
    	    for (Terms.Bucket bucket1 : terms1.getBuckets()) {
    	        terms2 = bucket1.getAggregations().get("average_age");
    	        double avg = terms2.getValue();
              System.err.println(avg);
    	   
    	    }
    	    
//            Bucket elasticBucket = byCompanyAggregation.getBucketByKey("Elastic"); 
//            Avg averageAge = elasticBucket.getAggregations().get("average_age"); 
//            double avg = averageAge.getValue();
//            System.err.println(avg);
        //    */
            
            // 获取建议结果
            /*Suggest suggest = searchResponse.getSuggest(); 
            TermSuggestion termSuggestion = suggest.getSuggestion("suggest_user"); 
            for (TermSuggestion.Entry entry : termSuggestion.getEntries()) { 
                for (TermSuggestion.Entry.Option option : entry) { 
                    String suggestText = option.getText().string();
                }
            }
            */
            
            //异步方式发送获查询请求
            /*
            ActionListener<SearchResponse> listener = new ActionListener<SearchResponse>() {
                @Override
                public void onResponse(SearchResponse getResponse) {
                    //结果获取
                }
            
                @Override
                public void onFailure(Exception e) {
                    //失败处理
                }
            };
            client.searchAsync(searchRequest, listener); 
            */
            
        } catch (IOException e) {
            logger.error(e);
        }
    }
}