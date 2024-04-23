<?php
$DB_SERVER = "db"; 
$DB_USER = "admin"; 
$DB_PASS = "test"; 
$DB_DATABASE = "database";

$con = mysqli_connect($DB_SERVER, $DB_USER, $DB_PASS, $DB_DATABASE);

//Comprobamos conexión
if (mysqli_connect_errno()) {
    echo 'Error de conexion: ' . mysqli_connect_error();
    exit();
}

//Verificar registro de usuario
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    //Recibir datos del formulario de registro
    $parametros = json_decode(file_get_contents('php://input'), true);
    $name = $parametros['name'];
    $lastName = $parametros['lastName'];
    $email = $parametros['email'];
    $username = $parametros['username'];
    $password = $parametros['password'];

    //Comprobamos si el usuario ya existe
    $check_query = "SELECT * FROM users WHERE username='$username'";
    $check_result = mysqli_query($con, $check_query);

    if (mysqli_num_rows($check_result) > 0) {
        //El usuario ya existe, devuelve un mensaje de error
        echo "El nombre de usuario '$username' ya está en uso.";
    } else {
        //Insertar nuevo usuario en la base de datos
        $insert_query = "INSERT INTO users (name, lastName, email, username, password) VALUES ('$name', '$lastName', '$email', '$username', '$password')";
        if (mysqli_query($con, $insert_query)) {
            //Registro exitoso
            echo "Registro exitoso para el usuario '$username'.";
        } else {
            //Error al registrar
            echo "Error al registrar el usuario.";
        }
    }
}

?>
