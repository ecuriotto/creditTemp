(function () {
  try {
    return angular.module('bonitasoft.ui.widgets');
  } catch(e) {
    return angular.module('bonitasoft.ui.widgets', []);
  }
})().directive('customTimeline', function() {
    return {
      template: '    <ul class="timeline">\n        <li ng-class="$index % 2 === 0 ? \'\' : \'timeline-inverted\'" ng-repeat="event in properties.events track by $index">\n            <div class="timeline-badge"><i class="glyphicon glyphicon-check"></i></div>\n             <div class="timeline-panel">\n                  <div class="timeline-heading">\n                      <h4 class="timeline-title" ng-bind-html="event.title"></h4>\n                      <p><small class="text-muted"><i class="glyphicon glyphicon-user"></i> {{event.user}}</small> | <small class="text-muted"><i class="glyphicon glyphicon-time"></i> {{event.time}}</small></p>\n                  </div>\n                  <div class="timeline-body">\n                      <p ng-bind-html="event.body"></p>\n                  </div>\n              </div>\n        </li>\n    </ul>'
    };
  });
