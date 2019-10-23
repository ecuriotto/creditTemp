angular.module('bonitasoft.ui.extensions',['ngSanitize'])
 .filter('wordCase', [function () {
   return function toWordCase(str) {
     return prettifyCamelCase(str);
   };
}]);

/**
 * Changes camel case to a human readable format. So helloWorld, hello-world and hello_world becomes "Hello World". 
 * */
function prettifyCamelCase(str) {
    var output = "";
    var len = str.length;
    var char;

    for (var i=0 ; i<len ; i++) {
        char = str.charAt(i);

        if (i === 0) {
            output += char.toUpperCase();
        }
        else if (char !== char.toLowerCase() && char === char.toUpperCase()) {
            output += " " + char;
        }
        else if (char == "-" || char == "_") {
            output += " ";
        }
        else {
            output += char;
        }
    }

    return output;
}