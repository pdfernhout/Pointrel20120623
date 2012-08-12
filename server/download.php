<?php

function startsWith($haystack, $needle) {
    $length = strlen($needle);
    return (substr($haystack, 0, $length) === $needle);
}

// header("Content-type: text/plain");

function curPageURL() {
  $pageURL = 'http';
  if ($_SERVER["HTTPS"] == "on") {$pageURL .= "s";}
  $pageURL .= "://";
  if ($_SERVER["SERVER_PORT"] != "80") {
    $pageURL .= $_SERVER["SERVER_NAME"].":".$_SERVER["SERVER_PORT"].$_SERVER["REQUEST_URI"];
   } else {
     $pageURL .= $_SERVER["SERVER_NAME"].$_SERVER["REQUEST_URI"];
   }
  return $pageURL;
}

$url = curPageURL(); // $_SERVER['SCRIPT_FILENAME'];
$arr = parse_url($url);
$parameters = $arr["query"];
parse_str($parameters, $data);
$resource = $data["resource"];
$start = $data["start"];
$end = $data["end"];
$user_id = $data["user_id"];

if (!startsWith($resource, "pointrel://sha256_")) {
  header("HTTP/1.0 400 Bad Request");
  print("Bad request");
} else {
  $file_name = "resources/" . urlencode(substr($resource, strlen("pointrel://")));
  // print("file: $file_name");
  if (!file_exists($file_name)) {
    header("HTTP/1.0 404 Not Found");
    print("Not Found");
  } else {
    // get everythign after the first dot
    $content_type = substr($resource, strpos($resource, '.') + 1);
    header("Content-type: $content_type");
    // print("File exists");
    // Send data
   readfile($file_name);
  } 
}