<?php
$DB_SERVER = "db"; 
$DB_USER = "admin";
$DB_PASS = "test";
$DB_DATABASE = "database";

$con = mysqli_connect($DB_SERVER, $DB_USER, $DB_PASS, $DB_DATABASE);

//Comprobar conexión
if (mysqli_connect_errno()) {
    echo 'Error de conexion: ' . mysqli_connect_error();
    exit();
}

//Verificar registro de supermercado
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    //Recibir datos del formulario de registro
    $parametros = json_decode(file_get_contents('php://input'), true);
    $nombreSupermercado = $parametros['nombre_super'];
    $localizacion = $parametros['localizacion'];
    $username_ref = $parametros['username_ref'];

    //Comprobar si el supermercado ya existe
    $check_query = "SELECT * FROM supermercados WHERE nombre_super='$nombreSupermercado'";
    $check_result = mysqli_query($con, $check_query);

    if (mysqli_num_rows($check_result) > 0) {
        //El supermercado ya existe, devuelve un mensaje de error
        echo "El supermercado '$nombreSupermercado' ya está registrado.";
    } else {
        //Insertar nuevo supermercado en la base de datos
        $insert_query = "INSERT INTO supermercados (nombre_super, localizacion, username_ref) VALUES ('$nombreSupermercado', '$localizacion', '$username_ref')";
        if (mysqli_query($con, $insert_query)) {
            //Registro exitoso
            echo "Supermercado '$nombreSupermercado' registrado exitosamente.";
        } else {
            //Error al registrar
            echo "Error al registrar el supermercado.";
        }
    }
}

?>
