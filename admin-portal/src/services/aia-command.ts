import { KeycloakService } from "./keycloak.service";

export class AIACommand {
  constructor(private keycloak: KeycloakService, private action: string) {}

  public execute(): void {
    this.keycloak.login({
      action: this.action,
    });
  }
}
