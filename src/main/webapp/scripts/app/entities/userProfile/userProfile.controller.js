'use strict';

angular.module('mypalApp')
    .controller('UserProfileController', function ($scope, $http) {
        $scope.amount = "";

        $scope.loadUser = function() {
            $http.get('/api/account').success(
                function(response){
                    $scope.user = response;
                }
            );
        };
        $scope.loadUser();

        $scope.save = function () {
            //Here we need to sum user.balance + amount
            $scope.user.balance += +$scope.amount;
            $http.post('/api/account', $scope.user).success(
                function(){
                    $scope.refresh();
                }
            );
        };

        $scope.refresh = function () {
            $scope.loadAll();
            $('#addFundsModal').modal('hide');
            $scope.clear();
        };

        $scope.clear = function () {
            $scope.amount = "";
            $scope.editForm.$setPristine();
            $scope.editForm.$setUntouched();
        };

        $('.modal').on('shown.bs.modal', function() {
            $(this).find('[autofocus]').focus();
        });
    });
