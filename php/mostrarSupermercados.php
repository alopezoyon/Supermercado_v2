<?php
$servername = "db"; 
$username = "admin"; 
$password = "test"; 
$dbname = "database";

$conn = new mysqli($servername, $username, $password, $dbname);

//Verificar la conexión
if ($conn->connect_error) {
    die("Error de conexión: " . $conn->connect_error);
}

//Verificar inicio de sesión
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $parametros = json_decode(file_get_contents('php://input'), true);
    $username = $parametros['username'];

    $query = "SELECT * FROM supermercados WHERE username_ref='$username'";
    $result = mysqli_query($conn, $query); // Aquí cambié $con por $conn

    //Crear un array para almacenar los resultados
    $supermercados = array();

    //Verificar si hay resultados
    if ($result->num_rows > 0) {
        //Recorrer los resultados y agregarlos al array
        while ($row = $result->fetch_assoc()) {
            $supermercado = array(
                "id" => $row["id"],
                "nombre_super" => $row["nombre_super"],
                "localizacion" => $row["localizacion"]
            );
            array_push($supermercados, $supermercado);
        }
    } else {
        //Si no se encontraron supermercados para el usuario dado, agregar un mensaje al array
        array_push($supermercados, array("message" => "No se encontraron supermercados para el usuario proporcionado."));
    }

    //Convertir el array a formato JSON y enviarlo al cliente
    echo json_encode($supermercados);
} else {
    //Si la solicitud no es de tipo POST, devolver un mensaje de error
    echo json_encode(array("message" => "Solicitud incorrecta."));
}

//Cerrar la conexión
$conn->close();
?>
