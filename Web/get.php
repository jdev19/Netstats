<?php
/* ----------------------------------------------
File Created by Josh Devecka(Jdev19/amd3th)
You may use the code as you wish,
but please give me some credit.
-------------------------------------------------*/

include("config.php");

date_default_timezone_set(date_default_timezone_get());

mysql_connect($mysql_host,$mysql_user,$mysql_pass) or die (mysql_error());
mysql_select_db($mysql_db) or die (mysql_error());

$res = mysql_query("SELECT * from $mysql_table ORDER BY `logged` DESC, `player` ASC");

// Create table
echo '<table border="1">';
echo '<tr><th>Name</th>';
if ($trackBroken) {
	echo '<th>Blocks Broken</th>';
}
if ($trackPlaced) {
	echo '<th>Blocks Placed</th>';
}
if ($trackDeaths) {
	echo '<th>Deaths</th>';
}
echo '<th>Last Login</th>';
echo '<th>Last Seen</th>';
echo '<th>Play Time</th>';
if ($trackIP) {
	echo '<th>Player IP</th>';
}
echo '</tr>';

while ($row = mysql_fetch_array($res)) {
    $name = $row['player'];
    $login = $row['enter'];
    $seen = $row['seen'];
    $total = $row['total'];
    $logged = $row['logged'];
    $ip = $row['IP'];
    $broken = $row['broken'];
    $placed = $row['placed'];
    $deaths = $row['deaths'];
    
	// Total playtime
	$total = $total/1000;
	$sec = $total%60;
	$total = $total/60;
	$min = $total%60;
	$total = $total/60;
	$hrs = $total%24;
	$total = $total/24;
	$day = $total%24;
	$time = ($day) ? $day." days " : '';
	$time .= ($hrs) ? $hrs." hours " : '';
	$time .= ($min) ? $min." mins " : '';
	$time .= $sec." secs";

	// Time they've been gone
	$ago = ((time()*1000)-$seen)/1000;
	$asec = $ago%60;
	$ago = $ago/60;
	$amin = $ago%60;
	$ago = $ago/60;
	$ahrs = $ago%24;
	$ago = $ago/24;
	$aday = $ago%24;
	$atime = ($aday) ? $aday." days " : '';
	$atime .= ($ahrs) ? $ahrs." hours " : '';
	$atime .= ($amin) ? $amin." mins " : '';
	$atime .= ($asec) ? $asec." secs " : '';
	$atime .= " ago";
	$atime = ($playerData['online']) ? "Currently Online" : $atime;
	
	// Generate the rest of the table
    echo "<tr>";
    if ($logged == 1) {
    echo "<td style = 'color:green'>" . $name . "</td>";
    } else {
    echo "<td style = 'color:red'>" . $name . "</td>";
    }
    if ($trackBroken) {
        echo "<td>" . $broken . "</td>";
    }
    if ($trackPlaced) {
        echo "<td>" . $placed . "</td>";
    }
    if ($trackDeaths) {
        echo "<td>" . $deaths . "</td>";
    }
    echo "<td>" . $inDate . "</td>";
    echo "<td>" . $atime . "</td>";
    echo "<td>" . $time . "</td>";
    if ($trackIP) {
        echo "<td>".$ip."</td>";\
    }
    echo "</tr>";
?>