

public class Edge {
    private long wayId;
    private String way;
    private long endNodeId;

    public Edge(long wayId, String way, Long endNodeId) {
        this.wayId = wayId;
        this.way = way;
        this.endNodeId = endNodeId;
    }

    public long getEndNodeId() {
        return endNodeId;
    }

    public long getWayId() {
        return wayId;
    }

    public String getWay() {
        return way;
    }
}