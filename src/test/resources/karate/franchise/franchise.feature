Feature: Gestión de Franquicias

  Background:
    * url baseUrl

  # ──────────────────────────────────────────────────────────────────────────
  # POST /api/v1/franchises
  # ──────────────────────────────────────────────────────────────────────────

  Scenario: TC-F01 Crear franquicia exitosamente
    Given path '/franchises'
    And request { name: 'Franquicia Karate #(java.util.UUID.randomUUID())' }
    When method POST
    Then status 201
    And match response.id   == '#uuid'
    And match response.name == '#string'

  Scenario: TC-F02 Crear franquicia con nombre duplicado retorna 409
    * def uniqueName = 'Franquicia Duplicada ' + java.util.UUID.randomUUID()
    Given path '/franchises'
    And request { name: '#(uniqueName)' }
    When method POST
    Then status 201

    Given path '/franchises'
    And request { name: '#(uniqueName)' }
    When method POST
    Then status 409

  # ──────────────────────────────────────────────────────────────────────────
  # GET /api/v1/franchises
  # ──────────────────────────────────────────────────────────────────────────

  Scenario: TC-F03 Listar franquicias retorna un arreglo
    Given path '/franchises'
    When method GET
    Then status 200
    And match response == '#array'

  # ──────────────────────────────────────────────────────────────────────────
  # GET /api/v1/franchises/{id}
  # ──────────────────────────────────────────────────────────────────────────

  Scenario: TC-F04 Obtener franquicia por ID existente
    * def uniqueName = 'Franquicia GetById ' + java.util.UUID.randomUUID()
    * def created = call read('helpers/create-franchise.feature') { name: '#(uniqueName)' }
    Given path '/franchises/' + created.id
    When method GET
    Then status 200
    And match response.id   == created.id
    And match response.name == uniqueName

  Scenario: TC-F05 Obtener franquicia con ID inexistente retorna 404
    Given path '/franchises/00000000-0000-0000-0000-000000000000'
    When method GET
    Then status 404

  # ──────────────────────────────────────────────────────────────────────────
  # PATCH /api/v1/franchises/{id}/name
  # ──────────────────────────────────────────────────────────────────────────

  Scenario: TC-F06 Actualizar nombre de franquicia exitosamente
    * def originalName = 'Franquicia OriginalNombre ' + java.util.UUID.randomUUID()
    * def newName      = 'Franquicia NuevoNombre '    + java.util.UUID.randomUUID()
    * def created = call read('helpers/create-franchise.feature') { name: '#(originalName)' }
    Given path '/franchises/' + created.id + '/name'
    And request { name: '#(newName)' }
    When method PATCH
    Then status 200
    And match response.name == newName

  Scenario: TC-F07 Actualizar nombre a uno ya existente retorna 409
    * def nameA = 'Franquicia NombreA ' + java.util.UUID.randomUUID()
    * def nameB = 'Franquicia NombreB ' + java.util.UUID.randomUUID()
    * call read('helpers/create-franchise.feature') { name: '#(nameA)' }
    * def createdB = call read('helpers/create-franchise.feature') { name: '#(nameB)' }
    Given path '/franchises/' + createdB.id + '/name'
    And request { name: '#(nameA)' }
    When method PATCH
    Then status 409

  Scenario: TC-F08 Actualizar nombre de franquicia inexistente retorna 404
    Given path '/franchises/00000000-0000-0000-0000-000000000000/name'
    And request { name: 'Nombre Inexistente' }
    When method PATCH
    Then status 404

  # ──────────────────────────────────────────────────────────────────────────
  # GET /api/v1/franchises/{id}/top-stock
  # ──────────────────────────────────────────────────────────────────────────

  Scenario: TC-F09 Top stock de franquicia sin sucursales retorna arreglo vacío
    * def uniqueName = 'Franquicia TopStock ' + java.util.UUID.randomUUID()
    * def created = call read('helpers/create-franchise.feature') { name: '#(uniqueName)' }
    Given path '/franchises/' + created.id + '/top-stock'
    When method GET
    Then status 200
    And match response == []

  Scenario: TC-F10 Top stock de franquicia inexistente retorna 404
    Given path '/franchises/00000000-0000-0000-0000-000000000000/top-stock'
    When method GET
    Then status 404
