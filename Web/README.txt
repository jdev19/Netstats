Netstats
-----------

Config.php

Set the values in Config.php to match your MySQL database.
Import the included players.sql via your Database Editor of choice.
To include the table in another webpage,
make the specific page a .php file and type <?php include("PATH TO get.php"); ?>

Web Version 1.1
------------------------------

With the recent addition of player ip to the database, I added the ability to include player IP to the web table.  This is not usually recommended but it's up to you.

To include player IP in the web table, open the get.php file in your favorite text editor. I recommend Notepad++ for windows users :).
Two lines need to be uncommented (remove the // in front) to show Player IP in the table:
echo "<th>Player IP</th>"; (on line 19)
and
echo "<td>" . $ip . "</td>"; (on line 81)
save and refresh your page.
