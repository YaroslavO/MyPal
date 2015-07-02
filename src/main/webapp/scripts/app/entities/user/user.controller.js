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
            User.save($scope.user,
                function () {
                    $scope.refresh();
                });
        };

        $scope.refresh = function () {
            $scope.loadAll();
            $('#saveFacultyModal').modal('hide');
            $scope.clear();
        };

        $scope.clear = function () {
            $scope.user = {title: null, id: null};
            $scope.editForm.$setPristine();
            $scope.editForm.$setUntouched();
        };

        $('.modal').on('shown.bs.modal', function() {
            $(this).find('[autofocus]').focus();
        });
    });
