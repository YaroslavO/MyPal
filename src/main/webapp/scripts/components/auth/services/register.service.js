'use strict';

angular.module('mypalApp')
    .factory('Register', function ($resource) {
        return $resource('api/register', {}, {
        });
    });


