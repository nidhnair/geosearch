package com.corelogic.li.geosearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.List;

public class ElasticDao {

    private  ElasticsearchClient esClient;

    ElasticDao(ElasticsearchClient esClient){
        this.esClient = esClient;
    }
    public void createSomeShapeGeometries(String indexName, List<SomeShapesfromNYC> someShapesfromNYCS) throws JsonProcessingException, IOException {

        long millis = System.currentTimeMillis();

        for (SomeShapesfromNYC someShapesfromNYC : someShapesfromNYCS) {


            IndexRequest<SomeShapesfromNYC> request = IndexRequest.of(x -> x
                    .index(indexName)
                    .id(someShapesfromNYC.getId())
                    .document(someShapesfromNYC)
            );

            IndexResponse response = esClient.index(request);

            System.out.println("Indexed with version " + response.version());

        }

        System.out.println("time per insert nyc cities = " + (System.currentTimeMillis() - millis) / 10 + " milli seconds");
    }
}
