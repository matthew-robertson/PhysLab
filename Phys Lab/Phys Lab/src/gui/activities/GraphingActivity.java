package gui.activities;

import global.StaticVariables;

import java.util.Arrays;

import utils.drawing.Panel;
import utils.file.FileUtils;
import utils.graphing.Graphable;
import utils.graphing.GraphableObject;
import utils.graphing.Point;
import utils.preferences.MessageBox;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.androidplot.Plot.BorderStyle;
import com.androidplot.series.XYSeries;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

public class GraphingActivity extends Activity {
	public final static String GRAPHING_ACTIVITY = "gui.activities.PREFERENCE_MESSAGE";
	public final static String NAME_ACTIVITY_GRAPHING = "graphing_activity";
	private static int id = 1;

	// private VideoActivity videoActivity;

	public double parseValue(String text) {
		try {
			double d = Double.parseDouble(text);
			return d;
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox box = new MessageBox(
					"Invalid Scale Distance. Using value of 100cm.");
			box.show(getFragmentManager(), "err_msgbx_parse");
			return 100.0D;
		}
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			double value = parseValue(StaticVariables.mainProject.scaleTextValue);

			if (StaticVariables.mainProject.getDataChanged()) {
				StaticVariables.mainProject.setDataUnchanged();
				StaticVariables.mainProject.updateGraphable(this, value);
			}

			// If the graph data has changed at all, update the
			// graphableobjectcontainer that is in use
			StaticVariables.mainProject.updateGraphable(this, value);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// setContentView(new Panel(this));
		setContentView(R.layout.activity_graphing);

		TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
		tabHost.setup();

		try {
			XYPlot positionTimePlot = (XYPlot) findViewById(R.id.position_time_plot);
			positionTimePlot.setBorderStyle(BorderStyle.NONE, 0.0F, 0.0F);
			Point[] points = StaticVariables.mainProject.getGraphable().getPositionTimeGraph().getPoints();
			Number[] series1Numbers = new Number[points.length * 2];///{ 1, 8, 5, 2, 7, 4 };
			
			for(int i = 0 ; i < points.length * 2; i+=2)
			{
				series1Numbers[i] = points[i/2].x;
				series1Numbers[i+1] = points[i/2].y;
			}
			
			XYSeries series1 = new SimpleXYSeries(
					Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
					SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, // Y_VALS_ONLY means use the element index as the x value
					"");                             // Set the display title of the series
						
			LineAndPointFormatter series1Format = new LineAndPointFormatter(
					Color.rgb(0, 200, 0),                   // line color
					Color.rgb(0, 100, 0),                   // point color
					null);              // fill color (optional)
			
			positionTimePlot.addSeries(series1, series1Format);
		    
		    // By default, AndroidPlot displays developer guides to aid in laying out your plot.
		    // To get rid of them call disableAllMarkup():
			positionTimePlot.disableAllMarkup();
		    positionTimePlot.getBackgroundPaint().setAlpha(0);
		    positionTimePlot.getGraphWidget().getBackgroundPaint().setAlpha(0);
		    positionTimePlot.getGraphWidget().getGridBackgroundPaint().setAlpha(0);   

		    positionTimePlot.setDomainLabel("Time");
			positionTimePlot.setRangeLabel("Position");

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			XYPlot velocityTimePlot = (XYPlot) findViewById(R.id.velocity_time_plot);
			velocityTimePlot.setBorderStyle(BorderStyle.NONE, 0.0F, 0.0F);
			Point[] points = StaticVariables.mainProject.getGraphable().getVelocityTimeGraph().getPoints();
			Number[] series1Numbers = new Number[points.length * 2];///{ 1, 8, 5, 2, 7, 4 };
			
			for(int i = 0 ; i < points.length * 2; i+=2)
			{
				series1Numbers[i] = points[i/2].x;
				series1Numbers[i+1] = points[i/2].y;
			}
			
			XYSeries series1 = new SimpleXYSeries(
					Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
					SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, // Y_VALS_ONLY means use the element index as the x value
					"");                             // Set the display title of the series
						
			LineAndPointFormatter series1Format = new LineAndPointFormatter(
					Color.rgb(0, 200, 0),                   // line color
					Color.rgb(0, 100, 0),                   // point color
					null);              // fill color (optional)
			
			velocityTimePlot.addSeries(series1, series1Format);
		    
		    // By default, AndroidPlot displays developer guides to aid in laying out your plot.
		    // To get rid of them call disableAllMarkup():
			velocityTimePlot.disableAllMarkup();
			velocityTimePlot.getBackgroundPaint().setAlpha(0);
			velocityTimePlot.getGraphWidget().getBackgroundPaint().setAlpha(0);
			velocityTimePlot.getGraphWidget().getGridBackgroundPaint().setAlpha(0);   

			velocityTimePlot.setDomainLabel("Time");
			velocityTimePlot.setRangeLabel("Velocity");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			XYPlot xTimePlot = (XYPlot) findViewById(R.id.x_time_plot);
			xTimePlot.setBorderStyle(BorderStyle.NONE, 0.0F, 0.0F);
			Point[] points = StaticVariables.mainProject.getGraphable().getXTimeGraph().getPoints();
			Number[] series1Numbers = new Number[points.length * 2];///{ 1, 8, 5, 2, 7, 4 };
			
			for(int i = 0 ; i < points.length * 2; i+=2)
			{
				series1Numbers[i] = points[i/2].x;
				series1Numbers[i+1] = points[i/2].y;
			}
			
			XYSeries series1 = new SimpleXYSeries(
					Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
					SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, // Y_VALS_ONLY means use the element index as the x value
					"");                             // Set the display title of the series
						
			LineAndPointFormatter series1Format = new LineAndPointFormatter(
					Color.rgb(0, 200, 0),                   // line color
					Color.rgb(0, 100, 0),                   // point color
					null);              // fill color (optional)
			
			xTimePlot.addSeries(series1, series1Format);
		    
		    // By default, AndroidPlot displays developer guides to aid in laying out your plot.
		    // To get rid of them call disableAllMarkup():
			xTimePlot.disableAllMarkup();
		    xTimePlot.getBackgroundPaint().setAlpha(0);
		    xTimePlot.getGraphWidget().getBackgroundPaint().setAlpha(0);
		    xTimePlot.getGraphWidget().getGridBackgroundPaint().setAlpha(0);   

		    xTimePlot.setDomainLabel("Time");
			xTimePlot.setRangeLabel("X");

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			XYPlot yTimePlot = (XYPlot) findViewById(R.id.y_time_plot);
			yTimePlot.setBorderStyle(BorderStyle.NONE, 0.0F, 0.0F);
			Point[] points = StaticVariables.mainProject.getGraphable().getYTimeGraph().getPoints();
			Number[] series1Numbers = new Number[points.length * 2];///{ 1, 8, 5, 2, 7, 4 };
			
			for(int i = 0 ; i < points.length * 2; i+=2)
			{
				series1Numbers[i] = points[i/2].x;
				series1Numbers[i+1] = points[i/2].y;
			}
			
			XYSeries series1 = new SimpleXYSeries(
					Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
					SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, // Y_VALS_ONLY means use the element index as the x value
					"");                             // Set the display title of the series
						
			LineAndPointFormatter series1Format = new LineAndPointFormatter(
					Color.rgb(0, 200, 0),                   // line color
					Color.rgb(0, 100, 0),                   // point color
					null);              // fill color (optional)
			
			yTimePlot.addSeries(series1, series1Format);
		    
		    // By default, AndroidPlot displays developer guides to aid in laying out your plot.
		    // To get rid of them call disableAllMarkup():
			yTimePlot.disableAllMarkup();
		    yTimePlot.getBackgroundPaint().setAlpha(0);
		    yTimePlot.getGraphWidget().getBackgroundPaint().setAlpha(0);
		    yTimePlot.getGraphWidget().getGridBackgroundPaint().setAlpha(0);   

		    yTimePlot.setDomainLabel("Time");
			yTimePlot.setRangeLabel("Y");

		} catch (Exception e) {
			e.printStackTrace();
		}

		TabSpec xTab = tabHost.newTabSpec("X/T");
		xTab.setContent(R.id.tab_x);
		xTab.setIndicator("X/T");

		TabSpec yTab = tabHost.newTabSpec("Y/T");
		yTab.setContent(R.id.tab_y);
		yTab.setIndicator("Y/T");

		TabSpec spec1 = tabHost.newTabSpec("P/T");
		spec1.setContent(R.id.tab1);
		spec1.setIndicator("P/T");

		TabSpec spec2 = tabHost.newTabSpec("V/T");
		spec2.setIndicator("V/T");
		spec2.setContent(R.id.tab2);

		TabSpec dataTab = tabHost.newTabSpec("Data");
		dataTab.setContent(R.id.data_tab);
		dataTab.setIndicator("Data");
		updateDataTab();
		
		tabHost.addTab(xTab);
		tabHost.addTab(yTab);
		tabHost.addTab(spec1);
		tabHost.addTab(spec2);
		tabHost.addTab(dataTab);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_graphing, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_bar_video:
			swapToVideoActivity();
			return true;
		case R.id.action_bar_save:
			saveProject();
			return true;
		case R.id.action_bar_graph:
			generateGraphs();
			return true;
		case R.id.data_tab:
			updateDataTab();
			return true;
		case R.id.action_bar_preferences:
			openPreferences();
			return true;
		case R.id.action_bar_export_text:
			exportToTxt();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void exportToTxt() {
		System.out.println(new FileUtils().exportDataAsTxt(this,
				StaticVariables.mainProject));
	}

	public void saveProject() {
		try {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			boolean saveInternally = prefs.getBoolean(
					"preference_default_save_option", false);
			new FileUtils().save(this, saveInternally,
					StaticVariables.mainProject);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Returns a valid id that isn't in use
	public int findId() {
		View v = findViewById(id);
		while (v != null) {
			v = findViewById(++id);
		}
		return id++;
	}

	public void updateDataTab() {
		// Needs .isChanged boolean for the data object thing to tell if this is
		// needed. Will offer a speed up and reduction in
		// stupid wastefulness

		/**
		 * <TextView android:id="@+id/runLabel" android:text="Time"
		 * android:layout_height="wrap_content"
		 * android:background="@drawable/cell_style" android:gravity="center" />
		 */
		// reference the table layout
		TableLayout tbl = (TableLayout) findViewById(R.id.table_layout_data);
		tbl.removeAllViews();

		try {
			GraphableObject positionTime = StaticVariables.mainProject
					.getGraphable().getPositionTimeGraph();
			Point[] points = positionTime.getPoints();

			// Add in a time/distance "label" row as well
			// declare a new row
			TableRow labelRow = new TableRow(this);

			TextView timeTextViewTime = new TextView(this);
			timeTextViewTime.setId(findId());
			timeTextViewTime.setText("Time");
			timeTextViewTime.setBackgroundResource(R.drawable.cell_style);
			timeTextViewTime.setGravity(Gravity.CENTER);

			TextView positionTextViewDist = new TextView(this);
			positionTextViewDist.setId(findId());
			positionTextViewDist.setText("Position");
			positionTextViewDist.setBackgroundResource(R.drawable.cell_style);
			positionTextViewDist.setGravity(Gravity.CENTER);

			// add views to the row
			labelRow.addView(timeTextViewTime); // you would actually want to
												// set properties on this before
												// adding it
			labelRow.addView(positionTextViewDist);

			// add the row to the table layout
			tbl.addView(labelRow);

			for (int i = 0; i < positionTime.getPoints().length; i++) {
				// declare a new row
				TableRow newRow = new TableRow(this);

				TextView timeTextView = new TextView(this);
				timeTextView.setId(findId());
				timeTextView.setText("" + points[i].getX());
				timeTextView.setBackgroundResource(R.drawable.cell_style);
				timeTextView.setGravity(Gravity.CENTER);

				TextView positionTextView = new TextView(this);
				positionTextView.setId(findId());
				positionTextView.setText("" + points[i].getY());
				positionTextView.setBackgroundResource(R.drawable.cell_style);
				positionTextView.setGravity(Gravity.CENTER);

				// add views to the row
				newRow.addView(timeTextView); // you would actually want to set
												// properties on this before
												// adding it
				newRow.addView(positionTextView);

				// add the row to the table layout
				tbl.addView(newRow);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getWidth()
	{
    	Display display = getWindowManager().getDefaultDisplay();
    	return display.getWidth();    	
	}
	
	public int getHeight()
	{
		Display display = getWindowManager().getDefaultDisplay();
		return display.getHeight();
	}
    
	public void generateGraphs() {

	}

	public void openPreferences() {
		Intent intent = new Intent(this, PreferencesActivity.class);
		intent.putExtra(GRAPHING_ACTIVITY, NAME_ACTIVITY_GRAPHING);
		startActivity(intent);
	}

	public void swapToVideoActivity() {
		Intent intent = new Intent(this, VideoActivity.class);
		intent.putExtra(GRAPHING_ACTIVITY, NAME_ACTIVITY_GRAPHING);
		startActivity(intent);
	}
}
