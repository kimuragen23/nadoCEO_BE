import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: 'http://localhost:30808',
  realm: 'nadoceo',
  clientId: 'nadoceo-frontend',
});

export default keycloak;
