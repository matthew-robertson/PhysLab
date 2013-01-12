package gui.activities;

import java.io.File;

import project.OptionsObject;
import project.Project;
import utils.action.Action;
import utils.action.ActionAdd;
import utils.action.EnumAction;
import utils.action.StackAction;
import utils.graphing.Boundary;
import utils.graphing.ClickBoundary;
import utils.graphing.GraphableObjectContainer;
import utils.graphing.MathUtils;
import utils.graphing.Point;
import utils.preferences.ChooseProjectDialog;
import utils.preferences.MessageBox;
import utils.preferences.VideoConfirmDialogFragment;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

public class VideoActivity extends Activity 
		implements VideoConfirmDialogFragment.VideoConfirmDialogListener, ChooseProjectDialog.ChooseProjectDialogListener, 
		GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener
{
	public final static String MAIN_ACTIVITY = "gui.activities.PREFERENCE_MESSAGE";
	public final static String NAME_ACTIVITY_MAIN = "main_activity";
	
	private boolean plottingPoints;
	private Menu menu;
	private GestureDetector mDetector; 
	private boolean isSettingOrigin;
	private int TOP_PADDING = 75;	
	private final int AXIS_SIZE = 7; // half the actual size (reduces math)
	private boolean isSettingScaleLeft;
	private boolean isSettingScaleRight;
	private final int TICKS_PER_SECOND = 20;
	private final int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
	private final int MAX_FRAMESKIP = 200000000;//5;
	private long next_game_tick = System.currentTimeMillis();
	private int loops; 
	private boolean isScaleSet = false;
	//Move this to the project
	private boolean topPaddingUpdated = false;
	private final String POSITIVE_MESSAGE = "Set Origin";
	private final String NEGATIVE_MESSAGE = "Cancel Set Origin";
	private final String STOP_PLOTTING_POINTS = "Stop Plotting Points";
    private final String START_PLOTTING_POINTS = "Start Plotting Points";
        
	private boolean isScaleMeasureDisplayed;
	public static MediaController mediaController;
	public static VideoView videoView;
	public static File clip;
	public static long vidLength;
	public static NumberPicker  stepAmount;
	public static SeekBar seeker;
	
	private ClickBoundary textBounds;
	private RelativeLayout layout;
	
	public final static String KEY_INTENT_WRN_MSG_CHANGE_VIDEO = "message.video.change.wrn";
	
	//TODO: open files is possibly fixed. Needs better testing.  
	//TODO: Legit video testing
	
	//TODO: [low priority] delete projects.
	//TODO: minor bug, the editTextPreference is not updating when opening the preferences activity.
	//TODO: minor bug, text-zone bounds don't reset. "oops"
	//TODO: minor bug with the right side of the scale, apparently.
	//TODO: slight tweek in VT/AT math for the first point (which is clearly wrong)
	//TODO: bitmap being retarded when swapping back to VideoActivity (the scale bitmap)
	//TODO: minor bug, preferences dont update the measure-axis value
	
	/**
	 * Probable issues: Project Swapping.
	 * 		Known Issue: Preferences dont update appropriately. They do get updated when a new project is created.
	 * 		[Test] See if the saving issue (where the project was always the default) has been fixed with the oncreate() change.
	 */
	
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);        
        
        
        plottingPoints = false;
        
        if(StaticVariables.scaleImage == null)
        {
        	StaticVariables.scaleImage = BitmapFactory.decodeResource(getResources(), R.drawable.scale);
        }
        
        // Instantiate the gesture detector with the
        // application context and an implementation of
        // GestureDetector.OnGestureListener
        mDetector = new GestureDetector(this,this);
        // Set the gesture detector as the double tap
        // listener.
        mDetector.setOnDoubleTapListener(this);
        
        
        topPaddingUpdated = false;
        
        //PreferenceManager.setDefaultValues(this, R.xml.preferences_menu, false);
        videoView = (VideoView) findViewById(R.id.track);  
        Intent i = getIntent();        
        
        try
        {
        	String extraString = i.getStringExtra(VideoSelect.VIDEO_SELECT_KEY);
        	
        	if(!StaticVariables.mainProject.videoPath.equals("") && !extraString.equals(StaticVariables.mainProject.videoPath))
        	{
        		clearData();
        	}

			StaticVariables.mainProject.videoPath = extraString;
        	
	        //Check to make sure we can actually read the external memory
	        if (canReadStorage()){
	        	clip= new File(Environment.getExternalStorageDirectory(),
	        			extraString);
	        	if (clip.exists()){
	        		initializeVideo();        		
	        	}        	
	        }
	        else {
	        	System.out.println("We don't appear to have read rights on your device's storage device (most likely an SD card).");
	        }
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        
        topPaddingUpdated = false;
        
        if(StaticVariables.mainProject != null)
        {
        	StaticVariables.mainProject.setDimensions(getWidth(), getHeight());
        }
     
        layout = (RelativeLayout)(findViewById(R.id.activity_video_relative_layout));
      


	        ViewTreeObserver vto = layout.getViewTreeObserver();
	        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
	            @Override
	            public void onGlobalLayout() 
	            {
	                redraw();
	                
	                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this); //.removeGlobalOnLayoutListener(this); 
	            }
	        });
        
    }
    
    public void clearData()
    {
    	StaticVariables.mainProject.origin = new Point(0, 0);
    	StaticVariables.mainProject.getOptions().clearAllPoints(layout);
    	StaticVariables.mainProject.options = new OptionsObject();
		StaticVariables.mainProject.actionStack = new StackAction(Project.ACTION_QUEUE_SIZE);
    	StaticVariables.mainProject.graphable = new GraphableObjectContainer();
    	StaticVariables.mainProject.videoBounds = null;
    	StaticVariables.mainProject.scaleBoundary = null;
    	resetOriginScalePointSetters();
    	topPaddingUpdated = false;
    }

    public void initializeSeekAndStep(){
    	stepAmount = (NumberPicker) findViewById(R.id.stepcontrol);
    	stepAmount.setMinValue(1);
        stepAmount.setValue(1);
        stepAmount.setMaxValue(5);
        
        seeker = (SeekBar) findViewById(R.id.seekControl);
        System.out.println(vidLength);
        seeker.setMax((int) vidLength);
        //Allow the seekBar to actually control the video's position
        seeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {            
            @Override
            public void onProgressChanged(SeekBar seeker, int progress,
                    boolean fromUser) {
            	if (fromUser){
            		videoView.seekTo(progress);
            	}
            }

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
			}
         });        	
    }
    
    public void stepVideo(View v){
    	if(videoView == null || stepAmount == null)
    	{
    		return;
    	}
    	
    	//If it's possible to step to the correct time, do so
    	if (videoView.getCurrentPosition() + (stepAmount.getValue() * 33)  < vidLength){
    		videoView.seekTo(videoView.getCurrentPosition() + (stepAmount.getValue() * 33));
    		seeker.setProgress(videoView.getCurrentPosition());
    	}
    	//Otherwise step to the end
    	else{
    		videoView.seekTo((int) vidLength);
    		seeker.setProgress(videoView.getCurrentPosition());
    	}
    	System.out.println(videoView.getCurrentPosition());
    }
    
    public void initializeVideo(){
    	videoView.setVideoPath(clip.getAbsolutePath());	
    	//Get the length of the video, in milliseconds, once loaded, then continue setup
    	videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			public void onPrepared(MediaPlayer mp){    				
				vidLength = videoView.getDuration();
				initializeSeekAndStep();
				videoView.start();
		    	videoView.pause();  
			};
		});
    }
        
    //Method to step the video and send a data point
    public void videoClick(View v){
    	stepVideo(v);  
    	/**
    	 * Add a method to send data here!
    	 */
    }
    
    public void playButton(View v){
    	if (videoView.isPlaying()){
    		videoView.pause();
    	}
    	else{
    		videoView.start();
    	}
    }
    
    public boolean canReadStorage(){
    	String state = Environment.getExternalStorageState();
    	if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
    		//We can read
    		return true;
    	}
    	return false;
    }
    
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        getMenuInflater().inflate(R.menu.activity_video, menu);
        this.menu = menu;
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        // Handle item selection
        switch (item.getItemId()) 
        {
            case R.id.preferences_item:
            	openPreferences();
                return true;
            case R.id.action_bar_graph:
            	openGraphs();
            	return true;
            /*
            case R.id.action_bar_undo:
            	reverseLastAction();
            	return true;
           	*/
            case R.id.action_bar_remove_point:
            	removePoint();
            	return true;
            case R.id.action_bar_set_origin:
            	setOrigin();            	
            	return true;
            case R.id.action_bar_plot_points:
            	plotPoints();
            	return true;
            case R.id.action_bar_set_scale:
            	setScale();            	
            	return true;
            case R.id.action_bar_save_video:
            	saveProject();
            	return true;
            case R.id.action_bar_open_project_video:
            	beginOpeningProject();
            	return true;
            case R.id.action_bar_new_project_video:
            	newProject();
            	return true;
            case R.id.action_bar_change_video:
            	returnToVideo();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void removePoint()
    {
    	StaticVariables.mainProject.options.removePointByTime(layout, getTime());
    }
    
    public void redraw()
    {
    	
    	
    	textBounds = new ClickBoundary(new Point[] { 
				new Point(0, 0),
				new Point(getWidth() * 0.4, 0), 
				new Point(getWidth() * 0.4, getHeight() / 10),
				new Point(0, getHeight() / 10)					
			});
    	
		TOP_PADDING = getHeight() - layout.getMeasuredHeight();
			
		double left = videoView.getLeft();
		double right = videoView.getRight();
		double top = videoView.getTop();
		double bottom = videoView.getBottom();
		
		StaticVariables.mainProject.videoBounds = new Boundary(new Point[] { new Point(left, top), new Point(right, top), new Point(right, bottom), new Point(left, bottom) });
		
    	Point[] points = StaticVariables.mainProject.getOptions().getPointsAsArray();
    	
    	for(int i = 0; i < points.length; i++)
    	{
    		double x = points[i].x;
    		double y = points[i].y - TOP_PADDING;
    		
    		final int SIZE = 4;
			ImageView image = points[i].imageView;
			//image.setImageResource(R.drawable.point);
			//mage.setAdjustViewBounds(true);
			//StaticVariables.mainProject.getOptions().registerPoint(new Point(x, y + TOP_PADDING, getTime(),image));
			
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(2 * SIZE, 2 * SIZE);
			params.leftMargin = (int) x - SIZE;
			params.topMargin = (int) y - SIZE;			
			//layout.removeView(image);
			
			layout.addView(image, params);	
			//image.setLayoutParams(params);
			
    		//points[i].imageView.setLayoutParams(points[i].imageView.getLayoutParams());
//    		layout.addView();
    	}
    	
    	if(StaticVariables.mainProject.origin != null)
    	{
    		double x = StaticVariables.mainProject.origin.x;
    		double y = StaticVariables.mainProject.origin.y;
    		StaticVariables.mainProject.origin = new Point(x, y);
    		
    		ImageView imageHA = (ImageView) findViewById(R.id.image_view_vertical_axis);
    		ImageView imageVA = (ImageView) findViewById(R.id.image_view_horizontal_axis);
    		ImageView imageO = (ImageView) findViewById(R.id.image_view_origin);
        	
    		imageHA.setVisibility(View.VISIBLE);
    		imageVA.setVisibility(View.VISIBLE);
    		imageO.setVisibility(View.VISIBLE);
    		
    		
    		System.out.println(getWidth() + "   ," + getHeight());
    		
    		double videoHeight = StaticVariables.mainProject.videoBounds.points[3].y - StaticVariables.mainProject.videoBounds.points[0].y;
    		
    		double height1 = (int)((y + AXIS_SIZE - TOP_PADDING) - (y - AXIS_SIZE - TOP_PADDING));
    		if(height1 > videoHeight)
    		{
    			height1 = videoHeight - (y - AXIS_SIZE - TOP_PADDING);
    		}
    		
    		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(getWidth(), (int) height1); 
    		//params.setMargins(0, (int)y - AXIS_SIZE - TOP_PADDING, (int)getWidth(), (int)y + AXIS_SIZE - TOP_PADDING);
    		params.leftMargin = 0;
    		params.topMargin = (int)y - AXIS_SIZE - TOP_PADDING;
    		
    		double height2 = getHeight();
    		if(height2 > videoHeight)
    		{
    			height2 = videoHeight;
    		}
    		
    		RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams((int) ((x + AXIS_SIZE) - (x - AXIS_SIZE)), (int) height2); 
//    		params1.setMargins((int)x - AXIS_SIZE, 0, (int)x + AXIS_SIZE, getHeight());
    		params1.leftMargin = (int)x - AXIS_SIZE;
    		params1.topMargin = 0;
    		
    		
    		double height3 = (int)((y + AXIS_SIZE - TOP_PADDING) - (y - AXIS_SIZE - TOP_PADDING));
    		if(height3 > videoHeight)
    		{
    			height3 = videoHeight - (y - AXIS_SIZE - TOP_PADDING);
    		}
    		
    		RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams((int)((x + AXIS_SIZE) - (x - AXIS_SIZE)), (int) height3); 
//    		params2.setMargins((int)x - AXIS_SIZE, (int)y - AXIS_SIZE - TOP_PADDING, (int)x + AXIS_SIZE, (int)y + AXIS_SIZE - TOP_PADDING);
    		params2.leftMargin = (int) (x - AXIS_SIZE);
    		params2.topMargin = (int) (y - AXIS_SIZE - TOP_PADDING);
    		
    		imageHA.setLayoutParams(params);
    		imageVA.setLayoutParams(params1);
    		imageO.setLayoutParams(params2);
    		
        	isSettingOrigin = false;
    		
    	}
    	
    	if(StaticVariables.mainProject.scaleBoundary != null)
    	{
    		setScale();
    		
    		
    		
    		ImageView image = (ImageView) findViewById(R.id.image_view_scale);
			Point rightPoint = StaticVariables.mainProject.scaleBoundary.centerOfRightLine();
			Point leftPoint = StaticVariables.mainProject.scaleBoundary.centerOfLeftLine();
			Point centerPoint = StaticVariables.mainProject.scaleBoundary.getPerfectCenter();
		    
			double lineLength = lineLength(rightPoint.getX(), rightPoint.getY(), leftPoint.x, leftPoint.y);
			
			System.out.println(">>" + lineLength);
			
			double angle = 0;//angleBetweenTwoVectors(rightPoint.x, rightPoint.y, x, y, leftPoint.getX(), leftPoint.getY());
			StaticVariables.mainProject.scaleBoundary.totalRotLeft += angle;				
			
			StaticVariables.mainProject.scaleBoundary.updateLengthLeft(lineLength);
			StaticVariables.mainProject.scaleBoundary.rotateOnRightCenter(angle);
			
		    left = StaticVariables.mainProject.scaleBoundary.points[0].x;
		    right = StaticVariables.mainProject.scaleBoundary.points[0].x;
		    top = StaticVariables.mainProject.scaleBoundary.points[0].y;
		    bottom = StaticVariables.mainProject.scaleBoundary.points[0].y;
		    
		    for(int i = 0; i < StaticVariables.mainProject.scaleBoundary.points.length; i++)
		    {
		    	Point p = StaticVariables.mainProject.scaleBoundary.points[i];
		    	
		    	if(p.x < left)
		    		left = p.x;
		    	if(p.x > right)
		    		right = p.x;
		    	if(p.y < top)
		    		top = p.y;
		    	if(p.y > bottom)
		    		bottom = p.y;
		    }
		    
		    Matrix matrix = new Matrix();
			image.setScaleType(ScaleType.MATRIX);
			matrix.setRotate((float)(StaticVariables.mainProject.scaleBoundary.totalRotRight + StaticVariables.mainProject.scaleBoundary.totalRotLeft), (int)centerPoint.getX(), (int)centerPoint.getY());//, (int)rightPoint.getX(), (int)rightPoint.getY()

			Bitmap resizedBitmap = getResizedBitmap(StaticVariables.scaleImage, (int)lineLength, StaticVariables.scaleImage.getHeight());
		    Bitmap img2 = Bitmap.createBitmap(resizedBitmap, 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight(), matrix, true);
		    image.setImageBitmap(img2);			 
		    
		    
		    double hypotenuse = Math.sqrt(Math.pow(bottom - top, 2) + Math.pow(right-left, 2));
		    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)hypotenuse, (int)hypotenuse);
		    params.leftMargin = (int) (left);
		    params.topMargin = (int) top;			    
		    //move the image to where it should be... not (0, 0)
		    layout.removeView(image);
		    layout.addView(image, params);	
		    
		    RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT); 
		    params1.setMargins(0, 0, (int)(getWidth() * 0.4), getHeight() / 10);
		    
        	EditText scaleText = (EditText)(findViewById(R.id.scale_text_zone));
        	scaleText.setLayoutParams(params1);
        	
    		/*
	    	ImageView image = (ImageView) findViewById(R.id.image_view_scale);
	    	Point rightPoint = StaticVariables.mainProject.scaleBoundary.centerOfRightLine();
			Point leftPoint = StaticVariables.mainProject.scaleBoundary.centerOfLeftLine();
			Point centerPoint = StaticVariables.mainProject.scaleBoundary.getPerfectCenter();
		    double lineLength = lineLength(leftPoint.getX(), leftPoint.getY(), rightPoint.getX(), rightPoint.getY());
		    image.setVisibility(View.VISIBLE);
			
	    	left = StaticVariables.mainProject.scaleBoundary.points[0].x;
		    right = StaticVariables.mainProject.scaleBoundary.points[0].x;
		    top = StaticVariables.mainProject.scaleBoundary.points[0].y;
		    bottom = StaticVariables.mainProject.scaleBoundary.points[0].y;
		    
		    for(int i = 0; i < StaticVariables.mainProject.scaleBoundary.points.length; i++)
		    {
		    	Point p = StaticVariables.mainProject.scaleBoundary.points[i];
		    	
		    	if(p.x < left)
		    		left = p.x;
		    	if(p.x > right)
		    		right = p.x;
		    	if(p.y < top)
		    		top = p.y;
		    	if(p.y > bottom)
		    		bottom = p.y;			    	
		    }
		    
		    Matrix matrix = new Matrix();
			image.setScaleType(ScaleType.MATRIX);
			matrix.setRotate((float)(StaticVariables.mainProject.scaleBoundary.totalRotRight + StaticVariables.mainProject.scaleBoundary.totalRotLeft), (int)centerPoint.getX(), (int)centerPoint.getY());//, (int)rightPoint.getX(), (int)rightPoint.getY()
	
			Bitmap resizedBitmap = getResizedBitmap(StaticVariables.scaleImage, (int)lineLength, StaticVariables.scaleImage.getHeight());
		    Bitmap img2 = Bitmap.createBitmap(resizedBitmap, 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight(), matrix, true);
		    image.setImageBitmap(img2);
		    
		    double hypotenuse = Math.sqrt(Math.pow(bottom - top, 2) + Math.pow(right-left, 2));
		    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)hypotenuse, (int)hypotenuse);
		    params.leftMargin = (int) (left);
		    params.topMargin = (int) top;			    
		    //move the image to where it should be... not (0, 0)
		    layout.removeView(image);
		    layout.addView(image, params);			
		
		   
		    RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT); 
		    params1.setMargins(0, 0, (int)(getWidth() * 0.4), getHeight() / 10);
		    
	    	EditText scaleText = (EditText)(findViewById(R.id.scale_text_zone));
	    	scaleText.setLayoutParams(params1);
	    	*/
    	}
    
    }
    
    public void changeActivity()
    {
    	Point[] points = StaticVariables.mainProject.getOptions().getPointsAsArray();
    	
    	for(int i = 0; i < points.length; i++)
    	{
    		layout.removeView(points[i].imageView);
    	}
    }
    
    public void returnToVideo()
    {
    	changeActivity();
    	Intent intent = new Intent(this, VideoSelect.class);
    	intent.putExtra(KEY_INTENT_WRN_MSG_CHANGE_VIDEO, KEY_INTENT_WRN_MSG_CHANGE_VIDEO);
    	startActivity(intent);
    }
    
    public String trimFilepath(String path) {
		return path.substring(path.lastIndexOf("/") + 1, path.indexOf(".physdat"));
	}

	private String[] queryForFilesInternally() {
		File[] files = getFilesDir().listFiles();
		String[] strings = new String[files.length];
		int totalValues = 0;
		for (int i = 0; i < strings.length; i++) 
		{
			if(files[i].toString().endsWith(".physdat"))
			{
				strings[totalValues] = trimFilepath(files[i].toString());
				totalValues++;						
			}
		}
		
		String[] betterValues = new String[totalValues];
		for(int i = 0; i < totalValues; i++)
		{
			betterValues[i] = strings[i];
		}
		
		return betterValues;
	}

	private String[] queryForFilesExternally() {
		if (canReadStorage()) {
			File[] files = getExternalFilesDir(null).listFiles();
			String[] strings = new String[files.length];
			int totalValues = 0;
			for (int i = 0; i < strings.length; i++) 
			{
				if(files[i].toString().endsWith(".physdat"))
				{
					strings[totalValues] = trimFilepath(files[i].toString());
					totalValues++;						
				}
			}
			
			String[] betterValues = new String[totalValues];
			for(int i = 0; i < totalValues; i++)
			{
				betterValues[i] = strings[i];
			}
			return betterValues;
		}

		return new String[] {};
	}

    
    public void beginOpeningProject()
    {
    	String[] mergedFilePaths;
    	String[] internal = queryForFilesInternally();
    	String[] external = queryForFilesExternally();
    	mergedFilePaths = new String[internal.length + external.length];
    	
    	for(int i = 0; i < internal.length; i++)
    	{
    		mergedFilePaths[i] = internal[i];
    	}
    	for(int i = 0; i < external.length; i++)
    	{
    		mergedFilePaths[internal.length + i] = external[i];
    	}
    	
    	DialogFragment newFragment = new ChooseProjectDialog(mergedFilePaths, internal.length - 1);
	    newFragment.show(getFragmentManager(), "open_project");    	
    }
    
    public void newProject()
    {
    	DialogFragment newFragment = new VideoConfirmDialogFragment();
	    newFragment.show(getFragmentManager(), "new_project");    		    
	    TOP_PADDING = getHeight() - layout.getMeasuredHeight();
    }
    
    public void onDialogPositiveClick(DialogFragment dialog, String message) 
	{
    	//Confirm new 
    	StaticVariables.mainProject = new Project(this);
    	StaticVariables.mainProject.setDimensions(getWidth(), getHeight());
	
    	Intent intent = new Intent(this, VideoSelect.class);
        startActivity(intent);
	}

	public void onDialogNegativeClick(DialogFragment dialog, String message) 
	{
	}
    
	public void openProject(DialogFragment dialog, String filePath, boolean storedInternally)
	{
		Project proj = new FileUtils().load(this, filePath, storedInternally);
		
		//Add a confirm dialog somewhere earlier? maybe for saving or something.
		if(proj != null)
		{
			StaticVariables.mainProject = proj;
		}
	
	
	}
	
	public void displayOrigin(float x, float y)
	{
		StaticVariables.mainProject.origin = new Point(x, y);
		
		ImageView imageHA = (ImageView) findViewById(R.id.image_view_vertical_axis);
		ImageView imageVA = (ImageView) findViewById(R.id.image_view_horizontal_axis);
		ImageView imageO = (ImageView) findViewById(R.id.image_view_origin);
    	
		imageHA.setVisibility(View.VISIBLE);
		imageVA.setVisibility(View.VISIBLE);
		imageO.setVisibility(View.VISIBLE);
		
		
		System.out.println(getWidth() + "   ," + getHeight());
		
		double videoHeight = StaticVariables.mainProject.videoBounds.points[3].y - StaticVariables.mainProject.videoBounds.points[0].y;
		
		double height1 = (int)((y + AXIS_SIZE - TOP_PADDING) - (y - AXIS_SIZE - TOP_PADDING));
		if(height1 > videoHeight)
		{
			height1 = videoHeight - (y - AXIS_SIZE - TOP_PADDING);
		}
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(getWidth(), (int) height1); 
		//params.setMargins(0, (int)y - AXIS_SIZE - TOP_PADDING, (int)getWidth(), (int)y + AXIS_SIZE - TOP_PADDING);
		params.leftMargin = 0;
		params.topMargin = (int)y - AXIS_SIZE - TOP_PADDING;
		
		double height2 = getHeight();
		if(height2 > videoHeight)
		{
			height2 = videoHeight;
		}
		
		RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams((int) ((x + AXIS_SIZE) - (x - AXIS_SIZE)), (int) height2); 
//		params1.setMargins((int)x - AXIS_SIZE, 0, (int)x + AXIS_SIZE, getHeight());
		params1.leftMargin = (int)x - AXIS_SIZE;
		params1.topMargin = 0;
		
		
		double height3 = (int)((y + AXIS_SIZE - TOP_PADDING) - (y - AXIS_SIZE - TOP_PADDING));
		if(height3 > videoHeight)
		{
			height3 = videoHeight - (y - AXIS_SIZE - TOP_PADDING);
		}
		
		RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams((int)((x + AXIS_SIZE) - (x - AXIS_SIZE)), (int) height3); 
//		params2.setMargins((int)x - AXIS_SIZE, (int)y - AXIS_SIZE - TOP_PADDING, (int)x + AXIS_SIZE, (int)y + AXIS_SIZE - TOP_PADDING);
		params2.leftMargin = (int) (x - AXIS_SIZE);
		params2.topMargin = (int) (y - AXIS_SIZE - TOP_PADDING);
		
		imageHA.setLayoutParams(params);
		imageVA.setLayoutParams(params1);
		imageO.setLayoutParams(params2);
		
		(menu.findItem(R.id.action_bar_set_origin)).setTitle(POSITIVE_MESSAGE);
    	isSettingOrigin = false;
	}
	
    public void setOrigin()
    {
    	if(!isSettingOrigin)
    	{
    		resetOriginScalePointSetters();
    	}
    	
    	isSettingOrigin = !isSettingOrigin;    	
    	(menu.findItem(R.id.action_bar_set_origin)).setTitle((!isSettingOrigin) ? POSITIVE_MESSAGE : NEGATIVE_MESSAGE);
    }
    
    /**
     * Mouse click inside where the origin in.
     * @return
     */
    public boolean isInOriginBounds(float x, float y)
    {
    	if(StaticVariables.mainProject.origin != null)
    	{
    		if(x > (StaticVariables.mainProject.origin.getX() - AXIS_SIZE) &&
    			x < (StaticVariables.mainProject.origin.getX() + AXIS_SIZE) &&
    			y > (StaticVariables.mainProject.origin.getY() - AXIS_SIZE) &&
    			y < (StaticVariables.mainProject.origin.getY() + AXIS_SIZE))
    		{
    			return true;
    		}
    	}  	
    	
    	return false;
    }
    
    public void plotPoints()
    {    	
    	if(!plottingPoints)
    	{
    		resetOriginScalePointSetters();
    	    MenuItem item = menu.findItem(R.id.action_bar_plot_points);
    		item.setTitle(STOP_PLOTTING_POINTS);
    	}
    	else
    	{
    		MenuItem item = menu.findItem(R.id.action_bar_plot_points);
    		item.setTitle(START_PLOTTING_POINTS);
    	}   
    	plottingPoints = !plottingPoints;
    }
    
    public void addPoint(int x, int y)
    {
    	Point point = new Point(x, y);
    	StaticVariables.mainProject.getOptions().registerPoint(point);
    	StaticVariables.mainProject.getActionStack().push(new ActionAdd());
    	System.out.println("Adding new point (hopefully) @" + x + "," + y);
    }
    
    public void setScale()
    {
    	if(StaticVariables.mainProject.scaleBoundary == null || !isScaleSet)
    	{
    		if(!topPaddingUpdated)
    		{
    			TOP_PADDING = getHeight() - layout.getMeasuredHeight();
    			
    			int left = videoView.getLeft();
    			int right = videoView.getRight();
    			int top = videoView.getTop();
    			int bottom = videoView.getBottom();;
    			
    			StaticVariables.mainProject.videoBounds = new Boundary(new Point[] { new Point(left, top), new Point(right, top), new Point(right, bottom), new Point(left, bottom) });
    			topPaddingUpdated = true;
    		}
    		
    		ImageView image = (ImageView) findViewById(R.id.image_view_scale);
            image.setVisibility(View.VISIBLE);
        	
            double height = StaticVariables.mainProject.videoBounds.points[3].y - StaticVariables.mainProject.videoBounds.points[0].y - (getHeight() / 10);
            
    		double left = (getWidth() * 0.25);
			double top = StaticVariables.mainProject.videoBounds.points[0].y + (0.5 * height);//(getHeight() * 0.5) - 20;
			double right = (getWidth() * 0.75);
			double bottom = StaticVariables.mainProject.videoBounds.points[3].y - (0.5 * height);//(getHeight() * 0.5) + 20;
			
			StaticVariables.mainProject.scaleBoundary = new Boundary(new Point[] {
    									 	new Point(left, top),
    									 	new Point(right, top),
    									 	new Point(right, bottom),
    									 	new Point(left, bottom) 
    									 });   
			
			//0, 0, (int)(getWidth() * 0.4), getHeight() / 10
			textBounds = new ClickBoundary(new Point[] { 
											new Point(0, 0),
											new Point(getWidth() * 0.4, 0), 
											new Point(getWidth() * 0.4, getHeight() / 10),
											new Point(0, getHeight() / 10)					
										});
    		
    		
    		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)(right-left), (int)(bottom-top)); 
		    //params.setMargins((int)left, (int)top, (int)right, (int)bottom);
		    params.leftMargin = (int) left;
		    params.topMargin = (int) top;
		    
		    System.out.println(">>>>"+ MathUtils.lineLength(StaticVariables.mainProject.scaleBoundary.centerOfLeftLine(), StaticVariables.mainProject.scaleBoundary.centerOfRightLine()));
		    
    		
    		image.setLayoutParams(params);
        	//*/
    		//image.layout((int)left, (int)top, (int)right, (int)bottom);
        	
        	EditText scaleText = (EditText)(findViewById(R.id.scale_text_zone));
        	scaleText.setText("100");
        	scaleText.setVisibility(View.VISIBLE);


		    RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT); 
		    params1.setMargins(0, 0, (int)(getWidth() * 0.4), getHeight() / 10);
		    
        	scaleText.setLayoutParams(params1);
    		
        	

        	isScaleMeasureDisplayed = true;
        	isScaleSet = true;
        	
    	}
    	
    	
    }
    
    /**
     * "Turns off" all of the following: settingScale, settingOrigin, plottingPoints functions. 
     * Basically prevents something like settingOrigin, plottingPoints at the same time.
     */
    public void resetOriginScalePointSetters()
    {
    	if(isSettingOrigin)
    	{
    		(menu.findItem(R.id.action_bar_set_origin)).setTitle(POSITIVE_MESSAGE);        	
    		isSettingOrigin = false;    
    	}
    	if(isScaleMeasureDisplayed)
    	{
    		EditText scaleText = (EditText)(findViewById(R.id.scale_text_zone));
    		scaleText.setVisibility(View.INVISIBLE);
    		isScaleMeasureDisplayed = false;
    	}
    	if(isSettingScaleRight)
    	{
    		isSettingScaleRight = false;
    	}
    	if(isSettingScaleLeft)
    	{
    		isSettingScaleLeft = false;
    	}
    	if(plottingPoints)
    	{
    		MenuItem item = menu.findItem(R.id.action_bar_plot_points);
    		item.setTitle(START_PLOTTING_POINTS);
    		plottingPoints = false;
    	}
    }
    
    public boolean isInScaleLeftBounds(float x, float y)
    {
    	return (StaticVariables.mainProject.scaleBoundary == null) ? false : StaticVariables.mainProject.scaleBoundary.isInLeftBounds(x, y);
    }
    
    public boolean isInScaleRightBounds(float x, float y)
    {
    	return (StaticVariables.mainProject.scaleBoundary == null) ? false : StaticVariables.mainProject.scaleBoundary.isInRightBounds(x, y);
    }    
    
    public void saveProject()
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	boolean saveInternally = prefs.getBoolean("preference_default_save_option", false);
    	new FileUtils().save(this, saveInternally, StaticVariables.mainProject);
    }
    
    public void reverseLastAction()
    {
    	//Check to make sure there's actually a queue event
    	if(StaticVariables.mainProject.getActionStack().size() > 0)
    	{
    		Action action = StaticVariables.mainProject.getActionStack().pop();
    		String description = action.getDescription();
    		//Last action was adding a point... so remove it
    		if(description.equals(EnumAction.ACTION_ADD_POINT.actionValue))
    		{
    			if(StaticVariables.mainProject.getOptions().getPointCount() > 0)
    	    	{
    	    		Point point = StaticVariables.mainProject.getOptions().removeLastPoint(layout);
    	    		System.out.println("Removing Last Point (hopefully)");
    	    	}
    		}
    		if(description.equals(EnumAction.ACTION_DELETE_POINT.actionValue))
    		{
    			StaticVariables.mainProject.getOptions().registerPoint((Point)(action.getAssociatedObject()));
    		}
    	}
    }
    
    public int getWidth()
	{
		android.view.Display display = getWindowManager().getDefaultDisplay();
		android.graphics.Point size = new android.graphics.Point();
		display.getSize(size);
		return size.x;
	}
	
	public int getHeight()
	{
		android.view.Display display = getWindowManager().getDefaultDisplay();
		android.graphics.Point size = new android.graphics.Point();
		display.getSize(size);
		return size.y;
	}
    
    public void openPreferences()
    {
    	changeActivity();
    	Intent intent = new Intent(this, PreferencesActivity.class);
        intent.putExtra(MAIN_ACTIVITY, NAME_ACTIVITY_MAIN);
        startActivity(intent);
    }

    public void openGraphs()
    {
    	changeActivity();
    	EditText text = (EditText)(findViewById(R.id.scale_text_zone));
    	StaticVariables.mainProject.scaleTextValue = text.getText().toString();
    	/*
    	for(int i = 0; i < 7; i++)
    		StaticVariables.mainProject.getOptions().registerPoint(new Point(i, i,i));
    	StaticVariables.mainProject.getOptions().setDataChanged(true);	
    	//*/
    	
    	Intent intent = new Intent(this, GraphingActivity.class);
        intent.putExtra(MAIN_ACTIVITY, "Unneeded_Method");
        startActivity(intent);
    }

	public boolean onDoubleTap(MotionEvent arg0) {
		return false;
	}

	public boolean onSingleTapConfirmed(MotionEvent e) 
	{
		
		return false;
	}

	public boolean isInScaleBounds(float x, float y)
	{
		return (StaticVariables.mainProject.scaleBoundary == null) ? false : StaticVariables.mainProject.scaleBoundary.contains(x, y);
	}
	
	public boolean onDown(MotionEvent e) 
	{
		
		return false;
	}

	public boolean onSingleTapUp(MotionEvent arg0)
	{	
		return false;
	}
	
	public double angleBetweenTwoVectors(double x1, double y1, double x2, double y2, double x3, double y3)
	{
		double angle1 = Math.atan2(y1 - y2, x1 - x2);
		double angle2 = Math.atan2(y1 - y3, x1 - x3);
		return Math.toDegrees(angle1-angle2);

		
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
		
		/*
		double A = x1 * x2 + y1 * y2;
		double B = Math.sqrt(Math.pow(x1, 2) + Math.pow(y1, 2));
		double C = Math.sqrt(Math.pow(x2, 2) + Math.pow(y2, 2));
		
		double answer = A / (B * C);
		
		return Math.toDegrees(Math.acos(answer));
		*/
	}
		
	public double lineLength(double x1, double y1, double x2, double y2)
	{
		return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
	}
	
	public Point rotate_point(float cx, float cy, float angle, Point p)
	{
	  double s = Math.sin(Math.toRadians(angle));
	  double c = Math.cos(Math.toRadians(angle));
	
	  // translate point back to origin:
	  p.x -= cx;
	  p.y -= cy;
	
	  // rotate point
	  double xnew = p.x * c - p.y * s;
	  double ynew = p.x * s + p.y * c;
	
	  // translate point back:
	  p.x = xnew + cx;
	  p.y = ynew + cy;
	  
	  return p;
	}
	
	public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight)
	{
	    int width = bm.getWidth();
	    int height = bm.getHeight();	    
	    float scaleWidth = (float) ((float) (newWidth) / (getWidth() * 0.5));
	    float scaleHeight = ((float) newHeight) / height;
	    
	    Matrix matrix = new Matrix();
	    matrix.postScale(scaleWidth, scaleHeight);
	    return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
	}
	
	public double angleOfLine(Point point1, Point point2)
	{
		double xLen = (point2.x - point1.x);
		double yLen = (point2.y - point1.y);
		double angle = Math.toDegrees(Math.atan((yLen/xLen)));
		return 360 + angle;
	}
	
	public boolean onScroll(MotionEvent e0, MotionEvent e, float arg2, float arg3) 
	{
		return false;
	}
	
	public void mouseOnDown(MotionEvent e)
	{
		//"Mouse Down"

		if(StaticVariables.mainProject == null || StaticVariables.mainProject.scaleBoundary == null)
		{
			//Should Register an Error
			return;
		}	
		
		float x = e.getRawX();
		float y = e.getRawY() - TOP_PADDING;
		//System.out.println("on_down@(" + x + "," + y + ")");
		if(!StaticVariables.mainProject.videoBounds.contains(x, y))
		{
			//resetOriginScalePointSetters();
			return;
		}
		//System.out.println(isInScaleLeftBounds(x, y) + ", " + isInScaleRightBounds(x, y) + " " + scaleBoundary.left + " " + 
		//		scaleBoundary.right + " " + scaleBoundary.top + " " + scaleBoundary.bottom);
		
		if(isInScaleLeftBounds(x, y))
		{
		//	resetOriginScalePointSetters();
			isSettingScaleLeft = true;
		//	isInScaleLeftBounds(x, y);
		}
		if(isInScaleRightBounds(x, y))
		{
		//	resetOriginScalePointSetters();
			isSettingScaleRight = true;
		//	isInScaleRightBounds(x, y);
		}
		
		TextView txt =((TextView)(findViewById(R.id.scale_text_zone)));
		if(isInScaleBounds(x, y) || (isInTextBounds(x, y) && txt.getVisibility() == View.VISIBLE)
				)
		{
			//resetOriginScalePointSetters();
			isScaleMeasureDisplayed = true;
	    }
		else
		{
			isScaleMeasureDisplayed = false;
		}
		
		//System.out.println("on_down@" + isSettingScaleLeft + ", " + isSettingScaleRight);
				
	}
	
	public boolean isInTextBounds(float x, float y)
	{
		return textBounds.contains(x, y);
	}
	
	public void mouseOnSingleTap(MotionEvent e)
	{
		if(StaticVariables.mainProject == null)
		{
			//Should Register an Error
		}		
		
		//There is padding of 75 px at the top (so 0, 75) is basically (0,0) of the useable area.
		//usable area is therefore 320x405
		float x = e.getRawX();
		float y = e.getRawY() - TOP_PADDING;
		//System.out.println("Single_Tap_Confirmed @(" + x + "," + y + ")");
		
		if(!StaticVariables.mainProject.videoBounds.contains(x, y))
		{
			//resetOriginScalePointSetters();
			return;
		}
		
		//Possibly move this to the touch event (so it can be a double click)
		if(isSettingOrigin)
		{
			displayOrigin(x, y + TOP_PADDING);
		}
		else if(isInOriginBounds(x, y)) //If the origin center point is clicked, lets the axis-be reset.
		{
			//Consider removing this unless it changes the cursor. Could be annoying or wierd to not know it's active.
			//Maybe toggle the menu item? 
			isSettingOrigin = true;
		}
		
		if(plottingPoints)
		{
			if(StaticVariables.mainProject.options.doesThisTimeExist(getTime()))
			{
				return;
			}
			
			final int SIZE = 4;
			ImageView image = new ImageView(this);
			image.setImageResource(R.drawable.point);
			image.setAdjustViewBounds(true);
			StaticVariables.mainProject.getOptions().registerPoint(new Point(x, y + TOP_PADDING, getTime(),image));
			
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(2 * SIZE, 2 * SIZE);
			params.leftMargin = (int) x - SIZE;
			params.topMargin = (int) y - SIZE;			
			layout.addView(image, params);			
		}
		
	}
	
	public double getTime()
	{
		return videoView.getCurrentPosition();
	}
	
	public void mouseMove(MotionEvent e)
	{
		loops = 0;

		if(System.currentTimeMillis() > next_game_tick && loops < MAX_FRAMESKIP)
	    {
			float x = e.getRawX();
			float y = e.getRawY() - TOP_PADDING;
			
			if(!StaticVariables.mainProject.videoBounds.contains(x, y))
			{
				//resetOriginScalePointSetters();
				//return;
			}
			
			if(isSettingScaleLeft)
			{
				ImageView image = (ImageView) findViewById(R.id.image_view_scale);
				Point rightPoint = StaticVariables.mainProject.scaleBoundary.centerOfRightLine();
				Point leftPoint = StaticVariables.mainProject.scaleBoundary.centerOfLeftLine();
				Point centerPoint = StaticVariables.mainProject.scaleBoundary.getPerfectCenter();
			    
				double lineLength = lineLength(rightPoint.getX(), rightPoint.getY(), x, y);
				
				System.out.println(">>" + lineLength);
				
				double angle = angleBetweenTwoVectors(rightPoint.x, rightPoint.y, x, y, leftPoint.getX(), leftPoint.getY());
				StaticVariables.mainProject.scaleBoundary.totalRotLeft += angle;				
				
				StaticVariables.mainProject.scaleBoundary.updateLengthLeft(lineLength);
				StaticVariables.mainProject.scaleBoundary.rotateOnRightCenter(angle);
				
			    double left = StaticVariables.mainProject.scaleBoundary.points[0].x;
			    double right = StaticVariables.mainProject.scaleBoundary.points[0].x;
			    double top = StaticVariables.mainProject.scaleBoundary.points[0].y;
			    double bottom = StaticVariables.mainProject.scaleBoundary.points[0].y;
			    
			    for(int i = 0; i < StaticVariables.mainProject.scaleBoundary.points.length; i++)
			    {
			    	Point p = StaticVariables.mainProject.scaleBoundary.points[i];
			    	
			    	if(p.x < left)
			    		left = p.x;
			    	if(p.x > right)
			    		right = p.x;
			    	if(p.y < top)
			    		top = p.y;
			    	if(p.y > bottom)
			    		bottom = p.y;
			    }
			    
			    Matrix matrix = new Matrix();
				image.setScaleType(ScaleType.MATRIX);
				matrix.setRotate((float)(StaticVariables.mainProject.scaleBoundary.totalRotRight + StaticVariables.mainProject.scaleBoundary.totalRotLeft), (int)centerPoint.getX(), (int)centerPoint.getY());//, (int)rightPoint.getX(), (int)rightPoint.getY()

				Bitmap resizedBitmap = getResizedBitmap(StaticVariables.scaleImage, (int)lineLength, StaticVariables.scaleImage.getHeight());
			    Bitmap img2 = Bitmap.createBitmap(resizedBitmap, 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight(), matrix, true);
			    image.setImageBitmap(img2);			 
			    
			    /*
			    DrawablePanel panel_drawing = (DrawablePanel)(findViewById(R.id.drawable_area_paint_thing));
			    panel_drawing.redraw(new Point[] {
					 	new Point(left, top),
					 	new Point(right, top),
					 	new Point(right, bottom),
					 	new Point(left, bottom),
					 	new Point(left, top),
					 	scaleBoundary.points[0],
					 	scaleBoundary.points[1],
					 	scaleBoundary.points[2],
					 	scaleBoundary.points[3], scaleBoundary.points[0], rightPoint, leftPoint,
					 	scaleBoundary.leftBoundary.points[0],
					 	scaleBoundary.leftBoundary.points[1],
					 	scaleBoundary.leftBoundary.points[2],
					 	scaleBoundary.leftBoundary.points[3],
					 	scaleBoundary.leftBoundary.points[0],
					 	scaleBoundary.rightBoundary.points[0],
						scaleBoundary.rightBoundary.points[1],
						scaleBoundary.rightBoundary.points[2],
						scaleBoundary.rightBoundary.points[3],
						scaleBoundary.rightBoundary.points[0]
				});
			    */
			    
			    double hypotenuse = Math.sqrt(Math.pow(bottom - top, 2) + Math.pow(right-left, 2));
			    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)hypotenuse, (int)hypotenuse);
			    params.leftMargin = (int) (left);
			    params.topMargin = (int) top;			    
			    //move the image to where it should be... not (0, 0)
			    layout.removeView(image);
			    layout.addView(image, params);	
			    
			    RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT); 
			    params1.setMargins(0, 0, (int)(getWidth() * 0.4), getHeight() / 10);
			    
	        	EditText scaleText = (EditText)(findViewById(R.id.scale_text_zone));
	        	scaleText.setLayoutParams(params1);
	 		}
			else if(isSettingScaleRight)
			{
				ImageView image = (ImageView) findViewById(R.id.image_view_scale);
				Point rightPoint = StaticVariables.mainProject.scaleBoundary.centerOfRightLine();
				Point leftPoint = StaticVariables.mainProject.scaleBoundary.centerOfLeftLine();
				Point centerPoint = StaticVariables.mainProject.scaleBoundary.getPerfectCenter();
			    double lineLength = lineLength(leftPoint.getX(), leftPoint.getY(), x, y);
				double angle = angleBetweenTwoVectors(leftPoint.getX(), leftPoint.getY(), x, y, rightPoint.x, rightPoint.y);
				StaticVariables.mainProject.scaleBoundary.totalRotRight += angle;				
				StaticVariables.mainProject.scaleBoundary.updateLengthRight(lineLength);
				StaticVariables.mainProject.scaleBoundary.rotateOnLeftCenter(angle);
				
				double left = StaticVariables.mainProject.scaleBoundary.points[0].x;
			    double right = StaticVariables.mainProject.scaleBoundary.points[0].x;
			    double top = StaticVariables.mainProject.scaleBoundary.points[0].y;
			    double bottom = StaticVariables.mainProject.scaleBoundary.points[0].y;
			    
			    for(int i = 0; i < StaticVariables.mainProject.scaleBoundary.points.length; i++)
			    {
			    	Point p = StaticVariables.mainProject.scaleBoundary.points[i];
			    	
			    	if(p.x < left)
			    		left = p.x;
			    	if(p.x > right)
			    		right = p.x;
			    	if(p.y < top)
			    		top = p.y;
			    	if(p.y > bottom)
			    		bottom = p.y;			    	
			    }
			    
			    Matrix matrix = new Matrix();
				image.setScaleType(ScaleType.MATRIX);
				matrix.setRotate((float)(StaticVariables.mainProject.scaleBoundary.totalRotRight + StaticVariables.mainProject.scaleBoundary.totalRotLeft), (int)centerPoint.getX(), (int)centerPoint.getY());//, (int)rightPoint.getX(), (int)rightPoint.getY()
		
				Bitmap resizedBitmap = getResizedBitmap(StaticVariables.scaleImage, (int)lineLength, StaticVariables.scaleImage.getHeight());
			    Bitmap img2 = Bitmap.createBitmap(resizedBitmap, 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight(), matrix, true);
			    image.setImageBitmap(img2);
			    	
			    /*
			    DrawablePanel panel_drawing = (DrawablePanel)(findViewById(R.id.drawable_area_paint_thing));
			    panel_drawing.redraw(new Point[] {
					 	new Point(left, top),
					 	new Point(right, top),
					 	new Point(right, bottom),
					 	new Point(left, bottom),
					 	new Point(left, top),					 	
					 	scaleBoundary.points[0],
					 	scaleBoundary.points[1],
					 	scaleBoundary.points[2],
					 	scaleBoundary.points[3], scaleBoundary.points[0], rightPoint, leftPoint,
					 	scaleBoundary.leftBoundary.points[0],
					 	scaleBoundary.leftBoundary.points[1],
					 	scaleBoundary.leftBoundary.points[2],
					 	scaleBoundary.leftBoundary.points[3],
					 	scaleBoundary.leftBoundary.points[0],					 	
					 	scaleBoundary.rightBoundary.points[0],
						scaleBoundary.rightBoundary.points[1],
						scaleBoundary.rightBoundary.points[2],
						scaleBoundary.rightBoundary.points[3],
						scaleBoundary.rightBoundary.points[0]					
			    });
			    */
			    
			    double hypotenuse = Math.sqrt(Math.pow(bottom - top, 2) + Math.pow(right-left, 2));
			    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)hypotenuse, (int)hypotenuse);
			    params.leftMargin = (int) (left);
			    params.topMargin = (int) top;			    
			    //move the image to where it should be... not (0, 0)
			    layout.removeView(image);
			    layout.addView(image, params);			
			


			    RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT); 
			    params1.setMargins(0, 0, (int)(getWidth() * 0.4), getHeight() / 10);
			    
	        	EditText scaleText = (EditText)(findViewById(R.id.scale_text_zone));
	        	scaleText.setLayoutParams(params1);
	 		}
			
			next_game_tick += SKIP_TICKS;
	        loops++;
	    }
	}
	
	public boolean onDoubleTapEvent(MotionEvent e) 
	{
		return false;
	}
	
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3)
	{
		return false;
	}

	public void onLongPress(MotionEvent arg0) 
	{	
	}
	
	public void onShowPress(MotionEvent arg0)
	{
	}

	public boolean onTouchEvent(MotionEvent event)
	{ 
		this.mDetector.onTouchEvent(event);	   
	    return super.onTouchEvent(event);
	}
	
	public boolean dispatchTouchEvent(MotionEvent event) 
	{
		if(!topPaddingUpdated)
		{
			TOP_PADDING = getHeight() - layout.getMeasuredHeight();
			
			int left = videoView.getLeft();
			int right = videoView.getRight();
			int top = videoView.getTop();
			int bottom = videoView.getBottom();;
			
			StaticVariables.mainProject.videoBounds = new Boundary(new Point[] { new Point(left, top), new Point(right, top), new Point(right, bottom), new Point(left, bottom) });
			topPaddingUpdated = true;
		}
		
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			mouseOnDown(event);
		}
		if(event.getAction() == MotionEvent.ACTION_SCROLL);
		{
			mouseMove(event);
		}
		
		if(event.getAction() == MotionEvent.ACTION_UP)
	   	{
			mouseOnSingleTap(event);
			isSettingScaleRight = false;
			isSettingScaleLeft = false;
	   	}
		
	   	if(isScaleMeasureDisplayed)
	   	{
	   		EditText scaleText = (EditText)(findViewById(R.id.scale_text_zone));        	
        	scaleText.setVisibility(View.VISIBLE);
	   	}
	   	else
	   	{
	   		EditText scaleText = (EditText)(findViewById(R.id.scale_text_zone));        	
        	scaleText.setVisibility(View.INVISIBLE);
	   	}
	   	
	    return super.dispatchTouchEvent(event);
	}
}