package utils.action;

import java.io.Serializable;
import java.util.List;

public class StackAction 	
		implements Serializable
{
	private static final long serialVersionUID = 1L;
	private Action[] _stack;
	private int first;
	private int last;
	private boolean firstAction;
	private int size;
	
	public StackAction(int size)
	{
		_stack = new Action[size];
		last = 0;
		first = 0;
		size = 0;
		firstAction = true;
	}
	
	/**
	 * @deprecated Untested and volatile
	 * @param size
	 * @param initialActions
	 */
	public StackAction(int size, List<Action> initialActions)
	{
		_stack = new Action[size];
		first = 0;
		last = initialActions.size();
		if(size < initialActions.size())
		{
			//Do not populate the ActionQueue because there isnt enough room.
			return;
		}
		
		for(int i = 0; i < initialActions.size(); i++)
		{
			_stack[i] = initialActions.get(i);
		}
	}
	
	/**
	 * Add to the end
	 * @param action
	 */
	public void push(Action action)
	{
		if(firstAction)
		{
			_stack[last] = action;
			if(last >= _stack.length)
			{
				last = 0;
			}
			if(first >= _stack.length)
			{
				first = 0;
			}	
			firstAction = false;
		}
		else
		{
			last++;
			if(last >= _stack.length)
			{
				last = 0;
			}
			
			_stack[last] = action;
			
			if(first == last)
			{
				first++;
			}
			if(first >= _stack.length)
			{
				first = 0;
			}	
		}
		size++;
	}
	
	/**
	 * Remove all elements
	 * @param index
	 */
	public void clear()
	{
		for(int i = 0; i < _stack.length; i++)
		{
			_stack[i] = null;
		}
		first = 0;
		last = 0;
		size = 0;
		firstAction = true;
	}
	
	/**
	 * Gets element at specified index.
	 * @param index
	 */
	public Action get(int index)
	{
		int i = index + first;
		while(i >= _stack.length)
		{
			i -= _stack.length;
		}
		
		return _stack[i];
	}
	
	public int size()
	{
		return size;
	}
	
	/**
	 * Gets the front element of the and removes it
	 */
	public Action pop()
	{
		/**
		 * 
		 * 
		 * 
		 * 
		 * This is what needs changed. Queue -> Stack. Basically, instead of taking from the front and advancing, take from the 
		 * back and go backwards. 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * if(size == 0)
		{
			return null;
		}
		
		Action action = _stack[first];
		_stack[first] = null;
		first++;
		if(first >= _stack.length)
		{
			first = 0;
		}
		size--;
		return action;
		 * 
		 */
		
		if(size == 0)
		{
			return null;
		}
		
		Action action = _stack[last];
		_stack[last] = null;
		last--;
		if(last < 0)
		{
			last = _stack.length - 1;
		}
		size--;
		return action;
	}
	
	/**
	 * Check the first element of the queue, but doesnt remove it.
	 */
	public Action peek()
	{
		return _stack[first];
	}
}
