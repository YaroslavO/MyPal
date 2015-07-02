'use strict';

angular.module('mypalApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('userProfile', {
                parent: 'entity',
                url: '/userProfile',
                data: {
                    roles: ['ROLE_USER', 'ROLE_ADMIN'],
                    pageTitle: 'Profile'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/userProfile/userProfile.html',
                        controller: 'UserProfileController'
                    }
                },
                resolve: {}
            })
    });
