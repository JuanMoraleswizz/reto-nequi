@ignore
Feature: Helper - Crear producto

  Scenario: crear producto en sucursal existente
    Given url baseUrl
    And path '/franchises/' + franchiseId + '/branches/' + branchId + '/products'
    And request { name: '#(productName)', stock: '#(productStock)' }
    When method POST
    Then status 201
    And def id       = response.id
    And def branchId = response.branchId
    And def name     = response.name
    And def stock    = response.stock
