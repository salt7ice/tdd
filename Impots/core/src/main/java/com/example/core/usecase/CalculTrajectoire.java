package com.example.core.usecase;

import com.example.core.domain.GraphMap;
import com.example.core.port.IportMapsRepository;

import java.util.List;

public class CalculTrajectoire {
    private final IportMapsRepository iportMapsRepository;

    public CalculTrajectoire(IportMapsRepository iportMapsRepository) {
        this.iportMapsRepository = iportMapsRepository;
    }

    public List<String> execute(int mapNr,String start, String end, boolean isSansAutoroutes, boolean isSpeedChoice) {
        GraphMap graphMap = iportMapsRepository.getGraphMap(mapNr);
        graphMap.setStart(start);
        graphMap.setEnd(end);
        return (isSpeedChoice?graphMap.getFastestPath(isSansAutoroutes) :graphMap.getShortestTrajectory(isSansAutoroutes) );
    }
}
