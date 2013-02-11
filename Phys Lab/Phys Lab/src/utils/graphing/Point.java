package utils.graphing;

import android.widget.ImageView;

public class Point extends WritablePoint
{
	private static final long serialVersionUID = 1L;
	public ImageView imageView;
	
	public Point(Point point)
	{
		this.imageView = point.imageView;
		this.x = point.x;
		this.y = point.y;
		this.timeStamp = point.timeStamp;
	}
	
	public Point(WritablePoint point)
	{
		this.x = point.x;
		this.y = point.y;
		this.timeStamp = point.timeStamp;
	}
	
	public Point()
	{
		x = 0;
		y = 0;
		timeStamp = 0;
	}
	
	public Point(double x, double y)
	{
		this.x = x;
		this.y = y;
		timeStamp = 0;
	}
	
	public Point(double x, double y, ImageView image)
	{
		this.x = x;
		this.y = y;
		this.imageView = image;
		timeStamp = 0;
	}	
	
	public Point(double x, double y, double time)
	{
		this.x = x;
		this.y = y;
		this.timeStamp = time;
	}
	
	public Point(double x, double y, double timeStamp, ImageView image)
	{
		this.x = x;
		this.y = y;
		this.imageView = image;
		this.timeStamp = timeStamp;
	}	

	public Point setImageView(ImageView image)
	{
		this.imageView = image;
		return this;
	}
	
	public Point setTimeStamp(double t)
	{
		this.timeStamp = t;
		return this;
	}
	
	public ImageView getImageView()
	{
		return imageView;
	}
	
	//Point labels? X="", Y=""?
}
