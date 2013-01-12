package utils.graphing;

import java.io.Serializable;

public class GraphableObject 
		implements Graphable, Serializable
{
	private static final long serialVersionUID = 1L;
	private Point[] points;
	private double minValue;
	private double maxValue;
	private int graphType;
	private double minValueY;
	private double maxValueY;
	
	public GraphableObject(Point[] points, double minValue, double maxValue, int graphType, double minValueY, double maxValueY)
	{
		this.points = points;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.graphType = graphType;
		this.minValueY = minValueY;
		this.maxValueY = maxValueY;
	}
	
	public GraphableObject()
	{
		this.points = new Point[10];
		this.minValue = 0;
		this.maxValue = 0;
		this.graphType = 0;
		this.minValueY = 0;
		this.maxValueY = 0;
	}
	
	public int getGraphType()
	{
		return graphType;
	}
	
	public Point getFirstPoint()
	{
		return (points.length > 0) ? points[0] : null;
	}
	
	public Point getLastPoint()
	{
		return (points.length > 0) ? points[points.length - 1] : null;
	}
	
	public GraphableObject setGraphType(int i)
	{
		this.graphType = i;
		return this;
	}
	
	public GraphableObject setMinValue(double d)
	{
		this.minValue = d;
		return this;
	}
	
	public GraphableObject setMaxValue(double d)
	{	
		this.maxValue = d;
		return this;
	}
	
	public double getMinValue()
	{
		return minValue;
	}
	
	public double getMaxValue()
	{
		return maxValue;
	}
	
	public Point[] getPoints()
	{
		return points;
	}
	
	public GraphableObject setPoints(Point[] points)
	{
		this.points = points;
		return this;
	}
	

	public GraphableObject setMinValueY(double d)
	{
		this.minValueY = d;
		return this;
	}
	
	public GraphableObject setMaxValueY(double d)
	{	
		this.maxValueY = d;
		return this;
	}
	
	public double getMinValueY()
	{
		return minValueY;
	}
	
	public double getMaxValueY()
	{
		return maxValueY;
	}
	
	public GraphableObject changePoint(int index, Point point)
	{
		try
		{
			this.points[index] = point;
		}
		catch(Exception e) //Invalid index Error
		{
			e.printStackTrace();
		}
		
		return this;
	}
}