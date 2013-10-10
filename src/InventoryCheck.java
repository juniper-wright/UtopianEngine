// Author: Ian McDevitt
// Title: The Utopian Engine
// Purpose: Text Adventure Game Engine

// This class, as well as InventoryUpdate, need to be removed.
// They exist solely to assist with passing data to the Room's constructor, as the constructor cannot
// differentiate between two arrays of integers.
// However, the way around this is to use a single constructor for Room, and combine everything into
// a new class called Keycombo. This will be a major overhaul.

public class InventoryCheck
{
	private static int[] _items;
	private static int _index;

	public InventoryCheck()
	{
		_items = new int[] { 0, 0 };
		_index = 100;
	}

	public InventoryCheck(int[] items)
	{
		_items = items;
		_index = 0;
	}

	public int getNext()
	{
		if (_items.length > _index)
		{
			return _items[(_index++)];
		}
		return 0;
	}

	public boolean checkNext()
	{
		return _items.length > _index;
	}
}