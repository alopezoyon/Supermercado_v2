<?php
//Cabecera con la clave del servidor y el tipo de contenido
$cabecera = array(
    'Authorization: key=AAAAMlY9bZE:APA91bGQ581aJsf-IR2WxxNfRMnYfbGq4saAnaK9uvyw1th44nWSMCh1aOuPe0p6Ed2JvTFEsPXVn_eCF9xY6TX6ZnBgBXpojGCQL6rjb5d98j_x-pGbimA3m7E3JHh2utJD3GL9U7kh',
    'Content-Type: application/json'
);

//Obtener el cuerpo JSON de la solicitud
$jsonData = file_get_contents('php://input');

//Decodificar el JSON para obtener los datos del mensaje
$datos = json_decode($jsonData, true);

//Obtener el token del cuerpo JSON
$token = $datos['to'];

//Datos del mensaje
$data = array(
    'to' => $token,
    'data' => array(
        'title' => $datos['mensaje'],
        'description' => 'Mensaje FCM'
    )
);

//Codificar los datos en formato JSON
$payload = json_encode($data);

//Inicializar cURL
$ch = curl_init();

//Configurar la URL de FCM
curl_setopt($ch, CURLOPT_URL, 'https://fcm.googleapis.com/fcm/send');

//Configurar las opciones de cURL
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, $cabecera);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, $payload);

//Ejecutar la solicitud cURL
$resultado = curl_exec($ch);

//Verificar si hubo algún error
if ($resultado === false) {
    echo 'Error al enviar el mensaje: ' . curl_error($ch);
} else {
    echo 'Mensaje enviado correctamente';
}

//Cerrar la conexión cURL
curl_close($ch);
?>
