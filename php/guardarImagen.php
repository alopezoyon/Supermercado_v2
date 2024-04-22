<?php
$DB_SERVER = "db"; 
$DB_USER = "admin"; 
$DB_PASS = "test"; 
$DB_DATABASE = "database";

$con = mysqli_connect($DB_SERVER, $DB_USER, $DB_PASS, $DB_DATABASE);

if (mysqli_connect_errno()) {
    echo 'Error de conexión: ' . mysqli_connect_error();
    exit();
}

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $parametros = json_decode(file_get_contents('php://input'), true);
    $titulo = $parametros['title'];
    $user = $parametros['user'];
    $base = $parametros['imageData'];
    $supermercado = $parametros['supermercado'];
    $binary = base64_decode($base);

    $nombre_archivo = $titulo . '_' . $user . '_'. $supermercado . '.jpg';
    $ruta_archivo = 'uploads/' . $nombre_archivo;

    $file = fopen($ruta_archivo, 'w+');
    fwrite($file, $binary);
    fclose($file);

    //Preparar la sentencia SQL para insertar la imagen en la base de datos
    $sql = "INSERT INTO imagenes (foto, titulo, user_prop, supermercado) VALUES (?, ?, ?, ?)";
    $stmt = mysqli_prepare($con, $sql);

    //Verificar si la preparación de la sentencia SQL fue exitosa
    if ($stmt) {
        //Vincular los parámetros a la sentencia SQL
        mysqli_stmt_bind_param($stmt, "ssss", $binary, $titulo, $user, $supermercado);

        //Ejecutar la sentencia SQL
        mysqli_stmt_execute($stmt);

        //Verificar si ocurrió algún error durante la ejecución de la sentencia SQL
        if (mysqli_stmt_errno($stmt) != 0) {
            echo 'Error de sentencia: ' . mysqli_stmt_error($stmt);
        } else {
            echo 'Imagen guardada correctamente.';
        }
    } else {
        //Si la preparación de la sentencia SQL falló
        echo 'Error al preparar la sentencia SQL: ' . mysqli_error($con);
    }
}

//Cerrar la conexión
mysqli_close($con);
?>
