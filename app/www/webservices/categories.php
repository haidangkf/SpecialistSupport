<?php
/*
WebService per scheda IntranetOperation :
crea xml con categorie e informazioni collegate
*/
require 'config.php';
$diffDayForUpdate = 5;
$evidenzaName  = "In Evidenza";
$evidenzaDesc  = "Le domande/risposte migliori, cambi di processo e le ultime novità da Operation Management";
$evidenzaColor = "#00cccc";

$query = "select
            -1 as id,
            '".utf8_decode($evidenzaName)."' as name,
            true as inevidenza,
            false as aggiornamenti,
            '".utf8_decode($evidenzaDesc)."' as descrizione,
            '".trim($evidenzaColor)."' as color,
            0 as elements
            union
            select
            kb_category_labels.id as id,
            name,
            inevidenza,
            case when datediff( curdate(), date )<=".$diffDayForUpdate." then true else false end as aggiornamenti,
            descrizione,
            color,
            case when domanda is null then 0 else count(domanda) end as elements
            from kb_category_labels
            left join kb_categories on kb_category_labels.id = id_category_labels
            left join kb_questions on kb_questions.id = id_questions
            group by name
            order by inevidenza desc, elements desc, name asc";
$result = mysqli_query($connection,$query);
while ($row = $result->fetch_array(MYSQLI_ASSOC)) {
    $array[] = $row;
}
mysqli_close($connection);

// se esiste il record 1( 0 esiste sempre, è categoria "in evidenza" )
if( $array[1]!=null ){
    $xml = new XMLWriter();
	$xml->openURI("php://output");
	$xml->startDocument('1.0','UTF-8');
	$xml->setIndent(false);
	$xml->startElement('categories');

    for($i=0; $i< count($array); $i++){
        addChildNode($xml, false, "category" );
            addChildNode($xml, true, "id", trim($array[$i]["id"]));
            addChildNode($xml, true, "name", utf8_encode($array[$i]["name"]));
            addChildNode($xml, true, "inevidenza", $array[$i]["inevidenza"]==1 ? "true" : "false" );
            addChildNode($xml, true, "aggiornamenti", $array[$i]["aggiornamenti"]==1 ? "true" : "false");
            addChildNode($xml, true, "descrizione", utf8_encode($array[$i]["descrizione"]) );
            addChildNode($xml, true, "color", trim($array[$i]["color"]));
            addChildNode($xml, true, "elements", trim($array[$i]["elements"]));
        $xml->endElement();
    }
    $xml->endElement();
    header('Content-type: text/xml');
    $xml->flush();
}



function addChildNode($xml, $boolChiudi, $name, $value){
	$xml->startElement($name);
	$xml->writeRaw( $value );
    if($boolChiudi) $xml->endElement();
}
?>
