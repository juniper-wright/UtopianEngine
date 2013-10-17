// Author: Ian McDevitt
// Title: The Utopian Engine
// Purpose: Text Adventure Game Engine

// This class represents a room, and contains all pertinent data. See variable declaration for explanation of variable usage.

public class Room
{
	boolean _can_travel = true;
	Roomstate[] _roomstates;
	int _roomstate = 0;			// roomstate is the state of the room. Rooms may have anywhere from 1 to 2147483647 roomstates.
								// Each roomstate basically functions as its own room, but only one can exist at a time
								// and they will all always occupy the same space in the two-dimensional grid of rooms

	// Constructor 0
	public Room()		// Default constructor. _can_travel keeps the player from entering the Room.
	{
		_can_travel = false;
	}
	
	// Constructor 1: Euclidean geometry, no items required, no items given
	public Room(Roomstate roomstates)
	{
		// TODO: Make the generateRoomstates function, and make it take an XML node
		// _roomstates = generateRoomstates();
	}
	
	
	// Gets the current roomstate of the current room
	public int getRoomstate()
	{
		return this._roomstate;
	}

	public void checkKeys(String in)
	{
		// Make this do things. This essentially will ask the Roomstate if it has a
	}
	
	// Returns the long description if it has not been seen, or the short description otherwise
	public String description()
	{
		return this._roomstates[this._roomstate].description();
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