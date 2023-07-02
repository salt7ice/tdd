package com.example.core.port;

import com.example.core.domain.GraphMap;
import com.example.core.exceptions.MapNotFoundException;

public interface IportMapsRepository {
    public GraphMap getGraphMap(int graphNr) throws MapNotFoundException;
}
