package com.quartz.cn.springbootquartzdemo.confg.esText;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.CollectionUtils;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion.Entry.Option;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.search.suggest.term.TermSuggestion;

import com.alibaba.fastjson.JSONObject;

import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry;

public class SuggestDemo {

	private static Logger logger = LogManager.getRootLogger();

	public static void main(String[] args) throws IOException {
		//termSuggest();

		logger.info("--------------------------------------");

	completionSuggester();//测试ok
	isExistDocument();
	updateDocument();
	MultiGetRequestDocument();
	Map<String, Object> where=new HashMap<String, Object>();
	 Map<String, Boolean> sortFieldsToAsc=new HashMap<String, Boolean>();
	 String[] includeFields=new String[]{}; 
	 String[] excludeFields=includeFields;//new String[]{"name"};
     int timeOut=100;
	List<Map<String, Object>> searchIndex = searchIndex("suggest2",1,2,where,sortFieldsToAsc,includeFields,excludeFields,timeOut);
	 for (Map<String, Object> map : searchIndex) {
		 for(java.util.Map.Entry<String, Object> entry : map.entrySet()){

			    String mapKey = entry.getKey();

			    Object mapValue = entry.getValue();

			    System.out.println(mapKey+":"+mapValue);

			}
	   }
	}
	
	/**
	 * 创建查询父对象 多个索引查询
	 * @throws IOException
	 */
	private static void MultiGetRequestDocument() throws IOException {
		RestHighLevelClient client = InitDemo.getClient();
		// 创建查询父对象
        MultiGetRequest request = new MultiGetRequest();
        // 添加子查询
        request.add(new MultiGetRequest.Item("mess", "_doc", "1"));
        request.add(new MultiGetRequest.Item("mess", "_doc", "2").fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE));
        String[] includes = new String[] {"user", "*r"};
        String[] excludes = Strings.EMPTY_ARRAY;
        FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includes, excludes);
        request.add(new MultiGetRequest.Item("mess", "_doc", "3").fetchSourceContext(fetchSourceContext));
//        // user必须在map集合中
//        request.add(new MultiGetRequest.Item("posts", "doc", "4")
//                .storedFields("user"));
//        MultiGetResponse response = client.mget(request, RequestOptions.DEFAULT);
//        MultiGetItemResponse item = response.getResponses()[0];
//        // user必须在map集合中
//        String value = item.getResponse().getField("user").getValue();

        // 针对每个子请求分别设置，无法在主请求中设置
        // 指定去哪个分片上查询，如何指定分片上没有，不会再去其它分片查询，如果不指定，则依次轮询各个分片查询
        request.add(new MultiGetRequest.Item("lib3", "user", "1")
                .routing("some_routing"));
      //  request.add(new MultiGetRequest.Item("index", "type", "with_parent")
     //           .parent("some_parent"));
        request.add(new MultiGetRequest.Item("dept", "_doc", "1")
                .versionType(VersionType.EXTERNAL)
                .version(101200003L));
        // preference, realtime and refresh 需要在主请求里设置，子请求中无法设置
        request.preference("some_preference");
        // realtime的值默认为true
        request.realtime(false);
        request.refresh(true);
        MultiGetResponse response = client.mget(request, RequestOptions.DEFAULT);
        int count = 0;
        for(MultiGetItemResponse item: response.getResponses()) {
            String index = item.getIndex();
            String type = item.getType();
            String id = item.getId();
            System.out.println("第" + ++count + "条-》index:" + index + "; type:" + type + "; id:" + id);
            if(item.getFailure() != null) {
                Exception e = item.getFailure().getFailure();
                ElasticsearchException ee = (ElasticsearchException) e;
                if(ee.getMessage().contains("reason=no such index")) {
                    System.out.println("查询的文档库不存在！");
                }
            }

            GetResponse getResponse = item.getResponse();

            if (getResponse.isExists()) {
                long version = getResponse.getVersion();
                String sourceAsString = getResponse.getSourceAsString();
                System.out.println("查询的结果为：");
                System.out.println(sourceAsString);
                Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
                byte[] sourceAsBytes = getResponse.getSourceAsBytes();
            } else {
                System.out.println("没有查询到相应文档");
            }
        }
