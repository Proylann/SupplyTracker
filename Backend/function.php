<?php

require '../Backend/dbcon.php';

function error422($message){
    $data = [
        'status' => 422,
        'message' => $message,
    ];
    header('HTTP/1.0 422 Unable to process entity');
    echo json_encode($data);
    exit();
}

function storeStudent($studentInput) {
    global $conn;

    // Validate Required Fields
    if (!isset($studentInput['FullName']) || !isset($studentInput['StudentNumber']) || 
        !isset($studentInput['Password']) || !isset($studentInput['Department'])) {
        error422('Missing required fields');
    }

    $fullname = mysqli_real_escape_string($conn, $studentInput['FullName']);
    $studentNumber = mysqli_real_escape_string($conn, $studentInput['StudentNumber']); // Ensure it's an integer
    $password = mysqli_real_escape_string($conn, $studentInput['Password']);
    $department = mysqli_real_escape_string($conn, $studentInput['Department']);

    // More Validation
    if(empty(trim($fullname))) error422('Enter your full name');
    if(empty(trim($studentNumber))) error422('Enter Student Number');
    if(empty(trim($password))) error422('Enter your password');
    if(empty(trim($department))) error422('Enter your department');

    // Insert Data
    $query = "INSERT INTO student_db.students (StudentNumber, Fullname, `Password`, Department) 
              VALUES ('$studentNumber', '$fullname', '$password', '$department')";

    if(mysqli_query($conn, $query)) {
        $data = ['status' => 201, 'message' => 'Student created successfully'];
        header('HTTP/1.0 201 Created');
        echo json_encode($data);
        exit();
    } else {
        error422('Database Error: ' . mysqli_error($conn)); // Debugging
    }
}



function getStudentList(){
    global $conn;
    $query = "SELECT * FROM student_db.student_tbl";
    $query_run = mysqli_query($conn, $query);

    if($query_run){
        if(mysqli_num_rows($query_run) > 0) {
            $res = mysqli_fetch_all($query_run, MYSQLI_ASSOC);
            $data = ['status' => 200, 'message' => 'Students list fetched successfully', 'data' => $res];
            header('HTTP/1.0 200 OK');
            return json_encode($data);
        } else {
            return error422('No students found');
        }
    } else {
        return error422('Server Error');
    }
}

function getStudent($studentParams){
    global $conn;
    if(empty($studentParams['StudentNumber'])) return error422('Enter Student Number');

    $studentNumber = mysqli_real_escape_string($conn, $studentParams['StudentNumber']);
    $query = "SELECT * FROM student_db.student_tbl WHERE StudentNumber='$studentNumber' LIMIT 1";
    $result = mysqli_query($conn, $query);

    if($result && mysqli_num_rows($result) == 1){
        $res = mysqli_fetch_assoc($result);
        $data = ['status' => 200, 'message' => 'Student fetched successfully', 'data' => $res];
        header('HTTP/1.0 200 OK');
        return json_encode($data);
    } else {
        return error422('Student not found');
    }
}

function updateStudent($studentInput, $studentParams){
    global $conn;
    if(empty($studentParams['StudentNumber'])) return error422('Enter Student Number');

    $studentNumber = mysqli_real_escape_string($conn, $studentParams['StudentNumber']);
    $fullname = mysqli_real_escape_string($conn, $studentInput['FullName']);
    $password = mysqli_real_escape_string($conn, $studentInput['Password']);
    $department = mysqli_real_escape_string($conn, $studentInput['Department']);

    if(empty(trim($fullname)) || empty(trim($password)) || empty(trim($department))) return error422('All fields are required');

    $query = "UPDATE student_db.student_tbl SET FullName='$fullname', Password='$password', Department='$department' WHERE StudentNumber='$studentNumber'";
    $result = mysqli_query($conn, $query);

    if($result){
        $data = ['status' => 200, 'message' => 'Student updated successfully'];
        header('HTTP/1.0 200 OK');
        return json_encode($data);
    } else {
        return error422('Server Error');
    }
}

function deleteStudent($studentParams){
    global $conn;
    if(empty($studentParams['StudentNumber'])) return error422('Enter Student Number');

    $studentNumber = mysqli_real_escape_string($conn, $studentParams['StudentNumber']);
    $query = "DELETE FROM student_db.student_tbl WHERE StudentNumber='$studentNumber' LIMIT 1";
    $result = mysqli_query($conn, $query);

    if($result){
        $data = ['status' => 200, 'message' => 'Student successfully deleted'];
        header('HTTP/1.0 200 OK');
        return json_encode($data);
    } else {
        return error422('Student not found');
    }
}
