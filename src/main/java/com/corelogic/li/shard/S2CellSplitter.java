package com.corelogic.li.shard;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.corelogic.li.geosearch.ElasticSearchClientCreator;
import com.google.common.geometry.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class S2CellSplitter {

    public static final int MAX_LEVEL = 6;

    ArrayList<S2CellId> allTexasCellLevel6 = new ArrayList<>();


    public ArrayList<S2CellId> getAllTexasCellLevel6() {
        return allTexasCellLevel6;
    }

    public S2LatLngRect getTexasboundingbox() {
        return texasboundingbox;
    }

    S2LatLngRect texasboundingbox = S2LatLngRect.fromPointPair(
            S2LatLng.fromDegrees(25.837377, -106.645646),  // Southwest corner of the texas
            S2LatLng.fromDegrees(36.500704, -93.508292) // Northeast corner of the texas
    );

    public static void main(String[] args) {
        // Define the number of points in the large S2 cell and the desired number of smaller cells
        int totalPoints = 1000000;
        int numSmallerCells = 10;



       // Map<S2CellId,Integer> celltoFeatureCountMap = createTexasCell();

    }



    public   Map<S2CellId, Integer> createTexasCellMap() {

        ElasticsearchClient es = ElasticSearchClientCreator.createElasticsearchClient();
        // Define the bounding box covering Texas
        S2LatLngRect texasboundingbox = S2LatLngRect.fromPointPair(
                S2LatLng.fromDegrees(25.837377, -106.645646),  // Southwest corner of the texas
                S2LatLng.fromDegrees(36.500704, -93.508292) // Northeast corner of the texas
        );
        S2RegionCoverer coverer = S2RegionCoverer.builder().setMaxLevel(MAX_LEVEL).setMinLevel(1).build();

        S2CellUnion allTexasCells = coverer.getCovering(texasboundingbox);

        allTexasCells.denormalize(MAX_LEVEL,1,allTexasCellLevel6);

        System.out.println("number of level cells covering texast" + allTexasCellLevel6.size());

        Map<S2CellId,Integer> celltoFeatureCountMap = new HashMap<>();
        System.out.println("Total num cells " + allTexasCellLevel6.size());
        for (S2CellId cellId : allTexasCellLevel6) {
            celltoFeatureCountMap.put(cellId, 0);
        }

        return celltoFeatureCountMap;
    }
}
