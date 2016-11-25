<?php


mysql_connect("mysql8.000webhost.com", "a9777586_natalie", "SvinKid12") or  die(mysql_error());
mysql_select_db("a9777586_kiddb");



$sql_movies= mysql_query("SELECT id, link, parent_id, audio_record, views_amount, answer_str FROM movies WHERE link not like 'https%' AND id>2");
while($row =mysql_fetch_assoc($sql_movies))
{
	//$movies[]= $row['id'].",".$row['link'].",".$row['parent_id'].",".$row['audio_record'].",".$row['views_amount'];
	//$movies['movie_link']= $row['link'].",".$row['parent_id'];
	//$movies[]= $row['audio_record'];
	//$movies[]= $row['id'].",".$row['link'].",".$row['parent_id'].",".$row['audio_record'];
	$movies[]= $row['id'].",".$row['link'].",".$row['parent_id'].",".$row['views_amount'].",".$row['answer_str'];
	//print(json_encode($movies));
}

print(json_encode($movies));
mysql_close();

?>  