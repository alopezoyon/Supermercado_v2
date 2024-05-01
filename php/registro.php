<?php
// Incluir la conexión a la base de datos
include('conexion.php');


//Verificar registro de usuario
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    //Recibir datos del formulario de registro
    $parametros = json_decode(file_get_contents('php://input'), true);
    $nombre = $parametros['nombre'];
    $apellidos = $parametros['apellidos'];
    $email = $parametros['email'];
    $usuario = $parametros['usuario'];
    $contrasena = $parametros['contrasena'];
    $fechaNacimiento = $parametros['fecha_nacimiento'];
    $telefono = $parametros['telefono'];

    //Comprobamos si el usuario ya existe
    $check_query = "SELECT * FROM usuarios WHERE usuario='$usuario'";
    $check_result = mysqli_query($con, $check_query);

    if (mysqli_num_rows($check_result) > 0) {
        $response['usuarioExiste'] = true;
        $response['exito'] = false;
    } else {
        $response['usuarioExiste'] = false;
        $hashed_password = hash('sha256', $contrasena);
        //Insertar nuevo usuario en la base de datos
        $insert_query = "INSERT INTO usuarios (USUARIO, PASSWORD, NOMBRE, APELLIDOS, FECHA_NACIMIENTO, NUMERO_TELEFONO, EMAIL)
                          VALUES ('$usuario', '$hashed_password', '$nombre', '$apellidos', '$fechaNacimiento', '$telefono', '$email')";
        if (mysqli_query($con, $insert_query)) {
            //Registro exitoso
            $response['exito'] = true;
        } else {
            //Error al registrar
            $response['exito'] = false;
        }
    }
}

// Devolver la respuesta en formato JSON
echo json_encode($response);

// Cerrar la conexión
mysqli_close($con);
?>
