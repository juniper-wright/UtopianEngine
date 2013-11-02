Utopian Engine
=============
A text adventure game engine, written in Java.

(At the time of this writing, the UtopianEngine is somewhere between version 1 and 2. It is very much full of compilation errors, because it needed to be completely rewritten.)

Version 2.0 of the Utopian Engine will feature:

* A fully-fledged built-in Scripting Language, named UtopiaScript. At this time, the UtopiaScript functions are:
	* Print - Print a string to the player's console
	* Println - Same as Print, but with a trailing endline
	* Pause - Pauses the game's operation until the player presses enter
	* Description - Prints out a predefined description for the room the user is in
	* RequireItem - Require that the player possess one or more of an item to continue
	* AddItem - Give the player one or more of an item
	* TakeItem - Take one or more of an item from the player
	* RoomState - Change the state of any room to one of a predefined list of states for that room
	* GoTo x y- Send the player to any room in the game
	* Go x/y - Send the player to x rooms horizontally, and y rooms vertically (can be negative)
	* Score - Modify the user's score
	* LoadGame - LOAD A NEW GAME. Basically, I put this in for episodic gaming. More on this later.
	* QuitGame - Ends the game. Used to denote winning or losing, or just quitting. The Utopian Engine does not discriminate.
	* Inventory - Prints the user's inventory. If the user inputs "inv" or "inventory" at any time, this happens, but game developers can add it to another event if they'd like.
* JAVASCRIPT! Yes, the Utopian Engine supports JavaScript, and makes the user's input available to JavaScript for dynamic event-handling. With a bit of finagling, it is possible to make an RPG.
* Two-dimensional array of Rooms, each of which can be completely independent of one another, or modify one another
* Each Room has a list of Roomstates - when certain actions happen, developers can script the rooms to change, subtly or otherwise. Basically everything changes about the room when its state changes.
