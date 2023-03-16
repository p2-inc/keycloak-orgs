import Button from "components/elements/forms/buttons/button";
import SectionHeader from "components/navs/section-header";
import {
  useGetCredentialsQuery,
  useDeleteCredentialMutation,
  useUpdateCredentialLabelMutation,
  UserCredentialMetadataRepresentation,
  CredentialRepresentation,
  CredentialMetadataRepresentation,
} from "store/apis/profile";
import { apiRealm } from "store/apis/helpers";
import Table, {
  TableColumns,
  TableRows,
} from "components/elements/table/table";
import TimeUtil from "services/time-util";
import { keycloakService } from "keycloak";
import { t } from "i18next";
import { AIACommand } from "services/aia-command";
import P2Toast from "components/utils/toast";

type CredContainer = {
  credential: CredentialRepresentation,
  metadata: CredentialMetadataRepresentation
};

const time = (time: string | undefined): string => {
  if (time === undefined) return "unknown";
  let t: number = Number(time);
  return TimeUtil.format(t);
};

const SigninProfile = () => {
  const { data: credentials = [], isLoading } = useGetCredentialsQuery({
    realm: apiRealm,
  });
  const [deleteCredential, { isSuccess }] = useDeleteCredentialMutation();

  const removeCredential = (credential: CredentialMetadataRepresentation): void => {
    if (credential === undefined) return;
    deleteCredential({
      realm: apiRealm,
      credentialId: credential.id!,
    }).then(() => {
      P2Toast({
        success: true,
        title: `${credential.userLabel ?? ""} removed.`,
      });
    }).catch((e) => {
      P2Toast({
        error: true,
        title: `Error removing ${credential.userLabel ?? ""} . Please try again.`,
      });
      console.error(e);
    });  };

  const setUpCredential = (action: string): void => {
    updateAIA(action);
  };

  const updateAIA = (action: string): void => {
    new AIACommand(keycloakService, action).execute();
  };

  const cols: TableColumns = [
    { key: "name", data: "Name" },
    { key: "created", data: "Created" },
    { key: "action", data: "", columnClasses: "flex justify-end" },
  ];

  const rowsForType = (credentialType: string, credentials: CredentialRepresentation[]): TableRows => {
    const metadatas: UserCredentialMetadataRepresentation[] = [];
    credentials
    .filter((credential) => credential.type === credentialType)
    .forEach((credential) => {
      if (credential.userCredentialMetadatas) {
        metadatas.push(...credential.userCredentialMetadatas);
      }
    });
    return metadatas
    .map((metadata) => ({
      name: metadata.credential?.userLabel ?? credentialType,
      created: time(metadata.credential?.createdDate),
      action: (
        <Button
          isBlackButton
          className="inline-flex w-full justify-center sm:ml-3 sm:w-auto"
          onClick={() => {
            if (credentialType === 'password') {
              updateAIA("UPDATE_PASSWORD");
            } else {
              removeCredential(metadata.credential!);
            }
          }
        }
        >
          Remove
        </Button>
      ),
    }));
  };
  
  return (
    <div className="space-y-12">
      <div>
        <SectionHeader
          title="Signing in"
          description="Configure ways to sign in."
        />
      </div>
      <div>
        <div className="space-y-5">
          <SectionHeader
            title={t("basic-authentication")}
            variant="medium"
          />
          <SectionHeader
            title="Password"
            description="Sign in by entering your password."
            variant="small"
          />
          <Table columns={cols} rows={rowsForType("password", credentials)} isLoading={isLoading} />
        </div>
      </div>
      <div>
        <div className="space-y-5">
          <SectionHeader
            title="Two-factor authentication"
            variant="medium"
          />
          <SectionHeader
            title="Authenticator application"
            description="Enter a verification code from authenticator application."
            variant="small"
          />
          <Button onClick={() => setUpCredential("CONFIGURE_TOTP")}>
            Set up authenticator application
          </Button>
          <Table columns={cols} rows={rowsForType("otp", credentials)} isLoading={isLoading} />
          <SectionHeader
            title="Security key"
            description="Use your security key to sign in."
            variant="small"
          />
          <Button onClick={() => setUpCredential("webauthn-register")}>
            Set up security key
          </Button>
          <Table columns={cols} rows={rowsForType("webauthn", credentials)} isLoading={isLoading} />
        </div>
      </div>
      <div>
        <div className="space-y-5">
          <SectionHeader
            title="Passwordless"
            variant="medium"
          />
          <SectionHeader
            title="Security key"
            description="Use your security key for passwordless sign in."
            variant="small"
          />
          <Button onClick={() => setUpCredential("webauthn-register-passwordless")}>
            Set up security key
          </Button>
          <Table columns={cols} rows={rowsForType("webauthn-passwordless", credentials)} isLoading={isLoading} />
        </div>
      </div>
    </div>
  );
};

export default SigninProfile;
