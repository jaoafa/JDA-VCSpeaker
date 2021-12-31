<?php
echo getcwd() . "\n";
if (!isset($argv[1]) || !isset($argv[2])) {
    echo "Usage: php image-text.php <imageUrl> <outputPath>\n";
    exit(1);
}
$image_url = $argv[1];
$output_path = $argv[2];

$vision_api_results_dir = "vision-api/results/";

if (!file_exists($vision_api_results_dir)) {
    echo "vision-api-results directory does not exist.\n";
    exit(1);
}

$ext = pathinfo($image_url, PATHINFO_EXTENSION);
$img = file_get_contents($image_url);
$image = imagecreatefromstring($img);

$font = __DIR__ . "/mplus-1mn-bold.ttf";
$black = imagecolorallocate($image, 0, 0, 0);
$white = imagecolorallocate($image, 255, 255, 255);

$hash = md5($img);
echo "MD5: " . $hash . "\n";
if (!file_exists($vision_api_results_dir . $hash)) {
    echo "ERROR: NOTFOUND VISION API RESULTS FILE\n";
    exit(1);
}
$result = json_decode(file_get_contents($vision_api_results_dir . $hash), true);
$textAnnotations = $result["responses"][0]["textAnnotations"];

foreach (array_slice($textAnnotations, 1) as $textAnnotation) {
    $description = $textAnnotation["description"];
    $vertices = $textAnnotation["boundingPoly"]["vertices"];

    $points = [];
    $min = null;
    foreach ($vertices as $vertex) {
        $x = $vertex["x"];
        $y = $vertex["y"];

        $points[] = $x;
        $points[] = $y;

        if ($min == null || $x + $y < $min["x"] + $min["y"]) {
            $min = ["x" => $x, "y" => $y];
        }
    }

    $color = imagecolorallocatealpha($image, 0, 0, 0, 70);
    imagepolygon($image, $points, count($points) / 2, $color);

    imagettfstroketext($image, 10, 0, $min["x"] + 2, $min["y"] + 10, $white, $black, $font, $description, 1);
}

imagepng($image, $output_path);
imagedestroy($image);
echo "Output file: $output_path\n";

function imagettfstroketext(&$image, $size, $angle, $x, $y, &$textcolor, &$strokecolor, $fontfile, $text, $px)
{
    for ($c1 = ($x-abs($px)); $c1 <= ($x+abs($px)); $c1++) {
        for ($c2 = ($y-abs($px)); $c2 <= ($y+abs($px)); $c2++) {
            imagettftext($image, $size, $angle, $c1, $c2, $strokecolor, $fontfile, $text);
        }
    }
    return imagettftext($image, $size, $angle, $x, $y, $textcolor, $fontfile, $text);
}