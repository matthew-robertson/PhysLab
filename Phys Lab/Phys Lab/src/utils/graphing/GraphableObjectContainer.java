package utils.graphing;

import java.io.Serializable;

public class GraphableObjectContainer
		implements Serializable
{
	private static final long serialVersionUID = 1L;
	private GraphableObject positionTimeGraph;
	private GraphableObject velocityTimeGraph;
	private GraphableObject accelerationTimeGraph;
	private GraphableObject xTimeGraph;
	private GraphableObject yTimeGraph;
	
	public GraphableObjectContainer()
	{
		this.positionTimeGraph = new GraphableObject();
		this.velocityTimeGraph = new GraphableObject();
		this.accelerationTimeGraph = new GraphableObject();
		this.xTimeGraph = new GraphableObject();
		this.yTimeGraph = new GraphableObject();
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
	
	public GraphableObjectContainer setXTimeGraph(GraphableObject obj)
	{
		this.xTimeGraph = obj;
		return this;
	}
	
	public GraphableObjectContainer setYTimeGraph(GraphableObject obj)
	{
		this.yTimeGraph = obj;
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
	
	public GraphableObject getXTimeGraph()
	{
		return xTimeGraph;
	}
	
	public GraphableObject getYTimeGraph()
	{
		return yTimeGraph;
	}
}
