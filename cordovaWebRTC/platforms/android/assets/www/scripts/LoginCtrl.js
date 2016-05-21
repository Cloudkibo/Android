angular.module('phonertcdemo')

  .controller('LoginCtrl', function ($scope, $state, $ionicPopup, signaling, ContactsService) {
    $scope.data = {};
    $scope.loading = false;

    $scope.login = function () {
      $scope.loading = true;
      
      console.info(KiboJava.getUsername());
      console.info(KiboJava.getPeer());
      console.info(KiboJava.getLastMessage());
      console.info(KiboJava.getRoom());
      
      signaling.emit('login', $scope.data.name);
    };

    signaling.on('login_error', function (message) {
      $scope.loading = false;
      var alertPopup = $ionicPopup.alert({
        title: 'Error',
        template: message
      });
    });

    signaling.on('login_successful', function (users) {
      ContactsService.setOnlineUsers(users, $scope.data.name);
      $state.go('app.contacts');
    });
  });
