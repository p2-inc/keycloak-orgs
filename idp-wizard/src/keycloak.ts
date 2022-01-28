import Keycloak from 'keycloak-js';

// Setup Keycloak instance as needed
const keycloak = Keycloak('./keycloak.json');

/*
keycloak.onTokenExpired = () => {
  console.log('token expired');
  keycloak.updateToken(30).then(() => {
    console.log('successfully get a new token');
  }).catch(() => {
    console.log('error getting a new token');
  });
}
*/

export default keycloak;

