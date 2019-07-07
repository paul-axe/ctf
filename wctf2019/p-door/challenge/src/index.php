<?php
//ini_set('error_reporting', ~E_ALL & ~E_NOTICE & ~E_STRICT & ~E_DEPRECATED); 

include "controllers.php";

$controller = new MainController();
$method = "do".$_GET["m"];

if (method_exists($controller, $method)){
    $controller->$method();
} else {
    $controller->doIndex();
}

