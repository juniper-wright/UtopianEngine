// Author: Ian McDevitt
// Title: The Utopian Engine
// Purpose: Text Adventure Game Engine

// This class represents a room, and contains all pertinent data. See variable declaration for explanation of variable usage.

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Room
{
	boolean _can_travel = true;
	Roomstate[] _roomstates;
	int _roomstate = 0;			// roomstate is the state of the room. Rooms may have anywhere from 1 to 2147483647 roomstates.
								// Each roomstate basically functions as its own room, but only one can exist at a time
								// and they will all always occupy the same space in the two-dimensional grid of rooms

	// Default constructor. _can_travel keeps the player from entering the Room.
	public Room()		
	{
		this._can_travel = false;
	}
	
	// Main constructor. Assumes that the roomstates have been built already.
	public Room(Element eRoom)
	{
		return;
	}
	
	
	// Gets the current roomstate of the current room
	public int getRoomstate()
	{
		return this._roomstate;
	}

	public void checkKeys(String in)
	{
		// Make this do things. This essentially will ask the Roomstate if it has the key.
	}
	
	// Returns the long description if it has not been seen, or the short description otherwise
	public String description()
	{
		return this._roomstates[this._roomstate].description();
	}
	
	// Returns the long description; this function is used whenever the player inputs the "look" (or similar) command
	public String description(boolean check)
	{
		return this._roomstates[this._roomstate].description(check);
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
	
	public boolean canTravel()
	{
		return this._can_travel;
	}
}