package data;

import com.example.core.domain.GraphMap;
import com.example.core.port.IportMapsRepository;

public class MapsRepository implements IportMapsRepository {

    @Override
    public GraphMap getGraphMap(int graphNr) {
        GraphMap graph1 = new GraphMap();
        graph1.addEdge("Bézier", "Montpellier", 83, 70, "RN113");
        graph1.addEdge("Montpellier", "Avignon", 80, 80, "RN84");
        graph1.addEdge("Avignon", "Arles", 36, 100, "RN96");
        graph1.addEdge("Arles", "Bézier", 145, 110, "RN50");
        graph1.addEdge("Bézier", "Avignon", 150, 130, "A9");

        GraphMap graph2 = new GraphMap();
        graph2.addEdge("Bézier", "Montpellier", 83, 70, "RN113");
        graph2.addEdge("Montpellier", "Avignon", 80, 80, "RN84");
        graph2.addEdge("Avignon", "Arles", 36, 100, "RN96");
        graph2.addEdge("Arles", "Bézier", 145, 110, "RN50");
        return (graphNr==1?graph1:graph2);
    }
}
