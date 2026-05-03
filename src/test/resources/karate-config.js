function fn() {
  var port = karate.properties['server.port'] || '8080';
  var baseUrl = 'http://localhost:' + port + '/api/v1';
  karate.log('Karate baseUrl:', baseUrl);
  return { baseUrl: baseUrl };
}
