<?php
include("config.php");

date_default_timezone_set(date_default_timezone_get());

mysql_connect($mysql_host,$mysql_user,$mysql_pass) or die (mysql_error());
mysql_select_db($mysql_db) or die (mysql_error());

$res = mysql_query("SELECT * from $mysql_table");

//Start Table
echo '<table border="1"><tr><th width="192">Name</th><th width="130">Last Login</th><th width="130">Last Seen</th><th width="80">Play Time</th><th width="50">Status</th></tr>';
while($row = mysql_fetch_array($res)){
	$name = $row['name'];
	$login = $row['enter'];
	$logout = $row['logout'];
	$total = $row['total'];
	$status = $row['status'];
    
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
echo "<tr><td>" . $name . "</td><td>" . $inDate . "</td><td>" . $longago . " ago</td><td>" . $playtime . "</td><td>" . $status2 . "</td></tr>";
}


echo "</table>";
?>