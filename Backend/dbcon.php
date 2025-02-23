<?php
$servername = "localhost";
$username = "root";        
$password = "";            
$dbname = "student_db";     


$conn = mysqli_connect($servername, $username, $password, $dbname);


if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

echo "Connected successfully!";
?>