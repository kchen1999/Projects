import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;

public class Node {
    private long id;
    private String name;
    private double lat;
    private double lon;
    private Set<Edge> adj;

    public Node(long id, double lon, double lat) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.adj = new HashSet<Edge>();
    }

    public long getId() {
        return this.id;
    }

    public double getLat() {
        return this.lat;
    }

    public double getLon() {
        return this.lon;
    }

    public Set<Edge> getAdj() {
        return this.adj;
    }

    public void addAdj(Long wayId, String name, Long endNodeId) {
        this.adj.add(new Edge(wayId, name, endNodeId));
    }

}
