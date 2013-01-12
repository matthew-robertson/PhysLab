package utils.preferences;

import android.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class MessageBox extends DialogFragment
{
	private String message;
    private String title;
    
    public MessageBox()
    {
    	this.message = "Are you sure?";
    	this.title = "Are you sure?";
    }
    
    public MessageBox(String message)
    {
    	this.message = message;
    }
    
    public Dialog onCreateDialog(Bundle savedInstanceState) 
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(message)
           .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() 
           {
        	   public void onClick(DialogInterface arg0, int arg1) {
        	   }
           });
       	return builder.create();
    }
}