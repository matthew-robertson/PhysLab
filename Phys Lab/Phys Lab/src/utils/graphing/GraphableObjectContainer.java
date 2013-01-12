package utils.graphing;

import java.io.Serializable;

public class GraphableObjectContainer
		implements Serializable
{
	private static final long serialVersionUID = 1L;
	private GraphableObject positionTimeGraph;
	private GraphableObject velocityTimeGraph;
	private GraphableObject accelerationTimeGraph;
	
	public GraphableObjectContainer(GraphableObject positionTime, GraphableObject velocityTime, GraphableObject accelerationTime)
	{
		this.positionTimeGraph = positionTime;
		this.velocityTimeGraph = velocityTime;
		this.accelerationTimeGraph = accelerationTime;	
	}

	public GraphableObjectContainer()
	{
		this.positionTimeGraph = new GraphableObject();
		this.velocityTimeGraph = new GraphableObject();
		this.accelerationTimeGraph = new GraphableObject();
	}
	
	public GraphableObjectContainer setPositionTimeGraph(GraphableObject ptg)
	{
		this.positionTimeGraph = ptg;
		return this;
	}
	
	public GraphableObjectContainer setVelocityTimeGraph(GraphableObject vtg)
	{
		this.velocityTimeGraph = vtg;
		return this;
	}
	
	public GraphableObjectContainer setAccelerationTimeGraph(GraphableObject atg)
	{
		this.accelerationTimeGraph = atg;
		return this;
	}
	
	public GraphableObject getPositionTimeGraph()
	{
		return positionTimeGraph;
	}
	
	public GraphableObject getVelocityTimeGraph()
	{
		return velocityTimeGraph;
	}
	
	public GraphableObject getAccelerationTimeGraph()
	{
		return accelerationTimeGraph;
	}
}
