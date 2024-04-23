<?php
$servername = "db";
$username = "admin";
$password = "test";
$database = "database";

$conn = new mysqli($servername, $username, $password, $database);

//Verificar la conexión
if ($conn->connect_error) {
    die("Error de conexión: " . $conn->connect_error);
}

//Array para almacenar los títulos de las imágenes
$response = array();

//Recibir los datos del formulario o petición
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $parametros = json_decode( file_get_contents( 'php://input' ), true );
    $nombre_supermercado = $parametros["nombre_supermercado"];
    $username_ref = $parametros["username"];

    //Consulta SQL para obtener los títulos de las imágenes
    $sql = "SELECT titulo FROM imagenes WHERE supermercado = '$nombre_supermercado' AND user_prop = '$username_ref'";
    $result = $conn->query($sql);

    //Verificar si hay resultados
    if ($result->num_rows > 0) {
        //Agregar los títulos al array de respuesta
        while($row = $result->fetch_assoc()) {
            $response[] = $row["titulo"];
        }
    } else {
        $response["message"] = "No se encontraron imágenes para ese usuario y supermercado.";
    }
}

//Cerrar la conexión
$conn->close();

//Devolver la respuesta en formato JSON
header('Content-Type: application/json');
echo json_encode($response);
?>
