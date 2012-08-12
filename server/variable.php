<?php

function startsWith($haystack, $needle) {
    $length = strlen($needle);
    return (substr($haystack, 0, $length) === $needle);
}

function endsWith($haystack, $needle) {
    $length = strlen($needle);
    return (substr($haystack, strlen($haystack) - $length, $length) === $needle);
}

header("Content-type: text/plain");

// http://twirlip.com/pointrel/variable.php?variable=baz&new_value=1&previous_value=&user_id=pdf&comment=foo
// http://twirlip.com/pointrel/variable.php?variable=baz&new_value=2&previous_value=1&user_id=pdf&comment=foo

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

function iso8601Timestamp($time=false) {
    if(!$time) $time=time();
    return date("Y-m-d", $time) . 'T' . date("H:i:s", $time) .'Z';
}

$url = curPageURL(); // $_SERVER['SCRIPT_FILENAME'];
//print($url);
//print('<p>');
$arr = parse_url($url);
//print_r($arr);
//print('<p>');
$parameters = $arr["query"];
//print_r($parameters);
//print('<p>');
parse_str($parameters, $data);
//print_r($data);
$variable = $data["variable"];
$new_value = $data["new_value"];
$previous_value = $data["previous_value"];
$user_id = $data["user_id"];
$comment = $data["comment"];

print("#variable: \"$variable\" new_value: \"$new_value\" user_id: \"$user_id\" previous_value: \"$previous_value\" comment: \"$comment\"\n");

if ($variable == "") {
    $dir = dir("variables");
	while (($fileName = $dir->read()) !== false) {
	    if (startsWith($fileName, "pv_") && endsWith($fileName, ".log")) {
	        $variableName = substr($fileName, 3, strlen($fileName) - 3 - 4);
			print("variable: $variableName\n");
		}
	}

	$dir->close();
	exit();
}

$file_name = "pv_" . $variable . ".log";

// Only allow certain characters in variable names for now
$file_name= "variables/" . preg_replace('/[^A-Za-z0-9_\.\-]/', '_', $file_name);

//print("filename: $file_name\n");
if (file_exists($file_name)) {
  //print("file exists\n");
  $file_name_escaped = escapeshellarg($file_name);
  $last_line = `tail -n 1 $file_name_escaped`;
} else {
  //print("log file does not exist\n");
  $last_line = "";
}
//print("last_line: " . '"' . $last_line . '"');
//print("\n");
if ($last_line == "") {
  $logValue = "";
} else {
  list($logTimestamp, $logUser, $logPrevious, $logValue, $logComment) = split(" ", $last_line, 5);
}

print("variable: $variable\n");
print("current_value: $logValue\n");

//print("logValue: \"$logValue\"");
//print("\n");
if ($new_value != "") {
  if ($logValue != $previous_value) {
    print("update_failed: previous value supplied did not match current value\n");
  } else {
    //print("have a new value\n"); 
    $timestamp = iso8601Timestamp();
    $new_line = "$timestamp $user_id $previous_value $new_value $comment\r\n";
    //print("new line: $new_line");
    $fh = fopen($file_name, 'a') or die("Can't open $file_name for writing: $php_errormsg");
    if (-1 == fwrite($fh, $new_line)) { die("Can't write to $file_name: $php_errormsg"); }
    fclose($fh) or die("Can't close $file_name: $php_errormsg");
    print("new_value: $new_value\n");
  }
}
