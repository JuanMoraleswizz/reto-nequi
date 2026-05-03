@ignore
Feature: Helper - Crear sucursal

  Scenario: crear sucursal en franquicia existente
    Given url baseUrl
    And path '/franchises/' + franchiseId + '/branches'
    And request { name: '#(branchName)' }
    When method POST
    Then status 201
    And def id         = response.id
    And def franchiseId = response.franchiseId
    And def name       = response.name
