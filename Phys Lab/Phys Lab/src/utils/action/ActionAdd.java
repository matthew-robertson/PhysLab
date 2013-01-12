package utils.action;

public class ActionAdd extends Action
{
	private static final long serialVersionUID = 1L;

	public ActionAdd(String description) 
	{
		super(description);
	}
	
	public ActionAdd()
	{
		super();
		this.description = EnumAction.ACTION_ADD_POINT.actionValue;
	}
}