//https://blog.csdn.net/qq_2300688967/java/article/details/83867140
		
	}


	/**分页查询
条件查询

文本模糊匹配
时间范围匹配
超时设置

es超时时间
RestHighLevelClient发送请求的http响应超时时间
排序
指定返回列
     * @param index  索引
     * @param from  开始页面
     * @param size  每页几条
     * @param where  条件查询
     * @param sortFieldsToAsc  
     * @param includeFields
     * @param excludeFields
     * @param timeOut
     * @return
     */
    public static List<Map<String, Object>> searchIndex(String index, int from, int size, Map<String, Object> where,
                                                 Map<String, Boolean> sortFieldsToAsc, String[] includeFields, String[] excludeFields,
                                                 int timeOut) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        try {
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            //条件
            if (where != null && !where.isEmpty()) {
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                where.forEach((k, v) -> {
                    if (v instanceof Map) {
                        //范围选择map  暂定时间
                        Map<String, Date> mapV = (Map<String, Date>) v;
                        if (mapV != null) {
                            boolQueryBuilder.must(
                                    QueryBuilders.rangeQuery(k).
                                            gte(format.format(mapV.get("start"))).
                                            lt(format.format(mapV.get("end"))));
                        }
                    } else {
                        //普通模糊匹配
                        boolQueryBuilder.must(QueryBuilders.wildcardQuery(k, v.toString()));
                    }
                });
                sourceBuilder.query(boolQueryBuilder);
            }

            //分页
            from = from <= -1 ? 0 : from;
            size = size >= 1000 ? 1000 : size;
            size = size <= 0 ? 15 : size;
            sourceBuilder.from(from);
            sourceBuilder.size(size);

            //超时
            sourceBuilder.timeout(new TimeValue(timeOut, TimeUnit.SECONDS));

            //排序
            if (sortFieldsToAsc != null && !sortFieldsToAsc.isEmpty()) {
                sortFieldsToAsc.forEach((k, v) -> {
                    sourceBuilder.sort(new FieldSortBuilder(k).order(v ? SortOrder.ASC : SortOrder.DESC));
                });
            } else {
                sourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
            }

            //返回和排除列
            if (!CollectionUtils.isEmpty(includeFields) || !CollectionUtils.isEmpty(excludeFields)) {
                sourceBuilder.fetchSource(includeFields, excludeFields);
            }

            SearchRequest rq = new SearchRequest();
            //索引
            rq.indices(index);
            //各种组合条件
            rq.source(sourceBuilder);
            RestHighLevelClient client = InitDemo.getClient();
            //请求
            System.out.println("请求"+rq.source().toString());  //打印 请求的数据
            SearchResponse rp = client.search(rq, RequestOptions.DEFAULT);
            System.out.println("请求返回的结果"+rp); 
            //解析返回
            if (rp.status() != RestStatus.OK || rp.getHits().getMaxScore()<0) {
                return Collections.emptyList();
            }

            //获取source
            return Arrays.stream(rp.getHits().getHits()).map(b -> {
                return b.getSourceAsMap();
            }).collect(Collectors.toList());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return Collections.emptyList();
    }
	   /**
	    * 更新文档
	    * @throws IOException
	    */
    static void updateDocument() throws IOException {
    	RestHighLevelClient client = InitDemo.getClient();
        UpdateRequest request = new UpdateRequest("suggest2", "GWoh13IBYPi_ZUEZyMQB");
        request.timeout("1s");

      String str="{  \"name\":\"苹果3\"}";
        request.doc(str, XContentType.JSON);
        UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);

        System.out.println(updateResponse.status());
        client.close();
    }

	/**
	 * 查询文档是否存在
	 * @throws IOException
	 */
	static void isExistDocument() throws IOException {
		RestHighLevelClient client = InitDemo.getClient();
        GetRequest request = new GetRequest("suggest2", "1");
        //不获取_source内容,提升效率
        request.fetchSourceContext(new FetchSourceContext(false));
        request.storedFields("_none_");

        boolean exists = client.exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }
