package gui.activities;

import java.awt.Point;

public class Line2D {

	public static void main(String[] args)
	{
		Point point3 = new Point(45, -45);
		Point point2 = new Point(-90, 0);
		Point point1 = new Point(0, 0);
		
		double d= angleBetweenTwoVectors(point1.x, point1.y, point2.x, point2.y, point3.x, point3.y);
		
		
		
		System.out.println(d);
	}
	
	public static double angleBetweenTwoVectors(double x1, double y1, double x2, double y2, double x3, double y3)
	{
		/*
		double l1x = x2 - x1;
		double l1y = y2 - y1;
		double l2x = x3 - x1;
		double l2y = y3 - y1;
		//float tan1 = (float)l1y/(float)l1x;
		//float tan2 = (float)l2y/(float)l2x;
		double ang1 = Math.atan2(l1y, l1x);
		double ang2 = Math.atan2(l2y, l2x);
		return (Math.toDegrees(ang2-ang1));
		//*/
		 double angle1 = Math.atan2(y1 - y2,
                 x1 - x2);
double angle2 = Math.atan2(y1 - y3,
             x1 - x3);
return Math.toDegrees(angle1-angle2);

	//*/
	
	}
}
