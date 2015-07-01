'use strict';

angular.module('mypalApp')
    .controller('UserController', function ($scope, User) {
        $scope.users = [];
        $scope.loadAll = function() {
            User.query(function(result) {
               $scope.users = result;
            });
        };
        $scope.loadAll();

        $scope.save = function () {
            if ($scope.user.id != null) {
                User.update($scope.user,
                    function () {
                        $scope.refresh();
                    });
            } else {
                User.save($scope.user,
                    function () {
                        $scope.refresh();
                    });
            }
            $scope.loadAll();
        };

        $scope.clear = function () {
            $scope.user = {title: null, id: null};
            $scope.editForm.$setPristine();
            $scope.editForm.$setUntouched();
        };
    });
