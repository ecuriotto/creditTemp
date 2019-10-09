describe('ACM event handler is activated', () => {
  
  it('Should ACM event handler be enabled', () => {
    cy.visit('/apps/cases');
  
    cy.get('form').within(() => {
      cy.get('input[name="username"]').type('misa.kumagai');
      cy.get('input[name="password"]').type('bpm');
      cy.get('input[type="submit"]').click();
    })

    cy.request('/API/extension/handlerStatus')
        .its('body')
        .should('eqls', { "eventHandlerEnabled" : true })
  })

})