angular.module('bonitasoft.ui.extensions',['ngSanitize'])
 .filter('labelized', [function () {
   return function toType(input) {
     return "<span class=\"label label-"+severity(input)+"\">"+input+"</span>";
   };
}]).filter('fromNow', [function (format) {
   return function fromNow(input) {
 	return moment(input,format).fromNow();
   };
}]);

function severity(status){
    switch(status) {
     case "Discretionary": return "primary";
     case "completed": return "success";
     case "error": return "danger";
     case "Completed": return "success";
     case "Required": return "danger";
     case "Optional": return "primary";
     case "PENDING": return "warning";
     case "RESOLVED": return "success";
     case "INVALID": return "danger";
     default:
       return "default";
   }
}
