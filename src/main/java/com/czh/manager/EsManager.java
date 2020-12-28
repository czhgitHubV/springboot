package com.czh.manager;

import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.ingest.DeletePipelineRequest;
import org.elasticsearch.action.ingest.PutPipelineRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Cancellable;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Author:chenzhihua
 * @Date: 2020/12/13 17:08
 * @Deacription:
 **/
@Service
public class EsManager {

    @Autowired
    public RestHighLevelClient client;

    private static final int START_OFFSET = 0;

    private static final  int MAX_COUNT = 1000;

    private IndexRequest getIndexRequest(String index,String doc,String pipelineId) {
        IndexRequest indexRequest = new IndexRequest(index);
        if (null == index) {
            throw new ElasticsearchException("index不能为空!");
        }
        if(!StringUtils.isEmpty(doc)){
            indexRequest.id(doc);
        }
        indexRequest.setPipeline(pipelineId);

        return indexRequest;
    }

    /**
     * @Author chenzhihua
     * @Date 16:13 2020/12/14 
     * @Description 创建索引
     *
     * @return*/
    public IndexResponse execIndex(String index,String doc, Map<String, Object> dataMap,String pipelineId) throws IOException {
        if(dataMap!=null && dataMap.size()<=0){
            throw new ElasticsearchException("数据不能为空!");
        }
        return client.index(getIndexRequest(index,doc,pipelineId).source(dataMap), RequestOptions.DEFAULT);

    }

    /**
     * @Author chenzhihua
     * @Date 16:16 2020/12/14 
     * @Description 异步执行索引
     **/
    public void  asyncExecIndex(String index,String type,String doc, Map<String, Object> dataMap,String pipelineId, ActionListener<IndexResponse> indexResponseActionListener) throws IOException {
        if(dataMap!=null && dataMap.size()<=0){
            throw new ElasticsearchException("数据不能为空!");
        }
        client.indexAsync(getIndexRequest(index,doc,pipelineId).source(dataMap),RequestOptions.DEFAULT,indexResponseActionListener);
    }

