package utils.action;

import java.io.Serializable;

public class Action 
		implements Serializable
{
	private static final long serialVersionUID = 1L;
	protected String description; 
	protected Object associatedObject;
	
	public Action()
	{
		this.description = "";
		this.associatedObject = null;
	}
	
	public Action(String description)
	{
		this.description = description;
		this.associatedObject = null;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public Object getAssociatedObject()
	{
		return associatedObject;
	}
	
	public Action setAssociatedObject(Object obj)
	{
		this.associatedObject = obj;
		return this;
	}
}
