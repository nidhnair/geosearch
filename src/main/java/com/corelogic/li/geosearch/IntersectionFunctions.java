package com.corelogic.li.geosearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.corelogic.li.shard.S2CellSplitter;
import com.google.common.geometry.*;
import org.geotools.api.referencing.FactoryException;
import org.locationtech.jts.io.ParseException;

import java.io.IOException;
import java.util.*;

import static com.corelogic.li.shard.S2CellSplitter.MAX_LEVEL;


public class IntersectionFunctions {

    //search and knn for subwways
    //triangultion

    public static final String SUBWAY_INDEX_NAME = "nycsubwaystations";
    static S2Cell cell1118 = null;
    static S2Cell cell498 = null;

    static S2CellId cell1118Id = null;
    static S2CellId cell498Id = null;

    public static void main(String[] args) throws IOException, FactoryException, ParseException {
        ElasticsearchClient es = ElasticSearchClientCreator.createElasticsearchClient();

        List<SomeShapesfromNYC> someShapesfromNYCS = findAllTracts(es);

        S2CellSplitter s2CellSplitter = new S2CellSplitter();

        Map<S2CellId, Integer> celltoFeatureCountMap = s2CellSplitter.createTexasCellMap();

        ElasticDao elasticDao = new ElasticDao(es);

        //display original mapping with counts
        Map<S2CellId, Integer> updatedCelltoFeatureCountMap = updateCelltoFeatureCountMapUsingTractData(someShapesfromNYCS, celltoFeatureCountMap);
        List<SomeShapesfromNYC> cellBounds = convertCellIdListToNYCShape(updatedCelltoFeatureCountMap);
        elasticDao.createSomeShapeGeometries("newshapes", cellBounds);

        //Create density based bounds

        int featureCount = findTotalFeatureCount(celltoFeatureCountMap);
        System.out.println("featureCount = " + featureCount);
        int maxCountPerShard = featureCount / 10;
        System.out.println("maxCountPerShard = " + maxCountPerShard);
        ArrayList<S2CellId> allTexasCellsLvl6 = s2CellSplitter.getAllTexasCellLevel6();
        List<S2CellUnion> finalCellUnionList = mergeIntoFinalShardedCellUnions(allTexasCellsLvl6, updatedCelltoFeatureCountMap, maxCountPerShard);
        List<SomeShapesfromNYC> rectangularBounds = convertCellUnionListToNYCShape(finalCellUnionList);
        elasticDao.createSomeShapeGeometries("tempshapes", rectangularBounds);


    }

    private static List<SomeShapesfromNYC> convertCellIdListToNYCShape(Map<S2CellId, Integer> celltoFeatureCountMap) {
        List<SomeShapesfromNYC> someShapesfromNYCS = new ArrayList<>();
        int id = 0;

        for (var entry : celltoFeatureCountMap.entrySet()) {
            Integer featureCount = entry.getValue();
            S2CellId s2CellId = entry.getKey();
            S2Cell s2Cell = new S2Cell(s2CellId);

            S2LatLngRect rectBound = s2Cell.getRectBound();
            String wkt = WKTConverter.convertS2LatLngRectToWKT(rectBound);
            SomeShapesfromNYC someShapesfromNYC = new SomeShapesfromNYC();
            someShapesfromNYC.setId("s2cell" + id++ + "");
            someShapesfromNYC.setLocation(wkt);
            Map<String, String> attributeMap = new HashMap<>();
            attributeMap.put("numcounties", entry.getValue() + "");
            someShapesfromNYC.setAttributeMap(attributeMap);
            someShapesfromNYCS.add(someShapesfromNYC);

        }

        return someShapesfromNYCS;
    }

    private static List<SomeShapesfromNYC> convertCellUnionListToNYCShape(List<S2CellUnion> finalCellUnionList) {
        List<SomeShapesfromNYC> someShapesfromNYCS = new ArrayList<>();
        int id = 1;
        for (S2CellUnion s2CellUnion : finalCellUnionList) {
            System.out.println("s2CellUnion.size() = " + s2CellUnion.size());
            S2LatLngRect rectBound = s2CellUnion.getRectBound();
            String wkt = WKTConverter.convertS2LatLngRectToWKT(rectBound);
            SomeShapesfromNYC someShapesfromNYC = new SomeShapesfromNYC();
            someShapesfromNYC.setId(id + "");
            someShapesfromNYC.setLocation(wkt);

            Map<String, String> attributeMap = new HashMap<>();
            attributeMap.put("bigcellid", id + "");
            id = id + 1;
            someShapesfromNYC.setAttributeMap(attributeMap);
//            if (id ==2 || id==3 || id==4) {
            someShapesfromNYCS.add(someShapesfromNYC);
//
            System.out.println("cellunion = " + id);
        }
        return someShapesfromNYCS;
    }

