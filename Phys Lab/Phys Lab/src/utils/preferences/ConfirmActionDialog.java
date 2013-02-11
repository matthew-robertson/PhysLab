package utils.preferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ConfirmActionDialog extends DialogFragment
{
	public interface ConfirmActionDialogListener {
        public void onActionConfirm(DialogFragment dialog, String message);
        public void onActionCancel(DialogFragment dialog, String message);
    }
	
	private String confirmMessage;
	private String cancelMessage;
	private String title;
    private String returnMessage;
	
    public ConfirmActionDialog()
    {
    	confirmMessage = "Confirm";
    	cancelMessage = "Cancel";
    	title = "";
    	returnMessage = "unset";
    }
    
    public ConfirmActionDialog(String con, String can, String t, String ret)
    {
    	confirmMessage = con;
    	cancelMessage = can;
    	title = t;
    	returnMessage = ret;
    }
    
    // Use this instance of the interface to deliver action events
  	ConfirmActionDialogListener mListener;
    
  	public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ConfirmActionDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
  	
  	public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(title)
               .setPositiveButton(confirmMessage, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Send the positive button event back to the host activity
                       mListener.onActionConfirm(ConfirmActionDialog.this, returnMessage);
                   }
               })
               .setNegativeButton(cancelMessage, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Send the negative button event back to the host activity
                       mListener.onActionCancel(ConfirmActionDialog.this, returnMessage);
                   }
               });
        return builder.create();
    }
}