    public List<Map> queryAll(String index) throws IOException {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        MatchAllQueryBuilder matchAllQueryBuilder = new MatchAllQueryBuilder();
        searchSourceBuilder.query(matchAllQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        SearchHit[] hits = response.getHits().getHits();
        List<Map> mapList = new LinkedList<>();
        for(SearchHit hit: hits){
            Map map = JSONObject.parseObject(hit.getSourceAsString(),Map.class);
            map.remove("message");
            mapList.add(map);
        }

        return mapList;
    }

    //查询数据
    public void testQuery(String index,String key,String value) throws IOException {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder ssb = new SearchSourceBuilder();
        QueryBuilder qb = new MatchQueryBuilder(key,value);
        ssb.query(qb);
        searchRequest.source(ssb);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            String record = hit.getSourceAsString();
            System.out.println(record);
        }
    }
    //批量删除数据
    public boolean deleteByQuery(String index,String key,String value) throws IOException {
        DeleteByQueryRequest request = new DeleteByQueryRequest(index);
        QueryBuilder qb = new MatchQueryBuilder(key,value);
        request.setQuery(qb);
        BulkByScrollResponse response = client.deleteByQuery(request,RequestOptions.DEFAULT);
        return true;
    }
    //批量插入数据
    public BulkResponse insertListData(String index, List<Map<String, Object>> jsonMapList) {
        BulkRequest bulkRequest = new BulkRequest();
        for (Map<String, Object> jsonMap:jsonMapList) {
            IndexRequest indexRequest = new IndexRequest(index).source(jsonMap);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulkResponse = null;
        try {
            //同步执行
            bulkResponse = client.bulk(bulkRequest,RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  bulkResponse;
    }

    //query
    public List<Map> searchMatch(String index,String key,String value,String querytype) throws IOException {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        if(querytype.equals("term")){
            //termquery
//        searchSourceBuilder.query(QueryBuilders.termQuery(key,value));
        }else if(querytype.equals("prefix")){
            //prefixQuery
//        searchSourceBuilder.query(QueryBuilders.prefixQuery(key,value));
        }else{
            //matchquery
            searchSourceBuilder.query(QueryBuilders.matchQuery(key,value));
        }
        searchSourceBuilder.from(START_OFFSET);
        searchSourceBuilder.size(MAX_COUNT);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = client.search(searchRequest,RequestOptions.DEFAULT);
        System.out.println(JSONObject.toJSON(response));

        SearchHit[] hits = response.getHits().getHits();
        List<Map> mapList = new LinkedList<>();
        for(SearchHit hit: hits){
            Map map = JSONObject.parseObject(hit.getSourceAsString(),Map.class);
            mapList.add(map);
        }

        return mapList;
    }

    //增加一个管道
    public AcknowledgedResponse putPipeline(String pipelineId, String pipelineSetting) throws IOException {
        PutPipelineRequest request = new PutPipelineRequest(
                pipelineId,
                new BytesArray(pipelineSetting.getBytes(StandardCharsets.UTF_8)),
                XContentType.JSON
        );
        AcknowledgedResponse response = client.ingest().putPipeline(request, RequestOptions.DEFAULT);
        return response;
    }
    //删除管道
    public AcknowledgedResponse deletePileline(String pipelineId) throws IOException {
        DeletePipelineRequest request = new DeletePipelineRequest(pipelineId);
        AcknowledgedResponse response = client.ingest().deletePipeline(request, RequestOptions.DEFAULT);
        return response;
    }

    //修改索引配置
    public AcknowledgedResponse updateIndexSetting(String index,String settingKey,String settingValue) throws IOException {
        UpdateSettingsRequest request = new UpdateSettingsRequest(index);
        Settings settings =
                Settings.builder()
                        .put(settingKey, settingValue)
                        .build();
        request.settings(settings);
        AcknowledgedResponse updateSettingsResponse =
                client.indices().putSettings(request, RequestOptions.DEFAULT);
        return updateSettingsResponse;
    }



    private UpdateByQueryRequest getUpdateByQueryRequest(String index) throws IOException {
        UpdateByQueryRequest request = new UpdateByQueryRequest(index);
        ActionListener<BulkByScrollResponse> listener = new ActionListener<BulkByScrollResponse>(){
            @Override
            public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
                long updated = bulkByScrollResponse.getUpdated();//已更新的文档数
                System.out.println("已更新的文档数:"+updated);
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("更新索引失败!!!"+e.getMessage());
            }
        };
        client.updateByQueryAsync(request, RequestOptions.DEFAULT,listener);

        return request;
    }


    /**
     * @Author chenzhihua
     * @Date 14:33 2020/12/15
     * @Description 同步更新索引并指定管道
     **/
    public Map<String,Object> updateIndex(String index,String pipelineId) throws IOException {
        UpdateByQueryRequest request = getUpdateByQueryRequest(index);
        request.setPipeline(pipelineId);
        BulkByScrollResponse byScrollResponse = client.updateByQuery(request, RequestOptions.DEFAULT);
        Map<String,Object> map=new HashMap<String,Object>();
        long total = byScrollResponse.getTotal();//获取处理的文档总数
        map.put("total",total);
        long updated = byScrollResponse.getUpdated();//已更新的文档数
        map.put("updated",updated);
        long versionConflicts = byScrollResponse.getVersionConflicts();//版本冲突数
        map.put("versionConflicts",versionConflicts);
        return map;
    }

    /**
     * @Author chenzhihua
     * @Date 14:33 2020/12/15
     * @Description 异步更新索引并指定管道
     **/
    public void updateIndexByAsyn(String index,String pipelineId) throws IOException {
        UpdateByQueryRequest request = getUpdateByQueryRequest(index);
        request.setPipeline(pipelineId);
        Map<String,Object> map=new HashMap<String,Object>();
        ActionListener<BulkByScrollResponse> lisener=new ActionListener<BulkByScrollResponse>(){
            @Override
            public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
//                long total = bulkByScrollResponse.getTotal();//获取处理的文档总数
//                map.put("total",total);
//                long updated = bulkByScrollResponse.getUpdated();//已更新的文档数
//                map.put("updated",updated);
//                long versionConflicts = bulkByScrollResponse.getVersionConflicts();//版本冲突数
//                map.put("versionConflicts",versionConflicts);
//                map.put("message", "sueecss");
                System.out.println("异步更新索引成功!!!");
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("异步更新失败!!!"+e.getMessage());
            }
        };
        client.updateByQueryAsync(request,RequestOptions.DEFAULT,lisener);
    }



}
