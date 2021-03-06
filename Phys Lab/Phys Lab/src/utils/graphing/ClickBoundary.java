package utils.graphing;

import java.io.Serializable;

public class ClickBoundary 
		implements Serializable
{
	private static final long serialVersionUID = 1L;
	public final Point[] points; // Points making up the boundary
    
	public ClickBoundary()
	{
		this.points = new Point[4];
	}	
		
	public ClickBoundary(Point[] points)
    {
    	this.points = points;
    }

	public boolean contains(Point test) 
    {
    	int i;
    	int j;
    	boolean result = false;
    	for (i = 0, j = points.length - 1; i < points.length; j = i++) 
    	{
    		if ((points[i].y > test.y) != (points[j].y > test.y) &&
    			(test.x < (points[j].x - points[i].x) * (test.y - points[i].y) / (points[j].y-points[i].y) + points[i].x)) {
    			result = !result;
    		}
    	}
    	return result;
    }
    
	public boolean contains(double x, double y) 
    {
    	int i;
    	int j;
    	boolean result = false;
    	for (i = 0, j = points.length - 1; i < points.length; j = i++) 
    	{
    		if ((points[i].y > y) != (points[j].y > y) &&
    			(x < (points[j].x - points[i].x) * (y - points[i].y) / (points[j].y-points[i].y) + points[i].x)) {
    			result = !result;
    		}
    	}
    	return result;
    }
	
	public Point rotate_point(float cx, float cy, float angle, Point p)
	{
	  double s = Math.sin(angle);
	  double c = Math.cos(angle);
	
	  // translate point back to origin:
	  p.x -= cx;
	  p.y -= cy;
	
	  // rotate point
	  double xnew = p.x * c - p.y * s;
	  double ynew = p.x * s + p.y * c;
	
	  // translate point back:
	  p.x = xnew + cx;
	  p.y = ynew + cy;
	  
	  return p;
	}
}
