<?php


mysql_connect("mysql8.000webhost.com", "a9777586_natalie", "SvinKid12") or  die(mysql_error());
mysql_select_db("a9777586_kiddb");


$Movie= $_POST['movieLink'];

$pID= $_POST['ParentID'];

$AudioRec= $_POST['audioRecord'];

$MovieID= $_POST['movieID'];

$ViewsAmount= $_POST['viewsAmount'];

$AnswerStr = $_POST['answerStr'];

$sql_movies= mysql_query("UPDATE movies SET views_amount = '$ViewsAmount', audio_record = '$AudioRec', answer_str = '$AnswerStr' WHERE id = '$MovieID'"); 
//$sql_donations= mysql_query("Insert into movies(link, parent_id, audio_record, views_amount) VALUES('$Movie','$pID','$AudioRec',$ViewsAmount)");
 
mysql_close();


?>  