// Simple expressions

fscommand(a, 1);
fscommand(a, "1");
fscommand(a, b);
fscommand("a", b);
fscommand("a", "b");

// predefined commands

fscommand("quit");
fscommand("fullscreen", true);
fscommand("allowscale", true);
fscommand("showmenu", true);
fscommand("exec", "my_application");
fscommand("trapallkeys", true);

// Calling javascript functions

/* TODO Example Javascript function

    function playerid_DoFSCommand(command, args) 
    {
        if (command == "alert") 
        {
            alert(args);
        }
    }

	fscommand("alert", "This message sent from within Flash.");
*/

