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
            setTimeout(timerComplete, 2500);
            User.save($scope.user,
                function (sentData, getHeaders) {
                    $scope.refresh();
                    $('#messageSuccess').show();

                },
                function (response){
                    $('#messageError').show();
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

        function timerComplete() {
            $('#messageSuccess').hide();
            $('#messageError').hide();
        }
    });
