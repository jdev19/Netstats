<?php
/* ----------------------------------------------
File Created by Josh Devecka(Jdev19)
You may use the code as you wish,
but please give me some credit.
-------------------------------------------------*/

include("config.php");

date_default_timezone_set(date_default_timezone_get());

mysql_connect($mysql_host,$mysql_user,$mysql_pass) or die (mysql_error());
mysql_select_db($mysql_db) or die (mysql_error());

$res = mysql_query("SELECT * from $mysql_table");

//Start Table
echo "<table border ='1'>";
echo "<tr><th width=192px>Name</th>";
echo "<th width=130px>Last Login</th>";
echo "<th width=130px>Last Seen</th>";
echo "<th width=80px>Play Time</th>";
echo "<th width=50px>Status</th>";
/* Uncomment (remove //) to include Player IP in table, scroll down and uncomment below as well. */
//echo "<th>Player IP</th>";
echo "</tr>";

while($row = mysql_fetch_array($res)){
	$name = $row['name'];
	$login = $row['enter'];
	$logout = $row['logout'];
	$total = $row['total'];
	$status = $row['status'];
    $ip = $row['ip'];
    
//Gather milliseconds from timestamp and convert to human readable time
$inSeconds = $login / 1000;
$inDate = date("D M/d G:i", $inSeconds);

$outSeconds = $logout / 1000;
$outDate = date("D M/d G:i", $outSeconds);

//Convert Total Playtime into readable time.
$total = floor($total / 1000);
$seconds = $total % 60;

$total = floor($total / 60);
$minutes = $total % 60;

$total = floor($total / 60);
$hours = $total % 24;

//calculate how long ago player was on
$etime = time() * 1000;
$ago = ($etime - $logout);

$ago = floor($ago /1000);
$psec = $ago % 60;

$ago = floor($ago /60);
$pmin = $ago % 60;

$ago = floor($ago / 60);
$phr = $ago %24;

$ago = floor($ago / 24);
$pday = $ago %24;


$longago = $pday . "d " . $phr . "h " . $pmin . "m " . $psec . "s";

//put in readable format
$playtime = $hours . "h " . $minutes . "m " . $seconds . "s";

if($status == 1){
    $status2 = "Online";
    } else {
    $status2 = "Offline";
    }
echo "<tr>"
echo "<td>" . $name . "</td>";
echo "<td>" . $inDate . "</td>";
echo "<td>" . $longago . " ago</td>";
echo "<td>" . $playtime . "</td>";
echo "<td>" . $status2 . "</td>";
/* Uncomment line below to include IP data in table */
//echo "<td>" . $ip . "</td>";
echo "</tr>";
}


echo "</table>";
?>