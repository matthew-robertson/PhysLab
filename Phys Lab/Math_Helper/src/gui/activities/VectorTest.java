package gui.activities;

public class VectorTest {

	public double angleBetweenTwoVectors(double x1, double y1, double x2, double y2, double x3, double y3)
	{
		double angle1 = Math.atan2(y1 - y2, x1 - x2);
		double angle2 = Math.atan2(y1 - y3, x1 - x3);
		return Math.toDegrees(angle1-angle2);
	}
	
	public static void main(String[] args)
	{
		
	}
}
