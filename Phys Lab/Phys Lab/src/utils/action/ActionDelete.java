package utils.action;

public class ActionDelete extends Action
{
	private static final long serialVersionUID = 1L;

	public ActionDelete(String description) 
	{
		super(description);
	}

	public ActionDelete()
	{
		super();
		this.description = EnumAction.ACTION_DELETE_POINT.actionValue;
	}
}
