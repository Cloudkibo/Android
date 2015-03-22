angular.module('phonertcdemo')

  .controller('CallCtrl', function ($scope, $state, $rootScope, $timeout, $ionicModal, $stateParams, signaling, ContactsService) {
    var duplicateMessages = [];

    $scope.callInProgress = false;

    $scope.isCalling = $stateParams.isCalling === 'true';
    $scope.contactName = $stateParams.contactName;

    $scope.allContacts = ContactsService.onlineUsers;
    $scope.contacts = {};
    $scope.hideFromContactList = [$scope.contactName];
    $scope.muted = false;

    $ionicModal.fromTemplateUrl('templates/select_contact.html', {
      scope: $scope,
      animation: 'slide-in-up'
    }).then(function(modal) {
      $scope.selectContactModal = modal;
    });
    
    
    $scope.sendMessage = function(message){
		message = {msg:message};
		message.room = $scope.room;
		message.to = $scope.peer;
		message.username = $scope.user.username;
		//console.log('Client sending message: ', message);
		signaling.emit('message', message);
    }
    
    signaling.on('youareonline', function(data){
			
		if($scope.lastMessage == 'AcceptCallFromOther'){
			
			$scope.sendMessage('got user media');
			
		}
		else if($scope.lastMessage == 'GotUserMedia'){
			
			call(false);
			
		}
		
	})
	
	$scope.user = {};
    
    $scope.user.username = KiboJava.getUsername();
	$scope.user._id = KiboJava.getId();
	$scope.peer = KiboJava.getPeer();
	$scope.lastMessage = KiboJava.getLastMessage();
	$scope.room = KiboJava.getRoom();
	
	signaling.emit('join global chatroom', {room: $scope.room, user: $scope.user});
    
    function call(isInitiator){
		//console.log(new Date().toString() + ': calling to ' + contactName + ', isInitiator: ' + isInitiator);

      var config = { 
        isInitiator: isInitiator,
        turn: {
          host: 'turn:ec2-54-68-238-149.us-west-2.compute.amazonaws.com:3478',
          username: 'test',
          password: 'test'
        },
        streams: {
          audio: true,
          video: true
        }
      };

      var session = new cordova.plugins.phonertc.Session(config);
      
      session.on('sendMessage', function (data) { 
		  
		  $scope.sendMessage(JSON.stringify(data));
		  
        /*signaling.emit('message', contactName, { 
          type: 'phonertc_handshake',
          data: JSON.stringify(data)
        });*/
      });

      session.on('answer', function () {
        console.log('Answered!');
      });

      session.on('disconnect', function () {
        if ($scope.contacts[contactName]) {
          delete $scope.contacts[contactName];
        }

        if (Object.keys($scope.contacts).length === 0) {
          signaling.emit('sendMessage', contactName, { type: 'ignore' });
          $state.go('app.contacts');
        }
      });

      session.call();
      
      $scope.contacts[$scope.peer] = session; 

	}

    
    $scope.answer = function () {
      if ($scope.callInProgress) { return; }

      $scope.callInProgress = true;
      $timeout($scope.updateVideoPosition, 1000);

      call(false, $stateParams.contactName);

      setTimeout(function () {
        console.log('sending answer');
        signaling.emit('sendMessage', $stateParams.contactName, { type: 'answer' });
      }, 1500);
    };

    $scope.updateVideoPosition = function () {
      $rootScope.$broadcast('videoView.updatePosition');
    };

    $scope.openSelectContactModal = function () {
      cordova.plugins.phonertc.hideVideoView();
      $scope.selectContactModal.show();
    };

    $scope.closeSelectContactModal = function () {
      cordova.plugins.phonertc.showVideoView();
      $scope.selectContactModal.hide();      
    };

    $scope.addContact = function (newContact) {
      $scope.hideFromContactList.push(newContact);
      signaling.emit('sendMessage', newContact, { type: 'call' });

      cordova.plugins.phonertc.showVideoView();
      $scope.selectContactModal.hide();
    };

    $scope.hideCurrentUsers = function () {
      return function (item) {
        return $scope.hideFromContactList.indexOf(item) === -1;
      };
    };

    $scope.toggleMute = function () {
      $scope.muted = !$scope.muted;

      Object.keys($scope.contacts).forEach(function (contact) {
        var session = $scope.contacts[contact];
        session.streams.audio = !$scope.muted;
        session.renegotiate();
      });
    };

    function onMessageReceive (name, message) {
		
		var message2 = {};
		message2.type = 'phonertc_handshake';
		message2.data = JSON.stringify(message);
		
		message = message2;

      switch (message.type) {
        case 'answer':
          $scope.$apply(function () {
            $scope.callInProgress = true;
            $timeout($scope.updateVideoPosition, 1000);
          });

          var existingContacts = Object.keys($scope.contacts);
          if (existingContacts.length !== 0) {
            signaling.emit('sendMessage', name, {
              type: 'add_to_group',
              contacts: existingContacts,
              isInitiator: false
            });
          }

          call(true, name);
          break;


        case 'phonertc_handshake':
          if (duplicateMessages.indexOf(message.data) === -1) {
            $scope.contacts[$scope.peer].receiveMessage(name);//(JSON.parse(message.data));
            duplicateMessages.push(message.data);
          }
          
          break;

      } 
    }

    signaling.on('message', onMessageReceive);

    $scope.$on('$destroy', function() { 
      signaling.removeListener('message', onMessageReceive);
    });
  });
