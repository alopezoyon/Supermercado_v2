<?php
$DB_SERVER = "db";
$DB_USER = "admin";
$DB_PASS = "test";
$DB_DATABASE = "database"; 

//Establecer la conexión
$con = mysqli_connect($DB_SERVER, $DB_USER, $DB_PASS, $DB_DATABASE);

//Comprobar conexión
if (mysqli_connect_errno()) {
    echo 'Error de conexión: ' . mysqli_connect_error();
    exit();
}


if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $parametros = json_decode( file_get_contents( 'php://input' ), true );
    $query = "SELECT foto FROM imagenes where titulo= 'IMG_20240412_075241_'";
    $result = mysqli_query($con, $query);

    if (!$result) {
        echo 'Ha ocurrido algún error: ' . mysqli_error($con);
        exit;
    } else {
        $photo = mysqli_fetch_array($result);
        echo base64_decode($photo['foto']);
    }
}

mysqli_close($con);
?>
