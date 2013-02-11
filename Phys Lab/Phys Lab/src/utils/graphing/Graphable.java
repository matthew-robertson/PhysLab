package utils.graphing;

public interface Graphable

{
	public static final int GRAPH_TYPE_NONE = 0,
							   GRAPH_TYPE_POSITION_TIME = 1,
							   GRAPH_TYPE_VELOCITY_TIME = 2,
							   GRAPH_TYPE_ACCELERATION_TIME = 3,
							   GRAPH_TYPE_X_TIME = 4, 
							   GRAPH_TYPE_Y_TIME = 5;
	
	public abstract int getGraphType();
}
