// Author: Ian McDevitt
// Title: The Utopian Engine
// Purpose: Text Adventure Game Engine

// This class represents a room, and contains all pertinent data. See variable declaration for explanation of variable usage.
//This class is part of the Utopian Engine, which is released by Ian McDevitt to the public under the BSD 3-clause license. See /LICENSE for more information

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Room
{
	private boolean _can_travel = true;
	private Roomstate[] _roomstates;
	private KeyCombo[] _roomkeys;		// Room-global keys! In case you want to do something regardless of the roomstate
	private int _roomstate = 0;			// roomstate is the state of the room. Rooms may have anywhere from 1 to 2147483647 roomstates.
										// Each roomstate basically functions as its own room, but only one can exist at a time
										// and they will all always occupy the same space in the two-dimensional grid of rooms

	// Default constructor. _can_travel keeps the player from entering the Room.
	public Room()		
	{
		this._can_travel = false;
		
		this._roomkeys = new KeyCombo[1];
		this._roomkeys[0] = new KeyCombo();
		
		this._roomstates = new Roomstate[1];
		this._roomstates[0] = new Roomstate();
	}
	
	// Main constructor.
	public Room(Node roomNode)
	{
		NodeList roomkeys = ((Element)roomNode).getElementsByTagName("roomkey");
		
		this._roomkeys = new KeyCombo[roomkeys.getLength()];
		
		for(int i = 0; i < roomkeys.getLength(); i++)
		{
			this._roomkeys[i] = new KeyCombo((Element)roomkeys.item(i));
		}
		
		NodeList roomstates = ((Element)roomNode).getElementsByTagName("roomstate");
		
		this._roomstates = new Roomstate[roomstates.getLength()];
		
		for(int i = 0;i < roomstates.getLength(); i++)
		{
			this._roomstates[i] = new Roomstate(roomstates.item(i));
		}
		
		this._can_travel = true;
	}
	
	// Gets the current roomstate of the current room
	public int getRoomstate()
	{
		return this._roomstate;
	}
	
	// Sets roomstate to the new roomstate. As of yet, this function is unused
	public void setRoomstate(int newRoomstate)
	{
		this._roomstate = newRoomstate;
	}

	public NodeList checkKeys(String key)
	{
		NodeList script = null;

		script = this._roomstates[this._roomstate].checkKeys(key);
		
		for(int i = 0; i < this._roomkeys.length && script == null; i++)
		{
			script = this._roomkeys[i].checkKey(key);
		}
		
		return script;
	}
	
	// Passthrough to the Roomstate class's description function.
	public String description(boolean longDesc)
	{
		return this._roomstates[this._roomstate].description(longDesc);
	}

	// Updates roomstate based on roomstatefactor
	public void changeRoomstate(int roomstatefactor)
	{
		this._roomstate += roomstatefactor;
	}
	
	public boolean canTravel()
	{
		return this._can_travel;
	}
}