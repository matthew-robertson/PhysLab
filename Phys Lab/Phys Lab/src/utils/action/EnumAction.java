package utils.action;

public enum EnumAction 
{
	ACTION_DELETE_POINT("ACTION_DELETE_POINT"),
	ACTION_ADD_POINT("ACTION_ADD_POINT"),
	ACTION_SET_ORIGIN("NYI"),
	ACTION_SET_SCALE("NYI"),
	ACTION_TOGGLE_PLOTTING("NYI"); //plottingPoints = !plottingPoints...
	
	EnumAction(String str)
	{
		this.actionValue = str;
	}
	
	public String actionValue;
}
