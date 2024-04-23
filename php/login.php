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


//Verificar inicio de sesión
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $parametros = json_decode( file_get_contents( 'php://input' ), true );
    $username = $parametros['username'];
    $password = $parametros['password'];

    $query = "SELECT * FROM users WHERE username='$username' AND password='$password'";
    $result = mysqli_query($con, $query);

    if (mysqli_num_rows($result) == 1) {
        //Las credenciales son correctas
        echo "Inicio de sesión exitoso. Bienvenido, $username!";
    } else {
        //Las credenciales son incorrectas, incluir el nombre de usuario y la contraseña en el mensaje
        echo "Inicio de sesión fallido. Verifica tus credenciales. Usuario: $username, Contraseña: $password";
    }
}

?>
