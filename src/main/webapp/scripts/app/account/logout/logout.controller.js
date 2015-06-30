'use strict';

angular.module('mypalApp')
    .controller('LogoutController', function (Auth) {
        Auth.logout();
    });
