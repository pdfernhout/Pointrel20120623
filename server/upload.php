<?php 
// Derived from: http://php.about.com/od/advancedphp/ss/php_file_upload.htm
$targetDirectory = "resources/"; 
$target = $targetDirectory . basename( $_FILES['uploaded']['name']); 
$ok=1; 
 
// This is our size condition 
if ($uploaded_size > 350000) { 
  echo "Your file is too large.<br>"; 
  $ok=0; 
  } 
 
// This is our limit file type condition 
//if ($uploaded_type =="text/php") { 
//  echo "No PHP files<br>"; 
//  $ok=0; 
//} 
 
// Here we check that $ok was not set to 0 by an error 
if ($ok == 0) { 
   Echo "Sorry your file was not uploaded"; 
  } else {
    // If everything is ok we try to upload it 
    if (move_uploaded_file($_FILES['uploaded']['tmp_name'], $target)) {
      echo "The file " . basename( $_FILES['uploaded']['name']) . " has been uploaded"; 
    } else { 
      echo "Sorry, there was a problem uploading your file."; 
    } 
  } 
?> 
