@ignore
Feature: Helper - Crear franquicia

  Scenario: crear franquicia con nombre único
    Given url baseUrl
    And path '/franchises'
    And request { name: '#(name)' }
    When method POST
    Then status 201
    And def id   = response.id
    And def name = response.name
