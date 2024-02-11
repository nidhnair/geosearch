//package com.corelogic.li.shard;
//
//import com.google.common.geometry.*;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class S2CellSplitter {
//
//    public static void main(String[] args) {
//        // Define the number of points in the large S2 cell and the desired number of smaller cells
//        int totalPoints = 1000000;
//        int numSmallerCells = 10;
//        int pointsPerSmallerCell = totalPoints / numSmallerCells;
//
//        // Define the large S2 cell covering the USA
//        S2LatLngRect largeRect = S2LatLngRect.fromPointPair(
//                S2LatLng.fromDegrees(24.396308, -125.0),  // Southwest corner of the USA
//                S2LatLng.fromDegrees(49.384358, -66.93457) // Northeast corner of the USA
//        );
//        S2RegionCoverer coverer = S2RegionCoverer.builder().setMaxLevel(10).setMinLevel(10).setMaxCells(50).build();
//
//        S2CellUnion largeCell = coverer.getCovering(largeRect);
//
//        // Calculate the density of points per unit area in the large S2 cell
//        double largeCellArea = 0.0;
//        for (S2CellId cellId : largeCell.cellIds()) {
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
//
//        // Output the results
//        System.out.println("Number of smaller cells: " + smallerCells.size());
//        for (int i = 0; i < smallerCells.size(); i++) {
//            System.out.println("Smaller Cell " + (i + 1) + ": " + smallerCells.get(i).toString());
//        }
//    }
//}
