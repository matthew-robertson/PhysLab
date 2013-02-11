package global;

import java.io.File;

import junit.framework.Test;

import project.Project;
import utils.file.AutosaveThread;
import android.graphics.Bitmap;

public class StaticVariables 
{
	public volatile static Project mainProject;
	public static Bitmap scaleImage;	
	public static boolean firstLaunch = true;
	public static boolean clipChanged = false;
	public static File clip;
    public static AutosaveThread autosaveThread;
}