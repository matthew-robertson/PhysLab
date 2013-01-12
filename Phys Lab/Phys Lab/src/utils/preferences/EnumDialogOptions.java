package utils.preferences;

public enum EnumDialogOptions 
{
	DIALOG_DELETE_POINTS_CONFIRM("DIALOG_DELETE_POINTS_CONFIRM"),
	DIALOG_DELETE_POINTS_DENY("DIALOG_DELETE_POINTS_DENY"),

	;
	
	EnumDialogOptions(String str)
	{
		this.value = str;
	}
	
	public String value;
}
