package com.czh.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @Author:chenzhihua
 * @Date: 2020/12/13 16:46
 * @Deacription:
 **/
@Configuration
public class EsConfiguration {


    @Value("${spring.elasticsearch.rest.uris}")
    private String uris;

    @Value("${spring.elasticsearch.rest.username}")
    private String username;

    @Value("${spring.elasticsearch.rest.password}")
    private String password;

    @Bean
    public HttpHost[] httpHost(){
        String[] urlStr = uris.split(",");
        HttpHost[] httpHostArray = new HttpHost[urlStr.length];
        for (int i=0;i<urlStr.length;i++) {
            String hostname=urlStr[i].split(":")[1].substring(2);
            int port=Integer.parseInt(urlStr[i].split(":")[2]);
            String scheme=urlStr[i].split(":")[0];
            HttpHost httpHost = new HttpHost(hostname, port,scheme);
            httpHostArray[i]=httpHost;
        }
        return httpHostArray;
    }

    @Bean(initMethod="init",destroyMethod="close")
    public ESClientSpringFactory getFactory(){
        return ESClientSpringFactory.
                build(httpHost(),username,password);
    }

    @Bean
    @Scope("singleton")
    public RestClient getRestClient(){
        return getFactory().getClient();
    }

    @Bean
    @Scope("singleton")
    public RestHighLevelClient getRHLClient(){
        return getFactory().getRhlClient();
    }
}
