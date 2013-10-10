// Author: Ian McDevitt
// Title: The Utopian Engine
// Purpose: Text Adventure Game Engine

// This class represents a room, and contains all pertinent data. See variable declaration for explanation of variable usage.

public class Room
{
	boolean _seen = false;		// This determines whether or not the room has been seen before.
								// If the room has been seen before, the game will output a short description of the room
	int _roomstate = 0;			// roomstate is the state of the room. Rooms may have anywhere from 1 to 2147483647 roomstates.
								// Each roomstate basically functions as its own room, but only one can exist at a time
								// and they will all always occupy the same space in the two-dimensional grid of rooms
	String[] _description;		// This is a list of the long descriptions - one per roomstate
	String[] _shortDescription;	// This is a list of all the short descriptions - one per roomstate
	String[][] _keys;			// This is a list of lists of keys - each roomstate has its own list
								// Keys are used to compare user input against. The game will compare the user's
								// input against each key to determine if the user's input is valid
								// In a future version, there will be a spell-check feature that will allow up to two differences
								// between the user's input and the key
	String[][] _events;			// This is a list of lists of events - each roomstate has its own list
								// Parallel to the _keys array, the event is the text that is output when a key is matched
	int[][] _roomstateFactor;	// This is a list of lists of roomstate factors - each roomstate has its own list
								// Parallel to the _keys and array, this determines whether or not, and by how much,
								// the roomstate is changed when the corresponding key is entered
	InventoryCheck[][] _inventoryCheck;		// List of list of items required to activate a key
	InventoryUpdate[][] _inventoryUpdate;	// List of list of items given to player when parallel key is entered
	int[] _moveNorth;			// Determines what happens when the player attempts to move north. One variable per roomstate
	int[] _moveEast;			// Determines what happens when the player attempts to move east. One variable per roomstate
	int[] _moveSouth;			// Determines what happens when the player attempts to move south. One variable per roomstate
	int[] _moveWest;			// Determines what happens when the player attempts to move west. One variable per roomstate
	
	// Constructor 0
	public Room()		// Default constructor. As coded, this constructor will never be accessed.
	{
		this._description = new String[] { "Empty room.", "Empty room." };
		this._shortDescription = new String[] { "Empty room.", "Empty room." };
		this._keys = new String[][] { { "Empty room.", "Empty room." }, { "Empty room.", "Empty room." } };
		this._events = new String[][] { { "Empty room.", "Empty room." }, { "Empty room.", "Empty room." } };
		this._roomstateFactor = new int[][] { { 0, 0 }, { 0, 0 } };
		
		this._inventoryCheck = new InventoryCheck[this._events.length][];
		this._inventoryUpdate = new InventoryUpdate[this._events.length][];
		
		for (int i = 0; i < this._events.length; i++)
		{
			this._inventoryCheck[i] = new InventoryCheck[this._events[i].length];
			this._inventoryUpdate[i] = new InventoryUpdate[this._events[i].length];
			for (int j = 0; j < this._events[i].length; j++)
			{
				this._inventoryCheck[i][j] = new InventoryCheck();
				this._inventoryUpdate[i][j] = new InventoryUpdate();
			}
		}
		
		this._moveNorth = new int[this._description.length];
		this._moveEast = new int[this._description.length];
		this._moveSouth = new int[this._description.length];
		this._moveWest = new int[this._description.length];
		
		for (int i = 0; i < this._description.length; i++)
		{
			this._moveNorth[i] = 10200;
			this._moveEast[i] = 10200;
			this._moveSouth[i] = 10200;
			this._moveWest[i] = 10200;
		}
	}
	
	// Constructor 1: Euclidean geometry, no items required, no items given
	public Room(String[] description, String[] shortDescription, String[][] events, String[][] keys, int[][] roomstateFactor)
	{
		this._description = description;
		this._shortDescription = shortDescription;
		this._events = events;
		this._keys = keys;
		this._roomstateFactor = roomstateFactor;
		
		this._inventoryCheck = new InventoryCheck[this._events.length][];
		this._inventoryUpdate = new InventoryUpdate[this._events.length][];
		
		for (int i = 0; i < this._events.length; i++)
		{
			this._inventoryCheck[i] = new InventoryCheck[this._events[i].length];
			this._inventoryUpdate[i] = new InventoryUpdate[this._events[i].length];
			for (int j = 0; j < this._events[i].length; j++)
			{
				this._inventoryCheck[i][j] = new InventoryCheck();
				this._inventoryUpdate[i][j] = new InventoryUpdate();
			}
		}
		
		this._moveNorth = new int[this._description.length];
		this._moveEast = new int[this._description.length];
		this._moveSouth = new int[this._description.length];
		this._moveWest = new int[this._description.length];
		
		for (int i = 0; i < this._description.length; i++)
		{
			this._moveNorth[i] = 10200;
			this._moveEast[i] = 10200;
			this._moveSouth[i] = 10200;
			this._moveWest[i] = 10200;
		}
	}
	
