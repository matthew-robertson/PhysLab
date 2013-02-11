package utils.drawing;

import global.StaticVariables;
import gui.activities.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import utils.graphing.Graphable;
import utils.graphing.GraphableObjectContainer;
import utils.graphing.Point;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Panel extends SurfaceView implements SurfaceHolder.Callback
{
    private DrawingThread _thread;
    private int graphType = 1;
	private GraphableObjectContainer graphable;
		
    public Panel(Context context, AttributeSet attrs, int defStyle) 
    {
        super(context, attrs, defStyle);
    }

    public Panel(Context context, AttributeSet attrs) 
    {
        super(context, attrs);
    }
    
    public Panel(Context context) 
    {
        super(context);
        getHolder().addCallback(this);
        _thread = new DrawingThread(getHolder(), this);
    }

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
	{
	}

	public void surfaceCreated(SurfaceHolder holder) 
	{
	    _thread.setRunning(true);
	    _thread.start();
	}
	 
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
	    // simply copied from sample application LunarLander:
	    // we have to tell thread to shut down & wait for it to finish, or else
	    // it might touch the Surface after we return and explode
	    boolean retry = true;
	    _thread.setRunning(false);
	    while (retry) 
	    {
	        try 
	        {
	            _thread.join();
	            retry = false;
	        } catch (InterruptedException e) 
	        {
	            // we will try it again and again...
	        }
	    }
	}
	
	public void setGraphableObjectContainer(GraphableObjectContainer graphable)
	{
		this.graphable = graphable;
	}
	
	public void setGraphType(int i)
	{
		this.graphType = i;
	}
	
	public double roundToTwoDecimals(double d)
	{
		return ((double)Math.round(d * 100) / 100);
	}
	
	public String makeNumberSafe(double d)
	{
		if(d == 0)
		{
			return "0";
		}
		else if((d <= 100.00D && d >= 1.00D) || (d <= -1.00D && d >= -100.00D))
		{
			return ""+roundToTwoDecimals(d);
		}
		else 
		{	
			NumberFormat formatter = new DecimalFormat("0.##E0");
			return formatter.format(d);
		}
	}
	
	public double roundUp(double d)
	{
		return d;//Math.round(d * 10) / 10;
	}
	
	public void onDraw(Canvas canvas) 
	{
		try
		{
			canvas.drawColor(Color.BLACK);
			Paint paint = new Paint();
			paint.setColor(Color.WHITE);
			
			Paint paintYellow = new Paint();
			paintYellow.setColor(Color.YELLOW);
			
			//Only the positive quadrant in use
			final int positive_leftPadding = (int) (StaticVariables.mainProject.width / 8);
			final int positive_rightPadding = (int) (StaticVariables.mainProject.width / 8);
			final int positive_bottomPadding = (int) (StaticVariables.mainProject.height / 24);
			final int positive_topPadding = (int) (StaticVariables.mainProject.height / 24);
			final int positive_horizontalAxisPadding = positive_leftPadding;
			final int positive_verticalAxisPadding = this.getHeight() - positive_bottomPadding;
			//Only the negative quadrant in use
			final int negative_leftPadding = (int) (StaticVariables.mainProject.width / 16);
			final int negative_rightPadding = (int) (StaticVariables.mainProject.width / 16);
			final int negative_bottomPadding = (int) (StaticVariables.mainProject.height / 24);
			final int negative_topPadding = (int) (StaticVariables.mainProject.height / 24);
			final int negative_horizontalAxisPadding = ((getWidth() - negative_rightPadding));
			final int negative_verticalAxisPadding = negative_topPadding;
			
			//Both quadrants on an axis in use
			final int both_leftPadding = (int) (StaticVariables.mainProject.width / 16);
			final int both_rightPadding = (int) (StaticVariables.mainProject.width / 16);
			final int both_bottomPadding = (int) (StaticVariables.mainProject.height / 24);
			final int both_topPadding = (int) (StaticVariables.mainProject.height / 24);
			final int both_horizontalAxisPadding = both_leftPadding + ((getWidth() - both_leftPadding - both_rightPadding) / 2);
			final int both_verticalAxisPadding = both_topPadding + ((getHeight() - both_topPadding - both_bottomPadding) / 2);
			
			int leftPadding = 0;
			int rightPadding = 0;
			int bottomPadding = 0;
			int topPadding = 0;
			int horizontalAxisPadding = 0;
			int verticalAxisPadding = 0;
						
			//X bound values
			double max = 0.0D;
			double min = 0.0D;
			
			//Y Bound Values
			double h_max = 0.0D;
			double h_min = 0.0D;
			
			Point[] points; //Graph points
			
			if(graphType == Graphable.GRAPH_TYPE_ACCELERATION_TIME) //A/T
			{
				max = graphable.getAccelerationTimeGraph().getMaxValue();
				min = graphable.getAccelerationTimeGraph().getMinValue();
				h_max = graphable.getAccelerationTimeGraph().getMaxValueY();
				h_min = graphable.getAccelerationTimeGraph().getMinValueY();
				points = graphable.getAccelerationTimeGraph().getPoints();
			}
			else if(graphType == Graphable.GRAPH_TYPE_VELOCITY_TIME) //V/T
			{
				max = graphable.getVelocityTimeGraph().getMaxValue();
				min = graphable.getVelocityTimeGraph().getMinValue();
				h_max = graphable.getVelocityTimeGraph().getMaxValueY();
				h_min = graphable.getVelocityTimeGraph().getMinValueY();
				points = graphable.getVelocityTimeGraph().getPoints();
			}
			else if(graphType == Graphable.GRAPH_TYPE_X_TIME) //X/T
			{
				max = graphable.getXTimeGraph().getMaxValue();
				min = graphable.getXTimeGraph().getMinValue();
				h_max = graphable.getXTimeGraph().getMaxValueY();
				h_min = graphable.getXTimeGraph().getMinValueY();
				points = graphable.getXTimeGraph().getPoints();
			}
			else if(graphType == Graphable.GRAPH_TYPE_Y_TIME) //Y/T
			{
				max = graphable.getYTimeGraph().getMaxValue();
				min = graphable.getYTimeGraph().getMinValue();
				h_max = graphable.getYTimeGraph().getMaxValueY();
				h_min = graphable.getYTimeGraph().getMinValueY();
				points = graphable.getYTimeGraph().getPoints();
			}
			else //P/T
			{
				max = graphable.getPositionTimeGraph().getMaxValue();
				min = graphable.getPositionTimeGraph().getMinValue();
				h_max = graphable.getPositionTimeGraph().getMaxValueY();
				h_min = graphable.getPositionTimeGraph().getMinValueY();
				points = graphable.getPositionTimeGraph().getPoints();
			}
			
			//
			if(max == 0.0D)
				max = 1.0D;
			if(h_max == 0.0D)
				h_max = 1.0D;			
			//
			
			if(min >= 0.0D) //Positive X Quadrant only
			{
				leftPadding = positive_leftPadding;
				rightPadding = positive_rightPadding;
				horizontalAxisPadding = positive_horizontalAxisPadding;
				min = 0;
			}
			else if(max <= 0.0D) //Negative Quadrant (X) only
			{
				leftPadding = negative_leftPadding;
				rightPadding = negative_rightPadding;
				horizontalAxisPadding = negative_horizontalAxisPadding;
				max = 0.0D;
			}
			else //Need both X quadrants
			{
				leftPadding = both_leftPadding;
				rightPadding = both_rightPadding;
				horizontalAxisPadding = both_horizontalAxisPadding;
				double maxRange = max;
				double minRange = Math.abs(min);
				
				if(maxRange > minRange)
				{
					min = maxRange * -1;
				}
				else if(maxRange < minRange)
				{
					max = minRange;
				}
			}
			
			if(h_min >= 0.0D) //Positive quadrant (Y) only
			{
				topPadding = positive_topPadding;
				bottomPadding = positive_bottomPadding;
				verticalAxisPadding = positive_verticalAxisPadding;
				h_min = 0;
			}
			else if(h_max < 0.0D) //Negative quadrant (Y) only
			{
				topPadding = negative_topPadding;
				bottomPadding = negative_bottomPadding;
				verticalAxisPadding = negative_verticalAxisPadding;
				h_max = 0.0D;
			}
			else //Need both Y quadrants
			{
				topPadding = both_topPadding;
				bottomPadding = both_bottomPadding;
				verticalAxisPadding = both_verticalAxisPadding;
			
				double maxRange = h_max;
				double minRange = Math.abs(h_min);
				
				if(maxRange > minRange)
				{
					h_min = maxRange * -1;
				}
				else if(maxRange < minRange)
				{
					h_max = minRange;
				}
			}
			
			max = roundUp(max);
			h_max = roundUp(h_max);
			
			//Draw X axis
			canvas.drawLine(leftPadding, verticalAxisPadding, this.getWidth() - rightPadding, verticalAxisPadding, paint);
			
			//int arb_off = (int) (StaticVariables.mainProject.width / 160);
			
			Resources res = getResources();
			//float fontSize = res.getDimension(R.dimen.font_size);
			//
			int scaledSize = res.getDimensionPixelSize(R.dimen.panel_font_size);
			
			paint.setTextSize(scaledSize);
			
			for(int i = 0; i < 5; i++)
			{
				String text = makeNumberSafe(min + ((max - min) * (i * 0.25D)));
				
				Rect textBounds = new Rect();
				paint.getTextBounds(text, 0, text.length(), textBounds);
				//textBounds = new Rect(arb_off, 0, leftPadding - arb_off, (int) ((StaticVariables.mainProject.height / 12)));
				
				int j = (int)(leftPadding + ((getWidth() - leftPadding - rightPadding) * (i * 0.25D)) - (textBounds.width() * 0.5D));
				int k = verticalAxisPadding + 2 + textBounds.height() - textBounds.bottom;
				
				canvas.drawText(text, 
						j, 
						k, 
						paint);
				canvas.drawLine(leftPadding + (int)((getWidth() - leftPadding - rightPadding) * (i * 0.25D)), 
						verticalAxisPadding - 3,
						leftPadding + (int)((getWidth() - leftPadding - rightPadding) * (i * 0.25D)), 
						verticalAxisPadding + 3, 
						paintYellow);
			}
			
			//Draw Y axis
			canvas.drawLine(horizontalAxisPadding, topPadding, horizontalAxisPadding, (this.getHeight() - bottomPadding), paint);
				
			for(int i = 0; i < 5; i++)
			{
				String h_text = makeNumberSafe(h_min + ((h_max - h_min) * ((4 - i) * 0.25D)));
				
				Rect h_textBounds = new Rect();
				paint.getTextBounds(h_text, 0, h_text.length(), h_textBounds);
				canvas.drawText(h_text,
						horizontalAxisPadding - h_textBounds.width() - 2, 
						(int)(topPadding + ((getHeight() - topPadding - bottomPadding) * (i * 0.25D)) + h_textBounds.height() - (0.5D * h_textBounds.height()) - h_textBounds.bottom), 
						paint);
		
				canvas.drawLine(horizontalAxisPadding - 3, 
						topPadding + (int)((getHeight() - topPadding - bottomPadding) * (i * 0.25D)), 
						horizontalAxisPadding + 3, 
						topPadding + (int)((getHeight() - topPadding - bottomPadding) * (i * 0.25D)), 
						paintYellow);
			}
			
			double verticalGraphUnitSize = (h_min < 0) ? h_max - h_min : h_max;
			double horizontalGraphUnitSize = (min < 0) ? max - min : max;		
			int pointRadius = 2;
			
			/*
			if(verticalGraphUnitSize > 100 || horizontalGraphUnitSize > 100)
			{
				pointRadius = 1;
			}
			*/
			
			double oldX = 0;
			double oldY = 0;
			
			//Graph the points based on the graph
			for(int i = 0; i < points.length; i++)
			{
				double x = 0;
				double y = 0;
				
				if(min >= 0)
				{
					x = (leftPadding + ((getWidth() - leftPadding - rightPadding) * (points[i].getX() / horizontalGraphUnitSize)));
				}
				else if(max <= 0)
				{
					x = (leftPadding + ((getWidth() - leftPadding - rightPadding) * ((points[i].getX() - min) / horizontalGraphUnitSize)));
				}
				else
				{
					if(points[i].getX() < 0)
					{
						x = (leftPadding + ((getWidth() - leftPadding - rightPadding) * ((points[i].getX() - min) / horizontalGraphUnitSize)));
					}
					else
					{
						x = (leftPadding + ((getWidth() - leftPadding - rightPadding) * ((points[i].getX() + (0.5 * horizontalGraphUnitSize)) / horizontalGraphUnitSize)));
					}
				}
				
				if(h_min >= 0)
				{
					y = (topPadding + ((getHeight() - topPadding - bottomPadding) * (1 - (points[i].getY() / verticalGraphUnitSize))));
				}
				else if(h_max <= 0)
				{
					y = (topPadding + ((getHeight() - topPadding - bottomPadding) * ((h_max - points[i].getY()) / verticalGraphUnitSize)));
				}
				else
				{
					if(points[i].getY() < 0)
					{
						y = (topPadding + ((getHeight() - topPadding - bottomPadding) * (Math.abs(points[i].getY()) + (0.5 * verticalGraphUnitSize)) / verticalGraphUnitSize));
					}
					else
					{
						y = (topPadding + ((getHeight() - topPadding - bottomPadding) * ((h_max - points[i].getY()) / verticalGraphUnitSize)));
					}
				}
				
				if(Double.isNaN(x))
				{
					x = leftPadding;
				}
				if(Double.isNaN(y))
				{
					y = topPadding;
				}
				
				canvas.drawCircle((int)x,
						(int)y, 
						pointRadius, 
						paintYellow);
				
				if(i > 0)
				{
					canvas.drawLine((int)oldX, (int)oldY, (int)x, (int)y, paintYellow);
				}
				
				oldX = x;
				oldY = y;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}