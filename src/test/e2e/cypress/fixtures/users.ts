type User = {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  email: string;
}

const user1: User = { username: "user-1", password: "user-1" };
const user2: User = { username: "user-2", password: "user-2" };
const user3: User = { username: "user-3", password: "user-3" };

const idpUser: User = { username: "test@phasetwo.io", password: "test123", firstName: "Ben", lastName: "Big", email: "test@phasetwo.io" };
const unverifiedIdpUser: User = { username: "unverified@phasetwo.io", password: "test123", firstName: "Ben", lastName: "Big", email: "unverified@phasetwo.io" };

export { User, user1, user2, user3, idpUser, unverifiedIdpUser }
