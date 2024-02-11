package com.corelogic.li.shard;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.corelogic.li.geosearch.ElasticSearchClientCreator;
import com.google.common.geometry.*;

import java.util.HashMap;
import java.util.Map;

public class S2CellSplitter {

    public static void main(String[] args) {
        // Define the number of points in the large S2 cell and the desired number of smaller cells
        int totalPoints = 1000000;
        int numSmallerCells = 10;

//        ElasticsearchClient es = ElasticSearchClientCreator.createElasticsearchClient();
//
//        createTexasCell();


        // Calculate the density of points per unit area in the large S2 cell
//        double largeCellArea = 0.0;
//        for (S2CellId cellId : allTexasCells.cellIds()) {
//            S2Cell cell = new S2Cell(cellId);
//            largeCellArea += cell.exactArea();
//        }
//        double densityPerArea = totalPoints / largeCellArea;
//
//        // Split the large S2 cell into smaller cells
//        List<S2CellId> smallerCells = new ArrayList<>();
//        for (S2CellId cellId : largeCell.cellIds()) {
//            S2Cell cell = new S2Cell(cellId);
//            int cellPoints = (int) (densityPerArea * cell.exactArea());
//            int numSplitCells = Math.max(1, Math.min(numSmallerCells, cellPoints / pointsPerSmallerCell));
//            S2RegionCoverer splitCoverer =  S2RegionCoverer.builder().setLevelMod(2).setMaxCells(numSplitCells).build();
//
//            S2CellUnion splitCells = splitCoverer.getCovering(cell);
//            smallerCells.addAll(splitCells.cellIds());x
//        }

        // Output the results
//        System.out.println("Number of smaller cells: " + smallerCells.size());
//        for (int i = 0; i < smallerCells.size(); i++) {
//            System.out.println("Smaller Cell " + (i + 1) + ": " + smallerCells.get(i).toString());
//        }
    }

    private  Map<S2CellId, Integer> createTexasCell() {

        ElasticsearchClient es = ElasticSearchClientCreator.createElasticsearchClient();
        // Define the bounding box covering Texas
        S2LatLngRect texasboundingbox = S2LatLngRect.fromPointPair(
                S2LatLng.fromDegrees(25.837377, -106.645646),  // Southwest corner of the USA
                S2LatLng.fromDegrees(36.500704, -93.508292) // Northeast corner of the USA
        );
        S2RegionCoverer coverer = S2RegionCoverer.builder().setMaxLevel(10).setMinLevel(10).build();

        S2CellUnion allTexasCells = coverer.getCovering(texasboundingbox);

        Map<S2CellId,Integer> celltoFeatureCountMap = new HashMap<>();
        System.out.println("Total num cells " + allTexasCells.size());
        for (S2CellId cellId : allTexasCells.cellIds()) {
            celltoFeatureCountMap.put(cellId, 0);
        }

        return celltoFeatureCountMap;
    }
}
