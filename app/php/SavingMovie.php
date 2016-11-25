<?php


mysql_connect("mysql8.000webhost.com", "a9777586_natalie", "SvinKid12") or  die(mysql_error());
mysql_select_db("a9777586_kiddb");

$Movie= $_POST['movieLink'];


$pID= $_POST['ParentID'];

$AudioRec= $_POST['mFileName'];

$AnswerStr= $_POST['answerStr'];

$sql_donations= mysql_query("Insert into movies(link, parent_id, audio_record, views_amount, answer_str) VALUES('$Movie',$pID','$AudioRec',0,'$AnswerStr')");
 
mysql_close();


?>  