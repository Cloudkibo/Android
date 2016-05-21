angular.module('phonertcdemo')
  .factory('signaling', function (socketFactory) {
    var socket = io.connect('https://www.cloudkibo.com/');
    
    var socketFactory = socketFactory({
      ioSocket: socket
    });

    return socketFactory;
  });
