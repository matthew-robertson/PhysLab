package utils.graphing;

import java.io.Serializable;

import android.widget.ImageView;

public class WritablePoint 
		implements Serializable
{
	private static final long serialVersionUID = 1L;
	public double x;
	public double y;
	public double timeStamp;

	public WritablePoint(WritablePoint point)
	{
		this.x = point.x;
		this.y = point.y;
		this.timeStamp = point.timeStamp;
	}
	
	public WritablePoint()
	{
		x = 0;
		y = 0;
		timeStamp = 0;
	}
	
	public WritablePoint(double x, double y)
	{
		this.x = x;
		this.y = y;
		timeStamp = 0;
	}
	
	public WritablePoint(double x, double y, double time)
	{
		this.x = x;
		this.y = y;
		this.timeStamp = time;
	}
	
	public WritablePoint setX(double x)
	{
		this.x = x;
		return this;
	}
	
	public WritablePoint setY(double y)
	{
		this.y = y;
		return this;
	}
	
	public WritablePoint setTimeStamp(double t)
	{
		this.timeStamp = t;
		return this;
	}
	
	public double getTimeStamp()
	{
		return timeStamp;
	}
	
	public double getX()
	{
		return x;
	}
	
	public double getY()
	{
		return y;
	}
	
	 /**
     * Compares a point to this point by time value
	 * @return [this is invalid]-1 if the time value is lower, 0 if the time value is the same, 1 if the time value is greater
     */
	public int compareTo(Point point)
	{
		return (point.timeStamp < timeStamp) ? 1 : (point.timeStamp == timeStamp) ? 0 : -1; 
	}
	
	//Point labels? X="", Y=""?

}