    private static List<S2CellUnion> mergeIntoFinalShardedCellUnions(ArrayList<S2CellId> allTexasCellsLvl6, Map<S2CellId, Integer> updatedCelltoFeatureCountMap, int maxCountPerShard) {
        List<S2CellUnion> finalCellUnionList = new ArrayList<>();
        ArrayList<S2CellId> currentShardList = new ArrayList<>();
        S2CellUnion cellUnion;
        int currentShardCount = 0;

        Set<S2CellId> s = new HashSet<>();
        for (S2CellId s2CellId : allTexasCellsLvl6) {
//            System.out.println("s2CellId = " + s2CellId);
            Integer count = updatedCelltoFeatureCountMap.get(s2CellId);
//            System.out.println("count = " + count);
            currentShardCount = currentShardCount + count;
//            System.out.println("currentShardCount = " + currentShardCount);
            currentShardList.add(s2CellId);
            if (currentShardCount > maxCountPerShard) {
//                System.out.println("met current shard count = " + currentShardCount);
//                System.out.println("currentShardList size before = " + currentShardList.size());
                cellUnion = new S2CellUnion();
                cellUnion.initFromCellIds(currentShardList);
                ArrayList<S2CellId> output = new ArrayList<>();
                cellUnion.denormalize(6,1,output);
                finalCellUnionList.add(cellUnion);

//                System.out.println("currentShardList size after = " + currentShardList.size());
//                System.out.println(" Cellunion size = " + cellUnion.size());
                for (S2CellId normalizeCellId : cellUnion.cellIds()) {
                    System.out.println(" created s2CellId = " + normalizeCellId);
                    if (s.contains(normalizeCellId)){
                        System.out.println("already found " + normalizeCellId);
                    }else {
                        s.add(s2CellId);
                    }
                }
                currentShardList = new ArrayList<>();
                currentShardCount = 0;
//                return finalCellUnionList;
            }
        }
        System.out.println("num unique cells" + s.size());
        cellUnion = new S2CellUnion();
        cellUnion.initFromCellIds(currentShardList);
        finalCellUnionList.add(cellUnion);
        return finalCellUnionList;
    }


    private static int findTotalFeatureCount(Map<S2CellId, Integer> celltoFeatureCountMap) {
        int cellsWithNofeatures = 0;
        int totalFeatureCount = 0;
        for (var entry : celltoFeatureCountMap.entrySet()) {
//                var cellId = entry.getKey();
            Integer featureCount = entry.getValue();
            if (featureCount != 0) {
//                    System.out.println("cell id: " + cellId + ", hits: " + featureCount);
                totalFeatureCount = totalFeatureCount + featureCount;
            }
        }

        System.out.println("totalFeatureCount = " + totalFeatureCount);
        System.out.println("cells with no feature " + cellsWithNofeatures);

        return totalFeatureCount;
    }

    private static Map<S2CellId, Integer> updateCelltoFeatureCountMapUsingTractData(List<SomeShapesfromNYC> someShapesfromNYCS, Map<S2CellId, Integer> celltoFeatureCountMap) {
        S2RegionCoverer coverer = S2RegionCoverer.builder().setMaxLevel(MAX_LEVEL).setMinLevel(1).build();


        int overlapping = 0;
        for (SomeShapesfromNYC someShapesfromNYC : someShapesfromNYCS) {
            String location = someShapesfromNYC.getLocation();

            S2Region region = WKTConverter.convertWKTToS2Region(location);
            S2CellUnion cellUnion = coverer.getCovering(region);
            ArrayList<S2CellId> output = new ArrayList<>();
            cellUnion.denormalize(MAX_LEVEL, 1, output);


            if (output.size() > 2) {
                overlapping++;
//                System.out.println("Numbe cells in " + someShapesfromNYC.getId() + " is " + output.size());
            }


            for (S2CellId s2CellId : output) {
                Integer hit = celltoFeatureCountMap.get(s2CellId);
                if (hit == null) {
                    System.out.println("not hit");
                } else {
                    hit = hit + 1;
                    celltoFeatureCountMap.put(s2CellId, hit);
                }
            }


        }

        System.out.println("overlapping = " + overlapping);
        System.out.println("celltoFeatureCountMap size " + celltoFeatureCountMap.size());

        return celltoFeatureCountMap;
    }


    public static List<SomeShapesfromNYC> findAllTracts(ElasticsearchClient esClient) throws IOException {

        SearchResponse<SomeShapesfromNYC> response = esClient.search(s -> s
                .index("tl_2023_48_tract")
                .query(q -> q.matchAll(v -> v.queryName(""))
                ).size(9000), SomeShapesfromNYC.class);


        List<Hit<SomeShapesfromNYC>> hits = response.hits().hits();

        System.out.println("num hits " + hits.size());

        List<SomeShapesfromNYC> someShapesfromNYCS = new ArrayList<>();

        for (Hit<SomeShapesfromNYC> hit : hits) {
            SomeShapesfromNYC someShapesfromNYC = hit.source();
//            System.out.println("Name: " + someShapesfromNYC.getAttributeMap().get("NAME") +
//                    " Location " + someShapesfromNYC.getLocation());
            someShapesfromNYCS.add(someShapesfromNYC);
        }

        return someShapesfromNYCS;

    }

}