	// Construmer 2: Euclidean geometry, items required, no items given
	public Room(String[] description, String[] shortDescription, String[][] events, String[][] keys, int[][] roomstateFactor, InventoryCheck[][] inventoryCheck)
	{
		this._description = description;
		this._shortDescription = shortDescription;
		this._events = events;
		this._keys = keys;
		this._roomstateFactor = roomstateFactor;
		this._inventoryCheck = inventoryCheck;
		
		this._inventoryUpdate = new InventoryUpdate[this._events.length][];
		
		for (int i = 0; i < this._events.length; i++)
		{
			this._inventoryUpdate[i] = new InventoryUpdate[this._events[i].length];
			for (int j = 0; j < this._events[i].length; j++)
			{
				this._inventoryUpdate[i][j] = new InventoryUpdate();
			}
		}
		
		this._moveNorth = new int[this._description.length];
		this._moveEast = new int[this._description.length];
		this._moveSouth = new int[this._description.length];
		this._moveWest = new int[this._description.length];
		
		for (int i = 0; i < this._description.length; i++)
		{
			this._moveNorth[i] = 10200;
			this._moveEast[i] = 10200;
			this._moveSouth[i] = 10200;
			this._moveWest[i] = 10200;
		}
	}
	
	// Constructor 3: Euclidean geometry, no items required, items given
	public Room(String[] description, String[] shortDescription, String[][] events, String[][] keys, int[][] roomstateFactor, InventoryUpdate[][] inventoryUpdate)
	{
		this._description = description;
		this._shortDescription = shortDescription;
		this._events = events;
		this._keys = keys;
		this._roomstateFactor = roomstateFactor;
		this._inventoryUpdate = inventoryUpdate;
		
		this._inventoryCheck = new InventoryCheck[this._events.length][];
		
		for (int i = 0; i < this._events.length; i++)
		{
			this._inventoryCheck[i] = new InventoryCheck[this._events[i].length];
			for (int j = 0; j < this._events[i].length; j++)
			{
				this._inventoryCheck[i][j] = new InventoryCheck();
			}
		}
		this._moveNorth = new int[this._description.length];
		this._moveEast = new int[this._description.length];
		this._moveSouth = new int[this._description.length];
		this._moveWest = new int[this._description.length];
		
		for (int i = 0; i < this._description.length; i++)
		{
			this._moveNorth[i] = 10200;
			this._moveEast[i] = 10200;
			this._moveSouth[i] = 10200;
			this._moveWest[i] = 10200;
		}
	}

	// Constructor 4: Euclidean geometry, items required, items given
	public Room(String[] description, String[] shortDescription, String[][] events, String[][] keys, int[][] roomstateFactor, InventoryCheck[][] inventoryCheck, InventoryUpdate[][] inventoryUpdate)
	{
		this._description = description;
		this._shortDescription = shortDescription;
		this._events = events;
		this._keys = keys;
		this._roomstateFactor = roomstateFactor;
		this._inventoryCheck = inventoryCheck;
		this._inventoryUpdate = inventoryUpdate;
		
		this._moveNorth = new int[this._description.length];
		this._moveEast = new int[this._description.length];
		this._moveSouth = new int[this._description.length];
		this._moveWest = new int[this._description.length];
		
		for (int i = 0; i < this._description.length; i++)
		{
			this._moveNorth[i] = 10200;
			this._moveEast[i] = 10200;
			this._moveSouth[i] = 10200;
			this._moveWest[i] = 10200;
		}
	}
	
	// Constructor 5: Non-euclidean geometry, no items required, no items given
	public Room(String[] description, String[] shortDescription, String[][] events, String[][] keys, int[][] roomstateFactor, int[] moveNorth, int[] moveEast, int[] moveSouth, int[] moveWest)
	{
		this._description = description;
		this._shortDescription = shortDescription;
		this._events = events;
		this._keys = keys;
		this._roomstateFactor = roomstateFactor;
		
		this._inventoryCheck = new InventoryCheck[this._events.length][];
		this._inventoryUpdate = new InventoryUpdate[this._events.length][];
		
		for (int i = 0; i < this._events.length; i++)
		{
			this._inventoryCheck[i] = new InventoryCheck[this._events[i].length];
			this._inventoryUpdate[i] = new InventoryUpdate[this._events[i].length];
			for (int j = 0; j < this._events[i].length; j++)
			{
				this._inventoryCheck[i][j] = new InventoryCheck();
				this._inventoryUpdate[i][j] = new InventoryUpdate();
			}
		}
		
		this._moveNorth = moveNorth;
		this._moveEast = moveEast;
		this._moveSouth = moveSouth;
		this._moveWest = moveWest;
	}
	
