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

$res = mysql_query("SELECT * from $mysql_table ORDER BY `status` DESC, `name` ASC");

//Start Table
echo "<table border ='1'>";
echo "<tr><th width=192px>Name</th>";
echo "<th>Blocks Broken</th>";
echo "<th>Blocks Placed</th>";
echo "<th>Deaths</th>";
echo "<th>Last Login</th>";
echo "<th>Last Seen</th>";
echo "<th>Play Time</th>";
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
    $broken = $row['broken'];
    $placed = $row['placed'];
    $deaths = $row['deaths'];
    
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

$total = floor($total / 24);
$days = $total % 24;

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
$playtime = $days . "d " . $hours . "h " . $minutes . "m ";

//Start table data
echo "<tr>";
if ($status == 1) {
    echo "<td style = 'color:green'>" . $name . "</td>";
    } else {
    echo "<td style = 'color:red'>" . $name . "</td>";
    }
echo "<td>" . $broken . "</td>";
echo "<td>" . $placed . "</td>";
echo "<td>" . $deaths . "</td>";
echo "<td>" . $inDate . "</td>";
echo "<td>" . $longago . " ago</td>";
echo "<td>" . $playtime . "</td>";
/* Uncomment line below to include IP data in table */
//echo "<td>" . $ip . "</td>";
echo "</tr>";
}
echo "</table>";

?>