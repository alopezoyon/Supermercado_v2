<?php
$DB_SERVER = "db"; 
$DB_USER = "admin"; 
$DB_PASS = "test";
$DB_DATABASE = "database";

//Se establece la conexión
$con = mysqli_connect($DB_SERVER, $DB_USER, $DB_PASS, $DB_DATABASE);

//Comprobamos conexión
if (mysqli_connect_errno()) {
    echo 'Error de conexion: ' . mysqli_connect_error();
    exit();
}

//Verificar si se ha recibido un título de imagen para eliminar
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    //Obtener el título de la imagen desde los parámetros de la solicitud
    $parametros = json_decode(file_get_contents('php://input'), true);
    $tituloImagen = $parametros['tituloImagen'];

    //Consulta SQL para eliminar la imagen con el título especificado
    $query = "DELETE FROM imagenes WHERE titulo = '$tituloImagen'";
    $result = mysqli_query($con, $query);

    if ($result) {
        //La imagen se eliminó correctamente
        echo "Imagen '$tituloImagen' eliminada correctamente.";
    } else {
        //Hubo un error al eliminar la imagen
        echo "Error al eliminar la imagen '$tituloImagen'.";
    }
} else {
    //Si no se recibió un título de imagen, mostrar un mensaje de error
    echo "Error: No se proporcionó un título de imagen para eliminar.";
}
?>