	// Constructor 6: Non-euclidean geometry, items required, no items given
	public Room(String[] description, String[] shortDescription, String[][] events, String[][] keys, int[][] roomstateFactor, InventoryCheck[][] inventoryCheck, int[] moveNorth, int[] moveEast, int[] moveSouth, int[] moveWest)
	{
		this._description = description;
		this._shortDescription = shortDescription;
		this._events = events;
		this._keys = keys;
		this._roomstateFactor = roomstateFactor;
		this._inventoryCheck = inventoryCheck;
		
		this._inventoryUpdate = new InventoryUpdate[this._events.length][];
		
		for (int i = 0; i < this._events.length; i++)
		{
			this._inventoryUpdate[i] = new InventoryUpdate[this._events[i].length];
			for (int j = 0; j < this._events[i].length; j++)
			{
				this._inventoryUpdate[i][j] = new InventoryUpdate();
			}
		}
		
		this._moveNorth = moveNorth;
		this._moveEast = moveEast;
		this._moveSouth = moveSouth;
		this._moveWest = moveWest;
	}
	
	// Constructor 7: Non-euclidean geometry, no items required, items given
	public Room(String[] description, String[] shortDescription, String[][] events, String[][] keys, int[][] roomstateFactor, InventoryUpdate[][] inventoryUpdate, int[] moveNorth, int[] moveEast, int[] moveSouth, int[] moveWest)
	{
		this._description = description;
		this._shortDescription = shortDescription;
		this._events = events;
		this._keys = keys;
		this._roomstateFactor = roomstateFactor;
		this._inventoryUpdate = inventoryUpdate;
		
		this._inventoryCheck = new InventoryCheck[this._events.length][];
		
		for (int i = 0; i < this._events.length; i++)
		{
			this._inventoryCheck[i] = new InventoryCheck[this._events[i].length];
			for (int j = 0; j < this._events[i].length; j++)
			{
				this._inventoryCheck[i][j] = new InventoryCheck();
			}
		}
		
		this._moveNorth = moveNorth;
		this._moveEast = moveEast;
		this._moveSouth = moveSouth;
		this._moveWest = moveWest;
	}
	
	// Constructor 8: Non-euclidean geometry, items required, items given	
	public Room(String[] description, String[] shortDescription, String[][] events, String[][] keys, int[][] roomstateFactor, InventoryCheck[][] inventoryCheck, InventoryUpdate[][] inventoryUpdate, int[] moveNorth, int[] moveEast, int[] moveSouth, int[] moveWest)
	{
		this._description = description;
		this._shortDescription = shortDescription;
		this._events = events;
		this._keys = keys;
		this._roomstateFactor = roomstateFactor;
		this._inventoryCheck = inventoryCheck;
		this._inventoryUpdate = inventoryUpdate;
		
		this._moveNorth = moveNorth;
		this._moveEast = moveEast;
		this._moveSouth = moveSouth;
		this._moveWest = moveWest;
	}
	
	// Gets the current roomstate of the current room
	public int getRoomstate()
	{
		return this._roomstate;
	}

	// Gets the index of the key, if available. Returns -1 if not found.
	// This function will eventually be given spell-check functionality.
	public int getKey(String in)
	{
		for (int i = 0; i < this._keys[this._roomstate].length; i++)
		{
			if (in.equals(this._keys[this._roomstate][i]))
			{
				return i;
			}
		}
		return -1;
	}
	
	// Gets event from the roomstate and the key index
	public String getEvent(int roomstate, int key)
	{
		return this._events[roomstate][key];
	}
	
	// Gets roomstatefactor from the roomstate and the key index
	public int getRoomstateFactor(int roomstate, int key)
	{
		return this._roomstateFactor[roomstate][key];
	}

	// Gets list of items needed from the roomstate and the key index
	public InventoryCheck getInventoryCheck(int roomstate, int key)
	{
		return this._inventoryCheck[roomstate][key];
	}
	
	// Gets list of items to be given to player from the roomstate and key index
	public InventoryUpdate getInventoryUpdate(int roomstate, int key)
	{
		return this._inventoryUpdate[roomstate][key];
	}
	
	// Returns the long description if it has not been seen, or the short descrpition otherwise
	public String description()
	{
		if (!this._seen)
		{
			this._seen = true;
			return this._description[this._roomstate];
		}
		
		return this._shortDescription[this._roomstate];
	}
	
	// Returns the long description; this function is used whenever the player inputs the "look" (or similar) command
	public String description(boolean check)
	{
		this._seen = true;
		return this._description[this._roomstate];
	}

	// Updates roomstate based on roomstatefactor
	public void changeRoomstate(int roomstatefactor)
	{
		this._roomstate += roomstatefactor;
	}
	
	// Sets roomstate to the new roomstate. As of yet, this function is unused
	public void setRoomstate(int newRoomstate)
	{
		this._roomstate = newRoomstate;
	}
	
	// Checks what the result of moving north would be
	public int checkNorth()
	{
		return this._moveNorth[this._roomstate];
	}
	
	// Checks what the result of moving east would be
	public int checkEast()
	{
		return this._moveEast[this._roomstate];
	}
	
	// Checks what the result of moving south would be
	public int checkSouth()
	{
		return this._moveSouth[this._roomstate];
	}
	
	// Checks what the result of moving west would be
	public int checkWest()
	{
		return this._moveWest[this._roomstate];
	}
}