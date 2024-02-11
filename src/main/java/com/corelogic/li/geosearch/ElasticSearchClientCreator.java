package com.corelogic.li.geosearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

public class ElasticSearchClientCreator {
    // Create ElasticsearchClient
    static ElasticsearchClient createElasticsearchClient() {

        String serverUrl = "http://localhost:9200";

        // Create the low-level client
        RestClient restClient = RestClient
                .builder(HttpHost.create(serverUrl))
                .build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        return new ElasticsearchClient(transport);

    }
}