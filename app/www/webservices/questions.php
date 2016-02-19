<?php
/*
WebService per scheda IntranetOperation :
crea xml con elenco delle domande a seconda dei criteri passati come parametro
*/
require 'config.php';

$query = "SELECT kb_questions.id,
                domanda,
                IFNULL( 0, n_positive_votes - n_negative_votes ) AS voti,
                CASE WHEN kb_answers.date IS NULL
                THEN kb_questions.date
                ELSE kb_answers.date
                END AS data, SUBSTRING( text, 1, 100 ) AS sinossi
                FROM  `kb_questions`
                LEFT JOIN kb_answers ON id_answer = kb_answers.id";

if( isset($_GET["category"]) ){
    // CATEGORIE SPECIALI:
    switch ($_GET["category"]) {
        case -1: // IN EVIDENZA: definita in categories.php come -1
            $where = "";
            break;
        default:
            $where = " inner join kb_categories on kb_questions.id = id_questions
                        where id_category_labels = ".$_GET["category"];
            break;
    }

}
$query = $query.$where;
$query = $query." order by data desc";
$result = mysqli_query($connection,$query);
// per ogni domanda presente
while ($row = $result->fetch_array(MYSQLI_ASSOC)) {
    // cerco a quali categorie appartiene tale domanda
    $q = "SELECT name,color FROM  kb_category_labels
                inner join kb_categories on kb_category_labels.id = id_category_labels
                inner join kb_questions on kb_questions .id = id_questions
                where kb_questions .id = ".$row[id];
    $r = mysqli_query($connection,$q);
    while ($add = $r->fetch_array(MYSQLI_ASSOC)) {
        $arrCategories[] = $add;
    }
    // ottengo un array ( di un solo elemento se possiede una sola categoria )
    // che aggiungo in coda ai dati della domanda
    array_push( $row , $arrCategories); //ATTENZIONE! non inserisco associativo, è [0]
    $array[] = $row;
    // e azzero, pronto quindi per il record successivo
    $arrCategories = null;
}
mysqli_close($connection);
// se esiste il record
if( $array[0]!=null ){
    $xml = new XMLWriter();
	$xml->openURI("php://output");
	$xml->startDocument('1.0','UTF-8');
	$xml->setIndent(false);
	$xml->startElement('questions');

    for($i=0; $i< count($array); $i++){
        addChildNode($xml, false, "question" );
            addChildNode($xml, true, "domanda", utf8_encode($array[$i]["domanda"]));
            addChildNode($xml, true, "voti", trim($array[$i]["voti"]));
            addChildNode($xml, true, "data", trim($array[$i]["data"]));
            addChildNode($xml, true, "sinossi", utf8_encode($array[$i]["sinossi"]));
            addChildNode($xml, false, "categories" );
            // per ogni categoria di quella domanda.
            // [0] non indica posizione 0, ma indica label associativa ( vedi sopra array_push )
            for($j=0; $j<count($array[$i][0]); $j++){
                addChildNode($xml, false, "category" );
                addChildNode($xml, true, "name", trim($array[$i][0][$j]["name"]));
                addChildNode($xml, true, "color", trim($array[$i][0][$j]["color"]));
                $xml->endElement();
            }
            $xml->endElement();
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

/*function formatDate($date){
    return date( "j F Y", strtotime($date) );
}*/
?>
