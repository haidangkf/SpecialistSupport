<?php
/*
WebService per scheda IntranetOperation :
controlla se la matricola in fase di login è abilitata all'uso della
applicazione e ne restituisce il profilo locale.
*/

require 'config.php';
$query = "select * from kb_users where matricola ='".$_GET["m"]."'";
$result = mysqli_query($connection,$query);
$array[] = $result->fetch_array(MYSQLI_ASSOC); // record univoco
mysqli_close($connection);

// se esiste il record ( che è sempre unico, come la matricola )
if( $array[0]!=null ){
	$xml = new XMLWriter();
	$xml->openURI("php://output");
	$xml->startDocument('1.0','UTF-8');
	$xml->setIndent(false);
	$xml->startElement('user');

	addChildNode($xml,"matricola", trim($array[0]["matricola"]));
	addChildNode($xml,"struttura", trim($array[0]["struttura"]));
	addChildNode($xml,"citta", trim($array[0]["citta"]));
	addChildNode($xml,"indirizzo", trim($array[0]["indirizzo"]));
	addChildNode($xml,"profilo", trim($array[0]["profilo"]));

	$xml->endElement();
	header('Content-type: text/xml');
	$xml->flush();
}





function addChildNode($xml, $name, $value){
	$xml->startElement($name);
	$xml->writeRaw( $value );
	$xml->endElement();
}

?>
