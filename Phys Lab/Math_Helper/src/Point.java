

import java.io.Serializable;

import javax.swing.text.html.ImageView;

public class Point 
		implements Serializable
{
	private static final long serialVersionUID = 1L;
	public double x;
	public double y;
	private ImageView imageView;
	
	public Point(Point point)
	{
		this.imageView = point.imageView;
		this.x = point.x;
		this.y = point.y;
	}
	
	public Point()
	{
		x = 0;
		y = 0;
	}
	
	public Point(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	public Point(double x, double y, ImageView image)
	{
		this.x = x;
		this.y = y;
		this.imageView = image;
	}	

	public Point setImageView(ImageView image)
	{
		this.imageView = image;
		return this;
	}
	
	public Point setX(double x)
	{
		this.x = x;
		return this;
	}
	
	public Point setY(double y)
	{
		this.y = y;
		return this;
	}
	
	public double getX()
	{
		return x;
	}
	
	public double getY()
	{
		return y;
	}
	
	public ImageView getImageView()
	{
		return imageView;
	}
	
	 /**
     * Compares a point to this point by X value
	 * @return [this is invalid]-1 if the x value is lower, 0 if the x value is the same, 1 if the x value is greater
     */
	public int compareTo(Point point)
	{
		return (point.getX() < x) ? 1 : (point.getX() == x) ? 0 : -1; 
	}
	
	//Point labels? X="", Y=""?
}
