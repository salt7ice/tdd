package data;

import com.example.core.domain.Edge;
import com.example.core.domain.GraphMap;
import com.example.core.exceptions.MapNotFoundException;
import com.example.core.port.IportMapsRepository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ParserCSV implements IportMapsRepository {
    private static final String DELIMITER = ";";


    @Override
    public GraphMap getGraphMap(int graphNr) throws MapNotFoundException {
        String filePath;
        filePath = (graphNr == 1 ? "/Users/kallelskander/Desktop/shortest path/architectureHexProject/tdd/bigMap.csv" :
                "/Users/kallelskander/Desktop/shortest path/architectureHexProject/tdd/smallMap.csv"
        );


        GraphMap map = new GraphMap();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(DELIMITER);

                map.addEdge(data[0], data[1], Integer.parseInt(data[2]), Integer.parseInt(data[3]), data[4]);
            }

        } catch (Exception e) {
            throw new MapNotFoundException("Map not found!", e);
        }
        return map;

    }
}
