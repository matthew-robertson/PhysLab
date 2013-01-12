package utils.graphing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GraphData	 
		implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String videoPath;
	private List<Point> points;
	private double graphScale;
	
	public GraphData()
	{
		videoPath = "";
		graphScale = 0;
		points = new ArrayList<Point>();
	}
	
	public GraphData(String path, List<Point> points, double graphscale)
	{
		this.graphScale = graphscale;
		this.videoPath = path;
		this.points = points;
	}
	
	public GraphData setVideoPath(String str)
	{
		videoPath = str;
		return this;
	}
	
	public String getVideoPath()
	{
		return videoPath;
	}
	
	public GraphData addPoint(Point point)
	{
		points.add(point);
		return this;
	}
	
	public Point getPointAtIndex(int index)
	{
		return points.get(index);
	}
	
	public List getPoints()
	{
		return points;
	}
	
	public Point[] getPointsAsArray()
	{
		Point[] newPoints = new Point[points.size()];
		for(int i = 0; i < points.size(); i++)
		{
			newPoints[i] = points.get(i);
		}
		return newPoints;
	}
}
