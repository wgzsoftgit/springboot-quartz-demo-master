package com.quartz.cn.springbootquartzdemo.confg.suggesterText;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
 /**
  * 搜索补全代码
  * @author wgz
  * @date 创建时间：2020年6月22日 上午6:54:12
  */
public class EsSuggestTest01 {
 
private static RestHighLevelClient client=ElasticsearchConfiguration.getClient();;
 
    
   // public void init() throws UnknownHostException {
  //      client = ElasticsearchConfiguration.getClient();
  // }
 
    
  //  public void close() throws IOException {
  //  	ElasticsearchConfiguration.close();
  //  }
    public static void main(String[] args) throws IOException {
     	String index = "suggest2";
    	String type = "folks";
    	QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
    	String text = "t"; // 水   shui
    	String field = "name";
		
    	if(checkLetter(text)) {
    		field = "name.keyword_pinyin";	
    	} else if(checkChinese(text)) {
    		field = "name";
    	} else {
    		field = "name.keyword_pinyin";
	}
		
	Set<String> results = getSuggestWord(index, type, field, text, queryBuilder);
	results=new HashSet();
	System.err.println(results.size());
	//结果为空且是拼音，可以尝试拼音首字母提示
	if(results.size() == 0 && checkLetter(text)) {
		field = "name.keyword_first_py";
		results = getSuggestWord(index, type, field, text, queryBuilder);
	}
		
    	for (String result : results) {
    	    System.out.println(result);
    	}
    	
    	
    	ElasticsearchConfiguration.close();
	}
    
    
    
	/**
	  * Description:提示词，支持中文、拼音、首字母等(注意要去掉_source信息)
	  * 
	  * 1、检测搜索词是中文还是拼音
      * 2、若是中文，直接按照name字段提示
      * 3、若是拼音（拼音+汉字），先按照name.keyword_pinyin获取，若是无结果按照首字母name.keyword_first_py获取
	  * 
	  * SearchRequestBuilder的size要设置为0，否则显示hits结果
	  * searchRequestBuilder.setSize(0);
	  * 
	  * _source 由于磁盘读取和网络传输开销，可以影响性能的大小，为了节省一些网络开销，请从_source 使用源过滤中过滤掉不必要的字段以最小化 _source大小
	  * 可以采用过滤的形式，也可以直接不显示_source
	  * 1、searchRequestBuilder.setFetchSource("name", null);     过滤形式
	  * 2、searchRequestBuilder.setFetchSource(false)   直接不显示_source
	  * 
	  * @author wangweidong
	  * CreateTime: 2018年6月28日 下午2:39:47
	  *
	  * @param index 索引
	  * @param type  类型
	  * @param field   key的过滤条件
	  * @param text   内容
	  *  @param queryBuilder  条件搜索
	  * @return
	 * @throws IOException 
	 */
	 public static Set<String> getSuggestWord(String index, String type, String field, String text, QueryBuilder queryBuilder) throws IOException {
		//过滤相同的提示词，Es5.2版本不支持过滤掉重复的建议，故需自己对ES返回做去重处理，Es6.1以上版本可以通过skip_duplicates字段处理，skip_duplicates表示是否应过滤掉重复的建议（默认为false）
		Set<String> results = new TreeSet<String>();
		CompletionSuggestionBuilder termSuggestionBuilder = new CompletionSuggestionBuilder(field);
		termSuggestionBuilder.text(text);
		termSuggestionBuilder.size(20);
	      
	   	
		

	   	SearchRequest searchRequest = new SearchRequest(index); 
        searchRequest.types(type);//"_doc"
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.size(0);
		sourceBuilder.query(queryBuilder); 
		
//		SuggestionBuilder termSuggestionBuilder = SuggestBuilders.completionSuggestion(field).prefix(text)
//				.skipDuplicates(true);
		SuggestBuilder suggestBuilder = new SuggestBuilder();
		suggestBuilder.addSuggestion("my-suggest-1", termSuggestionBuilder);
		sourceBuilder.suggest(suggestBuilder);
		searchRequest.source(sourceBuilder);
		  //3、发送请求        
        SearchResponse resp = client.search(searchRequest, RequestOptions.DEFAULT);
        

	   	Suggest sugg = resp.getSuggest();
	   	CompletionSuggestion suggestion = sugg.getSuggestion("my-suggest-1");
	   	List<CompletionSuggestion.Entry> list = suggestion.getEntries();
	   	for (int i = 0; i < list.size(); i++) {
	   		List<? extends Suggest.Suggestion.Entry.Option> options = list.get(i).getOptions();  
	   		for (Suggest.Suggestion.Entry.Option option : options) {
	   			String suggestText = option.getText().string();
	   			/** 最多返回9个推荐, 每个长度最大为20 */
				if (!StringUtils.isEmpty(suggestText) && suggestText.length() <= 20) {
                    /** 去除输入字段 */
                    if (suggestText.equals("shui")) continue;
                    results.add(suggestText);
                    if (results.size() >= 9) {
                        break;
                    }
                }
	   			
			}
	   	}
	   	
//	   	Set<String> keywords = null;
//		keywords = new HashSet<>();
//		for (CompletionSuggestion.Entry entry : termSuggestion.getEntries()) {
//			System.err.println("text: " + entry.getText().string());
//			for (CompletionSuggestion.Entry.Option option : entry) {
//				String suggestText = option.getText().string();
//				/** 最多返回9个推荐, 每个长度最大为20 */
//				if (!StringUtils.isEmpty(suggestText) && suggestText.length() <= 20) {
//                    /** 去除输入字段 */
//                    if (suggestText.equals("shui")) continue;
//                    keywords.add(suggestText);
//                    if (keywords.size() >= 9) {
//                        break;
//                    }
//                }
//				System.err.println("   suggest option : " + suggestText);
//			}
//		}
	   	return results;
    }
    
    /**
     * 只包含字母
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkLetter(String cardNum) { 
        String regex = "^[A-Za-z]+$";
        return Pattern.matches(regex, cardNum); 
    }
    
    /**
     * 验证中文
     * @param chinese 中文字符
     * @return 验证成功返回true，验证失败返回false
     */ 
    public static boolean checkChinese(String chinese) { 
        String regex = "^[\u4E00-\u9FA5]+$"; 
        return Pattern.matches(regex,chinese); 
    } 
}

//：https://blog.csdn.net/wwd0501/java/article/details/80885987