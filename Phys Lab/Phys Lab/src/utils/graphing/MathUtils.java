package utils.graphing;


public class MathUtils 
{
	public static double lineLength(Point point1, Point point2)
    {
    	return Math.sqrt(Math.pow((point2.getX() - point1.getX()), 2) + Math.pow((point2.getY() - point1.getY()), 2));
    }
    
    public static double lineLength(double x1, double y1, double x2, double y2)
	{
		return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
	}
}
