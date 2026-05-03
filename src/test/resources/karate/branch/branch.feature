Feature: Gestión de Sucursales

  Background:
    * url baseUrl

  # ──────────────────────────────────────────────────────────────────────────
  # POST /api/v1/franchises/{franchiseId}/branches
  # ──────────────────────────────────────────────────────────────────────────

  Scenario: TC-B01 Crear sucursal en franquicia existente
    * def franchise = call read('classpath:karate/franchise/helpers/create-franchise.feature') { name: 'Franquicia Branch #(java.util.UUID.randomUUID())' }
    Given path '/franchises/' + franchise.id + '/branches'
    And request { name: 'Sucursal Norte' }
    When method POST
    Then status 201
    And match response.id         == '#uuid'
    And match response.franchiseId == franchise.id
    And match response.name       == 'Sucursal Norte'

  Scenario: TC-B02 Crear sucursal en franquicia inexistente retorna 404
    Given path '/franchises/00000000-0000-0000-0000-000000000000/branches'
    And request { name: 'Sucursal Fantasma' }
    When method POST
    Then status 404

  # ──────────────────────────────────────────────────────────────────────────
  # GET /api/v1/franchises/{franchiseId}/branches
  # ──────────────────────────────────────────────────────────────────────────

  Scenario: TC-B03 Listar sucursales de una franquicia existente
    * def franchise = call read('classpath:karate/franchise/helpers/create-franchise.feature') { name: 'Franquicia List Branches #(java.util.UUID.randomUUID())' }
    * def args = { franchiseId: '#(franchise.id)', branchName: 'Sucursal Lista' }
    * call read('classpath:karate/branch/helpers/create-branch.feature') args
    Given path '/franchises/' + franchise.id + '/branches'
    When method GET
    Then status 200
    And match response == '#array'
    And match response[0].franchiseId == franchise.id

  # ──────────────────────────────────────────────────────────────────────────
  # GET /api/v1/franchises/{franchiseId}/branches/{branchId}
  # ──────────────────────────────────────────────────────────────────────────

  Scenario: TC-B04 Obtener sucursal por ID existente
    * def franchise = call read('classpath:karate/franchise/helpers/create-franchise.feature') { name: 'Franquicia GetBranch #(java.util.UUID.randomUUID())' }
    * def args = { franchiseId: '#(franchise.id)', branchName: 'Sucursal GetById' }
    * def branch = call read('classpath:karate/branch/helpers/create-branch.feature') args
    Given path '/franchises/' + franchise.id + '/branches/' + branch.id
    When method GET
    Then status 200
    And match response.id   == branch.id
    And match response.name == 'Sucursal GetById'

  Scenario: TC-B05 Obtener sucursal con ID inexistente retorna 404
    * def franchise = call read('classpath:karate/franchise/helpers/create-franchise.feature') { name: 'Franquicia Branch404 #(java.util.UUID.randomUUID())' }
    Given path '/franchises/' + franchise.id + '/branches/00000000-0000-0000-0000-000000000000'
    When method GET
    Then status 404

  # ──────────────────────────────────────────────────────────────────────────
  # PATCH /api/v1/franchises/{franchiseId}/branches/{branchId}/name
  # ──────────────────────────────────────────────────────────────────────────

  Scenario: TC-B06 Actualizar nombre de sucursal exitosamente
    * def franchise = call read('classpath:karate/franchise/helpers/create-franchise.feature') { name: 'Franquicia UpdBranch #(java.util.UUID.randomUUID())' }
    * def args = { franchiseId: '#(franchise.id)', branchName: 'Sucursal Original' }
    * def branch = call read('classpath:karate/branch/helpers/create-branch.feature') args
    Given path '/franchises/' + franchise.id + '/branches/' + branch.id + '/name'
    And request { name: 'Sucursal Actualizada' }
    When method PATCH
    Then status 200
    And match response.name == 'Sucursal Actualizada'

  Scenario: TC-B07 Actualizar nombre de sucursal inexistente retorna 404
    * def franchise = call read('classpath:karate/franchise/helpers/create-franchise.feature') { name: 'Franquicia UpdBranch404 #(java.util.UUID.randomUUID())' }
    Given path '/franchises/' + franchise.id + '/branches/00000000-0000-0000-0000-000000000000/name'
    And request { name: 'Nombre Fantasma' }
    When method PATCH
    Then status 404
