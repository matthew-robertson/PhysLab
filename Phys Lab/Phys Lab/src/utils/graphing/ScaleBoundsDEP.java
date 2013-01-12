package utils.graphing;

public class ScaleBoundsDEP
{	
	public float left;
	public float right;
	public float top;
	public float bottom;	
	public Point leftPivotPoint;
	public Point rightPivotPoint;	
	public float rotationAngle;
	
	public ScaleBoundsDEP()
	{
		left = 0;
		right = 0;
		top = 0;
		bottom = 0;
		leftPivotPoint = new Point(0, 0);
		rightPivotPoint = new Point(0, 0);
		rotationAngle = 0.0f;
		fixPivotPoints();
	}

	/**
	 * Does not set other variables
	 * @param l
	 * @param r
	 * @param t
	 * @param b
	 */
	public ScaleBoundsDEP(float l, float r, float t, float b)
	{
		left = l;
		right = r;
		top = t;
		bottom = b;
		leftPivotPoint = new Point(0, 0);
		rightPivotPoint = new Point(0, 0);
		rotationAngle = 0.0f;
		fixPivotPoints();	
	}
	
	public ScaleBoundsDEP(float l, float r, float t, float b, Point lPP, Point rPP)
	{
		left = l;
		right = r;
		top = t;
		bottom = b;
		leftPivotPoint = lPP;
		rightPivotPoint = rPP;
		rotationAngle = 0;	
		fixPivotPoints();
	}
	
	public ScaleBoundsDEP(float l, float r, float t, float b, Point lPP, Point rPP, float rA)
	{
		left = l;
		right = r;
		top = t;
		bottom = b;
		leftPivotPoint = lPP;
		rightPivotPoint = rPP;
		rotationAngle = rA;
		fixPivotPoints();
	}
	
	/**
	 * Does not auto-set other variables
	 * @param l
	 * @param r
	 * @param t
	 * @param b
	 */
	public void setBounds(float l, float r, float t, float b)
	{
		left = l;
		right = r;
		top = t;
		bottom = b;
		fixPivotPoints();
	}
	
	public void setBounds(float l, float r, float t, float b, Point lPP, Point rPP)
	{
		left = l;
		right = r;
		top = t;
		bottom = b;
		leftPivotPoint = lPP;
		rightPivotPoint = rPP;
		rotationAngle = 0.0f;
		fixPivotPoints();
	}
	
	public void setBounds(float l, float r, float t, float b, Point lPP, Point rPP, float rA)
	{
		left = l;
		right = r;
		top = t;
		bottom = b;
		leftPivotPoint = lPP;
		rightPivotPoint = rPP;
		rotationAngle = rA;
		fixPivotPoints();
	}
	
	public void fixPivotPoints()
	{
		leftPivotPoint.setX(left);
		leftPivotPoint.setY(top + ((bottom - top) / 2));
		rightPivotPoint.setX(right);
		rightPivotPoint.setY(top + ((bottom - top) / 2));		
	}
	
	public boolean contains(float x, float y)
	{
		//Counter rotate
		
		
		if(x > this.left && x < this.right && y > this.top && y < this.bottom)
		{
			return true;
		}
		return false;
	}
	
	public void recalculateBoundsByPivotPoints()
	{
		
	}
	
	public void recalculateClickBounds()
	{
		
	}
	
	public boolean isInLeftClickBounds()
	{
		return true;
	}
	
	public boolean isInRightClickBounds()
	{
		return true;
	}
}