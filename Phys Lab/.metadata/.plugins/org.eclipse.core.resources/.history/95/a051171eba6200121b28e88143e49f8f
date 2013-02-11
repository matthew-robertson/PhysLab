package utils.file;

import global.StaticVariables;
import android.content.Context;

public class AutosaveThread extends Thread
{
	private Context context;
	
	public AutosaveThread(Context context)
	{
		this.context = context;
	}
	
	public void run()
	{		
		while(true)
		{
			try {
				Thread.sleep(300000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
			
			if(StaticVariables.mainProject != null && StaticVariables.mainProject.autosaveEnabled)
			{
				new FileUtils().save(context, StaticVariables.mainProject.preferenceSaveInternally, StaticVariables.mainProject);
			}
		}		
	}
}
