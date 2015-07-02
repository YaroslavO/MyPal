'use strict';

angular.module('mypalApp')
    .controller('UserProfileController', function ($scope, $http) {
        $scope.amount = 0;

        $scope.loadUser = function() {
            $http.get('/api/account').success(
                function(response){
                    $scope.user = response;
                    $scope.user.balance = 100;
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
            $scope.amount = 0;
            $scope.editForm.$setPristine();
            $scope.editForm.$setUntouched();
        };
    });
