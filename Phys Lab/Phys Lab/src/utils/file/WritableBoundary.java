package utils.file;

import java.io.Serializable;

import utils.graphing.Point;
import utils.graphing.WritablePoint;

public class WritableBoundary 
		implements Serializable
{
	private static final long serialVersionUID = 1L;
	public WritablePoint[] points; // Points making up the boundary   
    public double totalRot = 0;
    
    public void setPoints(Point[] aPoints)
    {
    	this.points = new WritablePoint[aPoints.length];
    	
    	for(int i = 0; i < points.length; i++)
    	{
    		points[i] = new WritablePoint(aPoints[i]);
    	}
    }
    
}