//：https://blog.csdn.net/Mr_kidBK/java/article/details/105477095
	// 词项建议拼写检查，检查用户的拼写是否错误，如果有错给用户推荐正确的词，appel->apple
	public static void termSuggest() {
		int from1=0,size1=0;
		try (RestHighLevelClient client = InitDemo.getClient();) {
			// 1、创建search请求
			SearchRequest searchRequest = new SearchRequest("suggest2");

			// 2、用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			sourceBuilder.size(0);
//		int	from = from1 <= -1 ? 0 : from1;
//         int   size = size1 >= 1000 ? 1000 : size1;
//            size = size1 <= 0 ? 15 : size;
//            //其实位置
//            sourceBuilder.from(from);
//            //每页数量
//            sourceBuilder.size(size);
			
			
			sourceBuilder.query(QueryBuilders.matchAllQuery());
			// 做查询建议                   测试---err
			// 词项建议
			SuggestionBuilder termSuggestionBuilder = SuggestBuilders.termSuggestion("name").text("苹");
			SuggestBuilder suggestBuilder = new SuggestBuilder();
			suggestBuilder.addSuggestion("suggest_user", termSuggestionBuilder);
			sourceBuilder.suggest(suggestBuilder);
			/**
			 * "suggest":{ "suggest_user":[ { "text":"kmichy", "offset":0, "length":6,
			 * "options":[ { "text":"kimchy1", "score":0.6666666, "freq":2 }, {
			 * "text":"kimchy3", "score":0.6666666, "freq":1 } ] }
			 */

			searchRequest.source(sourceBuilder);

			// 3、发送请求
			SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

			// 4、处理响应
			// 搜索结果状态信息
			if (RestStatus.OK.equals(searchResponse.status())) {
				// 获取建议结果
				Suggest suggest = searchResponse.getSuggest();
				TermSuggestion termSuggestion = suggest.getSuggestion("suggest_user");
				for (TermSuggestion.Entry entry : termSuggestion.getEntries()) {
					System.err.println("text: " + entry.getText().string());
					for (TermSuggestion.Entry.Option option : entry) {
						String suggestText = option.getText().string();
						System.err.println("   suggest option : " + suggestText);
					}
				}
			}
			/*
			 * "suggest": { "my-suggestion": [ { "text": "tring", "offset": 0, "length": 5,
			 * "options": [ { "text": "trying", "score": 0.8, "freq": 1 } ] }, { "text":
			 * "out", "offset": 6, "length": 3, "options": [] }, { "text": "elasticsearch",
			 * "offset": 10, "length": 13, "options": [] } ] }
			 */

		} catch (IOException e) {
			logger.error(e);
		}
	}

	// 自动补全，根据用户的输入联想到可能的词或者短语              获取相关搜索、最多返回9条
	public static void completionSuggester() {
		try (RestHighLevelClient client = InitDemo.getClient();) {

			// 1、创建search请求
			// SearchRequest searchRequest = new SearchRequest();
			SearchRequest searchRequest = new SearchRequest("suggest2");

			// 2、用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			sourceBuilder.size(0); //SearchRequestBuilder的size要设置为0，否则显示hits结果
			
			
			sourceBuilder.query(QueryBuilders.matchAllQuery());
			
			// 做查询建议   测试ok
			// 自动补全
			/*
			 * POST music/_search?pretty { "suggest": { "song-suggest" : { "prefix" :
			 * "lucene s", "completion" : { "field" : "suggest" , "skip_duplicates": true }
			 * } } }
			 */

			SuggestionBuilder termSuggestionBuilder = SuggestBuilders.completionSuggestion("name.keyword_pinyin").prefix("shui")
					.skipDuplicates(true);
			SuggestBuilder suggestBuilder = new SuggestBuilder();
			suggestBuilder.addSuggestion("song-suggest", termSuggestionBuilder);
			sourceBuilder.suggest(suggestBuilder);

			
			searchRequest.source(sourceBuilder);

			// 3、发送请求
			SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

			// 4、处理响应
			// 搜索结果状态信息
			if (RestStatus.OK.equals(searchResponse.status())) {
				// 获取建议结果
				Suggest suggest = searchResponse.getSuggest();
				CompletionSuggestion termSuggestion = suggest.getSuggestion("song-suggest");
				
				Set<String> keywords = null;
				keywords = new HashSet<>();
				for (CompletionSuggestion.Entry entry : termSuggestion.getEntries()) {
					System.err.println("text: " + entry.getText().string());
					for (CompletionSuggestion.Entry.Option option : entry) {
						String suggestText = option.getText().string();
						System.err.println("   suggest option : " + suggestText);
						/** 最多返回9个推荐, 每个长度最大为20 */
						if (!StringUtils.isEmpty(suggestText) && suggestText.length() <= 20) {
	                        /** 去除输入字段 */
	                        if (suggestText.equals("shui")) continue;
	                        keywords.add(suggestText);
	                        if (keywords.size() >= 9) {
	                            break;
	                        }
	                    }
					
					}
				}
			}

		} catch (IOException e) {
			logger.error(e);
		}
	}

	

	
	
	

}