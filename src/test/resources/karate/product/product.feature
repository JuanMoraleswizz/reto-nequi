Feature: Gestión de Productos

  Background:
    * url baseUrl

  # ──────────────────────────────────────────────────────────────────────────
  # POST /api/v1/franchises/{fId}/branches/{bId}/products
  # ──────────────────────────────────────────────────────────────────────────

  Scenario: TC-P01 Agregar producto con stock válido
    * def franchise = call read('classpath:karate/franchise/helpers/create-franchise.feature') { name: 'Franquicia Prod #(java.util.UUID.randomUUID())' }
    * def bArgs = { franchiseId: '#(franchise.id)', branchName: 'Sucursal Prod' }
    * def branch = call read('classpath:karate/branch/helpers/create-branch.feature') bArgs
    Given path '/franchises/' + franchise.id + '/branches/' + branch.id + '/products'
    And request { name: 'Hamburguesa', stock: 100 }
    When method POST
    Then status 201
    And match response.id       == '#uuid'
    And match response.branchId == branch.id
    And match response.name     == 'Hamburguesa'
    And match response.stock    == 100

  Scenario: TC-P02 Agregar producto con stock negativo retorna 400
    * def franchise = call read('classpath:karate/franchise/helpers/create-franchise.feature') { name: 'Franquicia StockNeg #(java.util.UUID.randomUUID())' }
    * def bArgs = { franchiseId: '#(franchise.id)', branchName: 'Sucursal StockNeg' }
    * def branch = call read('classpath:karate/branch/helpers/create-branch.feature') bArgs
    Given path '/franchises/' + franchise.id + '/branches/' + branch.id + '/products'
    And request { name: 'Producto Malo', stock: -1 }
    When method POST
    Then status 400

  Scenario: TC-P03 Agregar producto con stock cero es válido
    * def franchise = call read('classpath:karate/franchise/helpers/create-franchise.feature') { name: 'Franquicia StockCero #(java.util.UUID.randomUUID())' }
    * def bArgs = { franchiseId: '#(franchise.id)', branchName: 'Sucursal StockCero' }
    * def branch = call read('classpath:karate/branch/helpers/create-branch.feature') bArgs
    Given path '/franchises/' + franchise.id + '/branches/' + branch.id + '/products'
    And request { name: 'Producto Agotado', stock: 0 }
    When method POST
    Then status 201
    And match response.stock == 0

  Scenario: TC-P04 Agregar producto en sucursal inexistente retorna 404
    * def franchise = call read('classpath:karate/franchise/helpers/create-franchise.feature') { name: 'Franquicia ProdBranch404 #(java.util.UUID.randomUUID())' }
    Given path '/franchises/' + franchise.id + '/branches/00000000-0000-0000-0000-000000000000/products'
    And request { name: 'Producto', stock: 10 }
    When method POST
    Then status 404

  # ──────────────────────────────────────────────────────────────────────────
  # DELETE /api/v1/franchises/{fId}/branches/{bId}/products/{pId}
  # ──────────────────────────────────────────────────────────────────────────

  Scenario: TC-P05 Eliminar producto existente retorna 204
    * def franchise = call read('classpath:karate/franchise/helpers/create-franchise.feature') { name: 'Franquicia DeleteProd #(java.util.UUID.randomUUID())' }
    * def bArgs = { franchiseId: '#(franchise.id)', branchName: 'Sucursal Delete' }
    * def branch = call read('classpath:karate/branch/helpers/create-branch.feature') bArgs
    * def pArgs = { franchiseId: '#(franchise.id)', branchId: '#(branch.id)', productName: 'Producto Borrar', productStock: 5 }
    * def product = call read('classpath:karate/product/helpers/create-product.feature') pArgs
    Given path '/franchises/' + franchise.id + '/branches/' + branch.id + '/products/' + product.id
    When method DELETE
    Then status 204

  Scenario: TC-P06 Eliminar producto inexistente retorna 404
    * def franchise = call read('classpath:karate/franchise/helpers/create-franchise.feature') { name: 'Franquicia DeleteProd404 #(java.util.UUID.randomUUID())' }
    * def bArgs = { franchiseId: '#(franchise.id)', branchName: 'Sucursal Delete404' }
    * def branch = call read('classpath:karate/branch/helpers/create-branch.feature') bArgs
    Given path '/franchises/' + franchise.id + '/branches/' + branch.id + '/products/00000000-0000-0000-0000-000000000000'
    When method DELETE
    Then status 404

  # ──────────────────────────────────────────────────────────────────────────
  # PATCH /api/v1/franchises/{fId}/branches/{bId}/products/{pId}/stock
  # ──────────────────────────────────────────────────────────────────────────

  Scenario: TC-P07 Actualizar stock con valor válido
    * def franchise = call read('classpath:karate/franchise/helpers/create-franchise.feature') { name: 'Franquicia UpdStock #(java.util.UUID.randomUUID())' }
    * def bArgs = { franchiseId: '#(franchise.id)', branchName: 'Sucursal UpdStock' }
    * def branch = call read('classpath:karate/branch/helpers/create-branch.feature') bArgs
    * def pArgs = { franchiseId: '#(franchise.id)', branchId: '#(branch.id)', productName: 'Producto Stock', productStock: 10 }
    * def product = call read('classpath:karate/product/helpers/create-product.feature') pArgs
    Given path '/franchises/' + franchise.id + '/branches/' + branch.id + '/products/' + product.id + '/stock'
    And request { stock: 99 }
    When method PATCH
    Then status 200
    And match response.stock == 99

  Scenario: TC-P08 Actualizar stock con valor negativo retorna 400
    * def franchise = call read('classpath:karate/franchise/helpers/create-franchise.feature') { name: 'Franquicia UpdStockNeg #(java.util.UUID.randomUUID())' }
    * def bArgs = { franchiseId: '#(franchise.id)', branchName: 'Sucursal UpdStockNeg' }
    * def branch = call read('classpath:karate/branch/helpers/create-branch.feature') bArgs
    * def pArgs = { franchiseId: '#(franchise.id)', branchId: '#(branch.id)', productName: 'Producto StockNeg', productStock: 10 }
    * def product = call read('classpath:karate/product/helpers/create-product.feature') pArgs
    Given path '/franchises/' + franchise.id + '/branches/' + branch.id + '/products/' + product.id + '/stock'
    And request { stock: -10 }
    When method PATCH
    Then status 400

  # ──────────────────────────────────────────────────────────────────────────
  # PATCH /api/v1/franchises/{fId}/branches/{bId}/products/{pId}/name
  # ──────────────────────────────────────────────────────────────────────────

  Scenario: TC-P09 Actualizar nombre de producto exitosamente
    * def franchise = call read('classpath:karate/franchise/helpers/create-franchise.feature') { name: 'Franquicia UpdProdName #(java.util.UUID.randomUUID())' }
    * def bArgs = { franchiseId: '#(franchise.id)', branchName: 'Sucursal UpdProdName' }
    * def branch = call read('classpath:karate/branch/helpers/create-branch.feature') bArgs
    * def pArgs = { franchiseId: '#(franchise.id)', branchId: '#(branch.id)', productName: 'Nombre Original', productStock: 20 }
    * def product = call read('classpath:karate/product/helpers/create-product.feature') pArgs
    Given path '/franchises/' + franchise.id + '/branches/' + branch.id + '/products/' + product.id + '/name'
    And request { name: 'Nombre Actualizado' }
    When method PATCH
    Then status 200
    And match response.name == 'Nombre Actualizado'

  # ──────────────────────────────────────────────────────────────────────────
  # GET top-stock con productos creados
  # ──────────────────────────────────────────────────────────────────────────

  Scenario: TC-P10 Top stock retorna el producto con mayor stock por sucursal
    * def franchise = call read('classpath:karate/franchise/helpers/create-franchise.feature') { name: 'Franquicia TopStockProd #(java.util.UUID.randomUUID())' }
    * def bArgs = { franchiseId: '#(franchise.id)', branchName: 'Sucursal TopStock' }
    * def branch = call read('classpath:karate/branch/helpers/create-branch.feature') bArgs
    * def p1Args = { franchiseId: '#(franchise.id)', branchId: '#(branch.id)', productName: 'Prod Low',  productStock: 5  }
    * def p2Args = { franchiseId: '#(franchise.id)', branchId: '#(branch.id)', productName: 'Prod High', productStock: 500 }
    * call read('classpath:karate/product/helpers/create-product.feature') p1Args
    * call read('classpath:karate/product/helpers/create-product.feature') p2Args
    Given path '/franchises/' + franchise.id + '/top-stock'
    When method GET
    Then status 200
    And match response == '#array'
    And match response[0].branchId   == '#uuid'
    And match response[0].branchName == '#string'
    And match response[0].productId  == '#uuid'
    And match response[0].stock      == 500
