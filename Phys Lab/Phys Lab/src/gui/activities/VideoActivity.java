package gui.activities;

import global.StaticVariables;

import java.io.File;

import project.Project;
import utils.action.StackAction;
import utils.file.FileUtils;
import utils.graphing.Boundary;
import utils.graphing.ClickBoundary;
import utils.graphing.GraphableObjectContainer;
import utils.graphing.Point;
import utils.graphing.ScaleBoundary;
import utils.preferences.ChooseProjectDialog;
import utils.preferences.ConfirmActionDialog;
import utils.preferences.VideoConfirmDialogFragment;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

public class VideoActivity extends Activity 
		implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener,
		ConfirmActionDialog.ConfirmActionDialogListener
{
	public final static String MAIN_ACTIVITY = "gui.activities.PREFERENCE_MESSAGE";
	public final static String NAME_ACTIVITY_MAIN = "main_activity";
	public final static String KEY_INTENT_WRN_MSG_CHANGE_VIDEO = "message.video.change.wrn";
	public final static String ERROR_EXTERNAL_STORAGE_NOT_READABLE = "We don't appear to have read rights on your device's storage device (most likely an SD card).";
    private int videoCheckDelay = 1000;

	private boolean plottingPoints;
	private GestureDetector mDetector; 
	private boolean isSettingOrigin;
	private int TOP_PADDING = 75;	
	private final int AXIS_SIZE = 15; // half the actual size (reduces math)
	private boolean isSettingScaleLeft;
	private boolean isSettingScaleRight;
	private boolean isScaleSet = false;
	private boolean topPaddingUpdated = false;
	public static MediaController mediaController;
	public static VideoView videoView;
	public static long vidLength;
	public static EditText frameSelected;
	public static SeekBar seeker;
	private RelativeLayout layout;
    private boolean updatedFrame = false;
    private int lasttime = 0;
    private boolean redraw;
    private boolean ignoreTextEvent = false;
    private Boundary actionbarBoundary;
	private ClickBoundary textBounds;
	private boolean isRemovingPoints = false;
	
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
        mDetector = new GestureDetector(this, this);
        // Set the gesture detector as the double tap listener
        mDetector.setOnDoubleTapListener(this);
        
        topPaddingUpdated = false;
        
        videoView = (VideoView) findViewById(R.id.track);
               
        if (StaticVariables.clip != null && StaticVariables.clip.exists())
        {
    		initializeVideo();    
    		StaticVariables.clipChanged = false;
    	}    
        
        topPaddingUpdated = false;
        
        if(StaticVariables.mainProject != null)
        {
        	StaticVariables.mainProject.setDimensions(getWidth(), getHeight());
        }
     
        layout = (RelativeLayout)(findViewById(R.id.activity_video_relative_layout));

        initActionbar();
        
        redraw = true;
        
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener()
        {
            public void onGlobalLayout() 
            {
            	if(redraw)
                {
            		redraw();	               		
            		redraw = false;    
                }
            }
        });        
    }
    
    public void onResume()
    {
    	super.onResume();
    	initActionbar();
    }
    
    /**
     * Value 1- scale left
     * Value 2- scale right
     * value 3- origin
     * value 4 - plotting points. 
     * value 5 - remove points
     * True = doing that. Only 1 value can be true.
     */
    private boolean[] activeMode = new boolean[5];
    
    public void initActionbar()
    {
    	final ImageView scaleLeft = (ImageView)(findViewById(R.id.action_bar_scale_left));
    	final ImageView scaleRight = (ImageView)(findViewById(R.id.action_bar_scale_right));
    	final ImageView origin = (ImageView)(findViewById(R.id.action_bar_set_origin));
    	final ImageView plotPoints = (ImageView)(findViewById(R.id.action_bar_plot_points));
    	final ImageView removePoints = (ImageView)(findViewById(R.id.action_bar_remove_points));
    	
    	final Drawable scaleLeftOn = getResources().getDrawable(R.drawable.icon_scale_left_on);
    	final Drawable scaleLeftOff = getResources().getDrawable(R.drawable.icon_scale_left_off);
    	final Drawable scaleRightOn = getResources().getDrawable(R.drawable.icon_scale_right_on);
    	final Drawable scaleRightOff = getResources().getDrawable(R.drawable.icon_scale_right_off);
    	final Drawable originOn = getResources().getDrawable(R.drawable.icon_origin_on);
    	final Drawable originOff = getResources().getDrawable(R.drawable.icon_origin_off);
    	final Drawable plotPointsOn = getResources().getDrawable(R.drawable.icon_plot_points_on);
    	final Drawable plotPointsOff = getResources().getDrawable(R.drawable.icon_plot_points_off);
    	final Drawable removePointsOn = getResources().getDrawable(R.drawable.icon_remove_points_on);
    	final Drawable removePointsOff = getResources().getDrawable(R.drawable.icon_remove_points_off);
    	
    	final LinearLayout linear_layout = (LinearLayout)(findViewById(R.id.action_bar_layout));
    	    	
		final TextView scale = ((TextView)(findViewById(R.id.scale_text_zone)));
		
    	scaleLeft.setOnClickListener(new View.OnClickListener(){
    	    public void onClick(View v) {
		     	if(activeMode[0])
		     	{
		     		return;
		     	}
    	    	
    	    	if(activeMode[1])
    	    	{
    	    		scaleRight.setImageDrawable(scaleRightOff);
    	    		activeMode[1] = false;
    	    	}
    	    	if(activeMode[2])
    	    	{
    	    		origin.setImageDrawable(originOff);
    	    		activeMode[2] = false;
    	    	}
    	    	if(activeMode[3])
    	    	{
    	    		plotPoints.setImageDrawable(plotPointsOff);
    	    		activeMode[3] = false;
    	    	}
    	    	if(activeMode[4])
    	    	{
    	    		removePoints.setImageDrawable(removePointsOff);
    	    		activeMode[4] = false;
    	    	}
    	    	
    	    	activeMode[0] = true;
    	    	setScale();
    	    	resetOriginScalePointSetters();
    	    	isSettingScaleLeft = true;
    	    	scale.setVisibility(View.VISIBLE);
		    	scaleLeft.setImageDrawable(scaleLeftOn);
		    	linear_layout.invalidate();
		    }
    	});

    	scaleRight.setOnClickListener(new View.OnClickListener(){
    	    public void onClick(View v) {
    	    	if(activeMode[1])
    	    	{
    	    		return;
    	    	}
    	    	
    	    	if(activeMode[0])
    	    	{
    	    		scaleLeft.setImageDrawable(scaleLeftOff);
    	    		activeMode[0] = false;
    	    	}
    	    	if(activeMode[2])
    	    	{
    	    		origin.setImageDrawable(originOff);
    	    		activeMode[2] = false;
    	    	}
    	    	if(activeMode[3])
    	    	{
    	    		plotPoints.setImageDrawable(plotPointsOff);
    	    		activeMode[3] = false;
    	    	}
    	    	if(activeMode[4])
    	    	{
    	    		removePoints.setImageDrawable(removePointsOff);
    	    		activeMode[4] = false;
    	    	}
    	    	
    	    	activeMode[1] = true;
    	    	setScale();
    	    	resetOriginScalePointSetters();
    	    	isSettingScaleRight = true;
    	    	scale.setVisibility(View.VISIBLE);
    	    	scaleRight.setImageDrawable(scaleRightOn);
		    	linear_layout.invalidate();
    	    }
    	});

    	origin.setOnClickListener(new View.OnClickListener(){
    	    public void onClick(View v) {
    	    	if(activeMode[2])
    	    	{
    	    		return;
    	    	}
    	    	
    	    	if(activeMode[0])
    	    	{
    	    		scaleLeft.setImageDrawable(scaleLeftOff);
    	    		activeMode[0] = false;
    	    	}
    	    	if(activeMode[1])
    	    	{
    	    		scaleRight.setImageDrawable(scaleRightOff);
    	    		activeMode[1] = false;
    	    	}
    	    	if(activeMode[3])
    	    	{
    	    		plotPoints.setImageDrawable(plotPointsOff);
    	    		activeMode[3] = false;
    	    	}
    	    	if(activeMode[4])
    	    	{
    	    		removePoints.setImageDrawable(removePointsOff);
    	    		activeMode[4] = false;
    	    	}
    	    	
    	    	activeMode[2] = true;
    	    	resetOriginScalePointSetters();
    	    	isSettingOrigin = true;
    	    	scale.setVisibility(View.INVISIBLE);
		    	origin.setImageDrawable(originOn);
		    	linear_layout.invalidate();
		    }
    	});

    	plotPoints.setOnClickListener(new View.OnClickListener(){
    	    public void onClick(View v) {
    	    	if(activeMode[3])
    	    	{
    	    		return;
    	    	}
    	    	
    	    	if(activeMode[0])
    	    	{
    	    		scaleLeft.setImageDrawable(scaleLeftOff);
    	    		activeMode[0] = false;
    	    	}
    	    	if(activeMode[1])
    	    	{
    	    		scaleRight.setImageDrawable(scaleRightOff);
    	    		activeMode[1] = false;
    	    	}
    	    	if(activeMode[2])
    	    	{
    	    		origin.setImageDrawable(originOff);
    	    		activeMode[2] = false;
    	    	}
    	    	if(activeMode[4])
    	    	{
    	    		removePoints.setImageDrawable(removePointsOff);
    	    		activeMode[4] = false;
    	    	}
    	    
    	    	activeMode[3] = true;
    	    	resetOriginScalePointSetters();
    	    	plottingPoints = true;	
    	    	scale.setVisibility(View.INVISIBLE);
    	    	plotPoints.setImageDrawable(plotPointsOn);
		    	linear_layout.invalidate();
    	    }
    	});
    	
    	removePoints.setOnClickListener(new View.OnClickListener(){
    	    public void onClick(View v) {
    	    	if(activeMode[4])
    	    	{
    	    		return;
    	    	}
    	    	
    	    	if(activeMode[0])
    	    	{
    	    		scaleLeft.setImageDrawable(scaleLeftOff);
    	    		activeMode[0] = false;
    	    	}
    	    	if(activeMode[1])
    	    	{
    	    		scaleRight.setImageDrawable(scaleRightOff);
    	    		activeMode[1] = false;
    	    	}
    	    	if(activeMode[2])
    	    	{
    	    		origin.setImageDrawable(originOff);
    	    		activeMode[2] = false;
    	    	}
    	    	if(activeMode[3])
    	    	{
    	    		plotPoints.setImageDrawable(plotPointsOff);
    	    		activeMode[3] = false;
    	    	}
    	    
    	    	activeMode[4] = true;
    	    	resetOriginScalePointSetters();
    	    	isRemovingPoints = true;	
    	    	scale.setVisibility(View.INVISIBLE);
    	    	removePoints.setImageDrawable(removePointsOn);
		    	linear_layout.invalidate();
    	    }
    	});
    }
    
    public void onStart()
    {
    	super.onStart();
    	if (StaticVariables.clip != null && StaticVariables.clip.exists() && StaticVariables.clipChanged)
        {
    		initializeVideo(); 
    		StaticVariables.clipChanged = false;
    	}    
    }
    
    public void onStop()
    {
    	super.onStop();
    }
    
    public void onPause()
    {
    	super.onPause();
    	
    	if(videoView != null)
    	{
    		videoView.pause();
    	}
    }
    
    public void clearData()
    {
    	StaticVariables.mainProject.origin = null;
    	StaticVariables.mainProject.clearAllPoints(layout);
		StaticVariables.mainProject.actionStack = new StackAction(Project.ACTION_QUEUE_SIZE);
    	StaticVariables.mainProject.graphable = new GraphableObjectContainer();
    	StaticVariables.mainProject.videoBounds = null;
    	StaticVariables.mainProject.scaleBoundary = null;
    	resetOriginScalePointSetters();
    	topPaddingUpdated = false;
    }

    public int parseInt(String s)
    {
    	try
    	{
    		int i = Integer.parseInt(s);
    		return i;
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		return 0;
    	}
    }

    public void initializeSeekAndStep(){
    	frameSelected = (EditText) findViewById(R.id.frame_number);
    	frameSelected.setText(""+StaticVariables.mainProject.frameSkip);
    	frameSelected.bringToFront();
    	frameSelected.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
            	try
            	{
            		if(!ignoreTextEvent)
            		{
	            		int time = Integer.parseInt(frameSelected.getText().toString()) * 33;
	            		if(time != lasttime)
	            		{
	                    	ignoreTextEvent = true;
	                    	if(videoView.isPlaying())
	            			{
	                    		videoView.seekTo(time);
	            			}
				    		seeker.setProgress(videoView.getCurrentPosition());
				    		lasttime = time;
	            		}
	            	}
            		ignoreTextEvent = false;
            	}
            	catch(Exception e)
            	{
            		e.printStackTrace();
            	}            	
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        }); 
    	
        seeker = (SeekBar) findViewById(R.id.seekControl);
        System.out.println(vidLength);
        seeker.setMax((int) vidLength);
        //Allow the seekBar to actually control the video's position
        seeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {            
            public void onProgressChanged(SeekBar seeker, int progress, boolean fromUser) {
            	if (fromUser){            		
            		videoView.seekTo(progress);
            		frameSelected.setText("" + videoView.getCurrentPosition() / 33);
            	}
            }

			public void onStartTrackingTouch(SeekBar arg0) {}
			public void onStopTrackingTouch(SeekBar arg0) {}
         });        	
    }
    
    public Runnable onEverySecond = new Runnable() 
    {
        public void run() 
        {
            if(videoView.isPlaying() && seeker != null) 
            {
            	System.out.println(">"+videoView.getCurrentPosition() + ", " + vidLength);
            	seeker.setProgress(videoView.getCurrentPosition());
            	frameSelected.setText("" + (videoView.getCurrentPosition() / 33));
            	updatedFrame = true;
            }
            
            if(updatedFrame)
            {
               	System.out.println(">"+videoView.getCurrentPosition() + ", " + vidLength);
                
            	seeker.setProgress(videoView.getCurrentPosition());
            	frameSelected.setText("" + (videoView.getCurrentPosition() / 33));
                updatedFrame = false;
            }
            
            seeker.postDelayed(onEverySecond, videoCheckDelay);
        }
    };
    
    public void stepVideo(View v)
    {
    	try
    	{
	    	if(videoView == null || !plottingPoints)
	    	{
	    		return;
	    	}
	    	
	    	videoView.pause();
	    	
	    	//If it's possible to step to the correct time, do so
	    	if (videoView.getCurrentPosition() + (StaticVariables.mainProject.frameSkip * 33)  < vidLength){
	    		videoView.seekTo(videoView.getCurrentPosition() + (StaticVariables.mainProject.frameSkip * 33));
	    		seeker.setProgress(videoView.getCurrentPosition());
	    		frameSelected.setText("" + videoView.getCurrentPosition() / 33);
	        }
	    	//Otherwise step to the end
	    	else{
	    		videoView.seekTo((int) vidLength);
	    		seeker.setProgress(videoView.getCurrentPosition());
	    		frameSelected.setText("" + videoView.getCurrentPosition() / 33);
	        }
	    	System.out.println(videoView.getCurrentPosition());
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }
    
    public void initializeVideo()
    {
    	videoView.setVideoPath(StaticVariables.clip.getAbsolutePath());
    	
    	AudioManager mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
	    int set_volume = 0;
	    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, set_volume, 0);
    	
    	//Get the length of the video, in milliseconds, once loaded, then continue setup
    	videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			public void onPrepared(MediaPlayer mp){    				
				vidLength = videoView.getDuration();
				
				//Video less than 10 seconds, adjust the seekbar update time
				if(vidLength < 10000)
				{
					videoCheckDelay = (int) (((vidLength / 10) >= 200) ? (vidLength / 10) : 200); 
				}
				
				initializeSeekAndStep();
				seeker.setMax(videoView.getDuration());
		        seeker.postDelayed(onEverySecond, videoCheckDelay);
				videoView.start();
		    	videoView.pause();		    	
			};
		});
    }
        
    //Method to step the video and send a data point
    public void videoClick(View v){
    	stepVideo(v);  
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
            case R.id.action_bar_main_menu:
            	returnToMenu();
            	return true;
            case R.id.action_bar_change_video:
            	returnToVideo();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void returnToMenu()
    {
    	ConfirmActionDialog dialog1 = new ConfirmActionDialog("Yes", "No", "Return to main menu?", "proj_exit_check");
    	dialog1.show(getFragmentManager(), "user_query_save_before_exit");
    }
    
	public void onActionConfirm(DialogFragment dialog, String message) {

		if(message.equals("proj_exit_check"))
		{
	    	ConfirmActionDialog dialog1 = new ConfirmActionDialog("Yes", "No", "Save project to disk before exiting?", "save");
	    	dialog1.show(getFragmentManager(), "user_query_save_before_exit");
		}

		if(message.equals("save"))
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    	boolean saveInternally = prefs.getBoolean("preference_default_save_option", false);
	    	new FileUtils().save(this, saveInternally, StaticVariables.mainProject);
			closeToMainMenu();
		}
	}
	
	private void closeToMainMenu()
	{
		Intent i = new Intent(this, TitleActivity.class);
		startActivity(i);
	}

	public void onActionCancel(DialogFragment dialog, String message) {
		if(message.equals("save")){
			closeToMainMenu();
		}
	}
    
    public void removePoint()
    {
    	StaticVariables.mainProject.removePointByTime(layout, getTime());
    }
    
    public void redraw()
    {
    	final TextView scale = ((TextView)(findViewById(R.id.scale_text_zone)));
		
    	textBounds = new ClickBoundary(new Point[] { 
				new Point(scale.getLeft(), scale.getTop()),
				new Point(scale.getRight(), scale.getTop()), 
				new Point(scale.getRight(), scale.getBottom()),
				new Point(scale.getLeft(), scale.getBottom())					
			});
    	
    	
		TOP_PADDING = getHeight() - layout.getMeasuredHeight();
			
		double left = videoView.getLeft();
		double right = videoView.getRight();
		double top = videoView.getTop();
		double bottom = videoView.getBottom();
		
		StaticVariables.mainProject.videoBounds = new Boundary(new Point[] { new Point(left, top), new Point(right, top), new Point(right, bottom), new Point(left, bottom) });
		
    	Point[] points = StaticVariables.mainProject.getPointsAsArray();
    	
    	for(int i = 0; i < points.length; i++)
    	{
    		double x = points[i].x;
    		double y = points[i].y - TOP_PADDING;
    		final int SIZE = 4;
			
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(2 * SIZE, 2 * SIZE);
			params.leftMargin = (int) x - SIZE;
			params.topMargin = (int) y - SIZE;			
			
			if(points[i].imageView == null)
			{
				points[i].imageView = new ImageView(this);
				points[i].imageView.setImageResource(R.drawable.point);
				points[i].imageView.setAdjustViewBounds(true);
			}
			
			final Point point = points[i];
			
			points[i].imageView.setOnClickListener(new View.OnClickListener(){
	    	    public void onClick(View v) {
		    	    	if(isRemovingPoints)
		    	    	{
		    	    		StaticVariables.mainProject.removePointByValue(layout, point);
		    	    	}
	    	    	}
	    	    });
			
			layout.addView(points[i].imageView, params);
    	}
    	
    	if(StaticVariables.mainProject.origin != null)
    	{
    		double x = StaticVariables.mainProject.origin.x;
    		double y = StaticVariables.mainProject.origin.y;
    		
    		ImageView imageVA = (ImageView) findViewById(R.id.image_view_vertical_axis);
    		ImageView imageHA = (ImageView) findViewById(R.id.image_view_horizontal_axis);
    		
    		imageHA.setVisibility(View.VISIBLE);
    		imageVA.setVisibility(View.VISIBLE);
    		    		
    		double videoHeight = StaticVariables.mainProject.videoBounds.points[3].y - StaticVariables.mainProject.videoBounds.points[0].y;
    		
    		double height1 = (int)((y + AXIS_SIZE - TOP_PADDING) - (y - AXIS_SIZE - TOP_PADDING));
    		if(height1 > videoHeight)
    		{
    			height1 = videoHeight - (y - AXIS_SIZE - TOP_PADDING);
    		}
    		
    		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(getWidth(), (int) height1); 
    		params.leftMargin = 0;
    		params.topMargin = (int)y - AXIS_SIZE - TOP_PADDING;
    		
    		double height2 = getHeight();
    		if(height2 > videoHeight)
    		{
    			height2 = videoHeight;
    		}
    		
    		RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams((int) ((x + AXIS_SIZE) - (x - AXIS_SIZE)), (int) height2); 
    		params1.leftMargin = (int)x - AXIS_SIZE;
    		params1.topMargin = 0;
    		
    		
    		double height3 = (int)((y + AXIS_SIZE - TOP_PADDING) - (y - AXIS_SIZE - TOP_PADDING));
    		if(height3 > videoHeight)
    		{
    			height3 = videoHeight - (y - AXIS_SIZE - TOP_PADDING);
    		}
    		
    		imageHA.setLayoutParams(params);
    		imageVA.setLayoutParams(params1);
    		
        	isSettingOrigin = false;
    	}
    	
    	if(StaticVariables.mainProject.scaleBoundary != null)
    	{
    		
    		ImageView image = (ImageView) findViewById(R.id.image_view_scale);
			Point rightPoint = StaticVariables.mainProject.scaleBoundary.points[1];
			image.setVisibility(View.VISIBLE);
			Point leftPoint = StaticVariables.mainProject.scaleBoundary.points[0];
		    StaticVariables.mainProject.scaleBoundary.points[0] = leftPoint;
			
			double lineLength = lineLength(rightPoint.getX(), rightPoint.getY(), leftPoint.x, leftPoint.y);
						
			double angle = angleBetweenTwoVectors(rightPoint.x, rightPoint.y, getWidth(), rightPoint.y, leftPoint.x, leftPoint.y);
			angle*=-1;
			
			StaticVariables.mainProject.scaleBoundary.totalRot = angle;	
			
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
			matrix.setRotate((float)(StaticVariables.mainProject.scaleBoundary.totalRot));//, (int)rightPoint.getX(), (int)rightPoint.getY());

			Bitmap resizedBitmap = getResizedBitmap(StaticVariables.scaleImage, (int)lineLength, StaticVariables.scaleImage.getHeight());
		    Bitmap img2 = Bitmap.createBitmap(resizedBitmap, 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight(), matrix, true);
		    image.setImageBitmap(img2);			 
		  			    
		    double hypotenuse = Math.sqrt(Math.pow(bottom - top, 2) + Math.pow(right-left, 2));
		    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)hypotenuse, (int)hypotenuse);
		    params.leftMargin = (int) (left);
		    params.topMargin = (int) top;			    
		    
		    image.setLayoutParams(params);
    	}    
    }
    
    public void changeActivity()
    {
    	Point[] points = StaticVariables.mainProject.getPointsAsArray();
    	
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

	
	public void displayOrigin(float x, float y)
	{
		StaticVariables.mainProject.origin = new Point(x, y);
		
		ImageView imageVA = (ImageView) findViewById(R.id.image_view_vertical_axis);
		ImageView imageHA = (ImageView) findViewById(R.id.image_view_horizontal_axis);
		imageHA.setVisibility(View.VISIBLE);
		imageVA.setVisibility(View.VISIBLE);
		
		double videoHeight = StaticVariables.mainProject.videoBounds.points[3].y - StaticVariables.mainProject.videoBounds.points[0].y;
		
		double height1 = (int)((y + AXIS_SIZE - TOP_PADDING) - (y - AXIS_SIZE - TOP_PADDING));
		if(height1 > videoHeight)
		{
			height1 = videoHeight - (y - AXIS_SIZE - TOP_PADDING);
		}
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(getWidth(), (int) height1); 
		params.leftMargin = 0;
		params.topMargin = (int)y - AXIS_SIZE - TOP_PADDING;
		
		double height2 = getHeight();
		if(height2 > videoHeight)
		{
			height2 = videoHeight;
		}
		
		RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams((int) ((x + AXIS_SIZE) - (x - AXIS_SIZE)), (int) height2); 
		params1.leftMargin = (int)x - AXIS_SIZE;
		params1.topMargin = 0;
		
		
		double height3 = (int)((y + AXIS_SIZE - TOP_PADDING) - (y - AXIS_SIZE - TOP_PADDING));
		if(height3 > videoHeight)
		{
			height3 = videoHeight - (y - AXIS_SIZE - TOP_PADDING);
		}
		
		imageHA.setLayoutParams(params);
		imageVA.setLayoutParams(params1);
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
    			int bottom = videoView.getBottom();
    			
    			StaticVariables.mainProject.videoBounds = new Boundary(new Point[] { new Point(left, top), new Point(right, top), new Point(right, bottom), new Point(left, bottom) });
    			topPaddingUpdated = true;
    		}
    		
    		ImageView image = (ImageView) findViewById(R.id.image_view_scale);
            image.setVisibility(View.VISIBLE);
        	
            double height = StaticVariables.scaleImage.getHeight();//(getHeight() / 30 < MIN_SCALE_HEIGHT) ? MIN_SCALE_HEIGHT : getHeight() / 30;//StaticVariables.mainProject.videoBounds.points[3].y - StaticVariables.mainProject.videoBounds.points[0].y - (getHeight() / 10);
            double videoHeight = StaticVariables.mainProject.videoBounds.points[3].y - StaticVariables.mainProject.videoBounds.points[0].y;
            double left = (getWidth() * 0.25);
			double top = StaticVariables.mainProject.videoBounds.points[0].y + (videoHeight * 0.5) - (0.5 * height);
			double right = (getWidth() * 0.75);
			double bottom = StaticVariables.mainProject.videoBounds.points[3].y - (0.5 * videoHeight) + (0.5*height);
			
			StaticVariables.mainProject.scaleBoundary = new ScaleBoundary(new Point[] {
    									 	new Point(left, top + ((bottom - top) * 0.5)),
    									 	new Point(right, top + ((bottom - top) * 0.5))
    									 });   
			
			final TextView scale = ((TextView)(findViewById(R.id.scale_text_zone)));
			textBounds = new ClickBoundary(new Point[] { 
					new Point(scale.getLeft(), scale.getTop()),
					new Point(scale.getRight(), scale.getTop()), 
					new Point(scale.getRight(), scale.getBottom()),
					new Point(scale.getLeft(), scale.getBottom())					
				});
			    		    		
    		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)(right-left), (int)(bottom-top)); 
		    params.leftMargin = (int) left;
		    params.topMargin = (int) top;
		        		
    		image.setLayoutParams(params);
    		
        	EditText scaleText = (EditText)(findViewById(R.id.scale_text_zone));
        	scaleText.setText("100");
        	scaleText.setVisibility(View.VISIBLE);
		    RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT); 
		    params1.setMargins(0, 0, (int)(getWidth() * 0.4), getHeight() / 10);
        	scaleText.setLayoutParams(params1);
    		
        	isScaleSet = true;
    	}    	
    }
    
    /**
     * "Turns off" all of the following: settingScale, settingOrigin, plottingPoints functions. 
     * Basically prevents something like settingOrigin, plottingPoints at the same time.
     */
    public void resetOriginScalePointSetters()
    {
		isSettingOrigin = false;    
    	isSettingScaleRight = false;
		isSettingScaleLeft = false;
		plottingPoints = false;
		isRemovingPoints = false;
    }
    
    public void saveProject()
    {
    	StaticVariables.autosaveThread.interrupt();
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	boolean saveInternally = prefs.getBoolean("preference_default_save_option", false);
    	new FileUtils().save(this, saveInternally, StaticVariables.mainProject);
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
		if(StaticVariables.mainProject == null || StaticVariables.mainProject.scaleBoundary == null)
		{
			return;
		}	
		
		float x = e.getRawX();
		float y = e.getRawY() - TOP_PADDING;
		
		if(!StaticVariables.mainProject.videoBounds.contains(x, y))
		{
			return;
		}
	}
	
	public double getTime()
	{
		return (videoView.getCurrentPosition() * 0.001);
	}
			
	public void mouseOnSingleTap(MotionEvent e)
	{
		//There is padding of 75 px at the top (so 0, 75) is basically (0,0) of the useable area.
		//usable area is therefore 320x405
		float x = e.getRawX();
		float y = e.getRawY() - TOP_PADDING;
		
		if(!StaticVariables.mainProject.videoBounds.contains(x, y) || actionbarBoundary.contains(x, y) || textBounds.contains(x, y))
		{
			return;
		}
		
		if(isSettingOrigin)
		{
			displayOrigin(x, y + TOP_PADDING);
		}
		
		if(plottingPoints)
		{
			if(StaticVariables.mainProject.doesThisTimeExist(getTime()))
			{
				return;
			}
			
			final int SIZE = 4;
			ImageView image = new ImageView(this);
			image.setImageResource(R.drawable.point);
			image.setAdjustViewBounds(true);
			
			final Point point = new Point(x, y + TOP_PADDING, getTime(),image);
			point.imageView.setOnClickListener(new View.OnClickListener(){
	    	    public void onClick(View v) {
		    	    	if(isRemovingPoints)
		    	    	{
		    	    		StaticVariables.mainProject.removePointByValue(layout, point);
		    	    	}
	    	    	}
	    	    });
		
			StaticVariables.mainProject.registerPoint(point);
			
			
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(2 * SIZE, 2 * SIZE);
			params.leftMargin = (int) x - SIZE;
			params.topMargin = (int) y - SIZE;			
			layout.addView(image, params);	
			
			stepVideo(null);
		}
		if(isSettingScaleLeft)
		{
			
			ImageView image = (ImageView) findViewById(R.id.image_view_scale);
			Point rightPoint = StaticVariables.mainProject.scaleBoundary.points[1];
			//Point leftPoint = StaticVariables.mainProject.scaleBoundary.centerOfLeftLine();
			//Point centerPoint = StaticVariables.mainProject.scaleBoundary.getPerfectCenter();
		    Point leftPoint = new Point(x, y);
		    StaticVariables.mainProject.scaleBoundary.points[0] = leftPoint;
			
			double lineLength = lineLength(rightPoint.getX(), rightPoint.getY(), x, y);
						
			double angle = angleBetweenTwoVectors(rightPoint.x, rightPoint.y, getWidth(), rightPoint.y, leftPoint.x, leftPoint.y);
			
			angle*=-1;
			
			//double angle = angleBetweenTwoVectors(rightPoint.x, rightPoint.y, x, y, leftPoint.getX(), leftPoint.getY());
			StaticVariables.mainProject.scaleBoundary.totalRot = angle;	
			//StaticVariables.mainProject.scaleBoundary.updateLengthLeft(lineLength);
			//StaticVariables.mainProject.scaleBoundary.rotateOnRightCenter(angle);
			
			System.out.println("angle=" + angle);
			
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
			matrix.setRotate((float)(StaticVariables.mainProject.scaleBoundary.totalRot));//, (int)rightPoint.getX(), (int)rightPoint.getY());

			Bitmap resizedBitmap = getResizedBitmap(StaticVariables.scaleImage, (int)lineLength, StaticVariables.scaleImage.getHeight());
		    Bitmap img2 = Bitmap.createBitmap(resizedBitmap, 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight(), matrix, true);
		    image.setImageBitmap(img2);			 
		  			    
		    double hypotenuse = Math.sqrt(Math.pow(bottom - top, 2) + Math.pow(right-left, 2));
		    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)hypotenuse, (int)hypotenuse);
		    params.leftMargin = (int) (left);
		    params.topMargin = (int) top;			    
		    
		    image.setLayoutParams(params);
		    
		    //move the image to where it should be... not (0, 0)
		    //layout.removeView(image);
		   //layout.addView(image, params);			    
        	
		}
		if(isSettingScaleRight)
		{
			ImageView image = (ImageView) findViewById(R.id.image_view_scale);
			Point leftPoint = StaticVariables.mainProject.scaleBoundary.points[0];
			//Point leftPoint = StaticVariables.mainProject.scaleBoundary.centerOfLeftLine();
			//Point centerPoint = StaticVariables.mainProject.scaleBoundary.getPerfectCenter();
		    Point rightPoint = new Point(x, y);
		    StaticVariables.mainProject.scaleBoundary.points[1] = rightPoint;
			
			double lineLength = lineLength(leftPoint.getX(), leftPoint.getY(), x, y);

			double angle = angleBetweenTwoVectors(leftPoint.x, leftPoint.y, getWidth(), leftPoint.y, rightPoint.x, rightPoint.y);
			
			angle*=-1;

			StaticVariables.mainProject.scaleBoundary.totalRot = angle;	
			
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
			matrix.setRotate((float)(StaticVariables.mainProject.scaleBoundary.totalRot));//, (int)primaryPoint.getX(), (int)primaryPoint.getY());

			Bitmap resizedBitmap = getResizedBitmap(StaticVariables.scaleImage, (int)lineLength, StaticVariables.scaleImage.getHeight());
		    Bitmap img2 = Bitmap.createBitmap(resizedBitmap, 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight(), matrix, true);
		    image.setImageBitmap(img2);			 
		  			    
		    double hypotenuse = Math.sqrt(Math.pow(bottom - top, 2) + Math.pow(right-left, 2));
		    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)hypotenuse, (int)hypotenuse);
		    params.leftMargin = (int) (left);
		    params.topMargin = (int) top;			    
		    
		    image.setLayoutParams(params);
		    
		    
		}
		
	}
	
	public void mouseMove(MotionEvent e)
	{
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
		if(actionbarBoundary == null)
		{
	    	final LinearLayout linearLayout = (LinearLayout)(findViewById(R.id.action_bar_layout));
	    	
			actionbarBoundary = new Boundary(new Point[] {
					new Point(linearLayout.getLeft(), linearLayout.getTop()),
					new Point(linearLayout.getRight(), linearLayout.getTop()),
					new Point(linearLayout.getRight(), linearLayout.getBottom() ),
					new Point(linearLayout.getLeft(), linearLayout.getBottom())					
			});
		}
		
		if(!topPaddingUpdated)
		{
			TOP_PADDING = getHeight() - layout.getMeasuredHeight();
			
			int left = videoView.getLeft();
			int right = videoView.getRight();
			int top = videoView.getTop();
			int bottom = videoView.getBottom();
			
			
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
	   	}
			   	
	    return super.dispatchTouchEvent(event);
	}


}