import Button from "components/elements/forms/buttons/button";
import SectionHeader from "components/navs/section-header";
import {
  useGetCredentialsQuery,
  useDeleteCredentialMutation,
  UserCredentialMetadataRepresentation,
  CredentialRepresentation,
  CredentialMetadataRepresentation,
} from "store/apis/profile";
import { config } from "config";
import Table, {
  TableColumns,
  TableRows,
} from "components/elements/table/table";
import TimeUtil from "services/time-util";
import { keycloakService } from "keycloak";
import { t } from "i18next";
import { AIACommand } from "services/aia-command";
import P2Toast from "components/utils/toast";
import { Key, Lock, Smartphone } from "lucide-react";

const WEB_AUTH_N = "webauthn";
const WEB_AUTH_N_REGISTER = "webauthn-register";
const WEB_AUTH_N_PASSWORDLESS = "webauthn-passwordless";
const WEB_AUTH_N_PASSWORDLESS_REGISTER = "webauthn-register-passwordless";
const OTP = "otp";

const time = (time: string | undefined): string => {
  if (time === undefined) return "unknown";
  let t: number = Number(time);
  return TimeUtil.format(t);
};

const SigninProfile = () => {
  const { features: featureFlags, realm } = config.env;
  const { data: credentials = [], isLoading } = useGetCredentialsQuery({
    realm,
  });
  const [deleteCredential] = useDeleteCredentialMutation();

  const hasWebAuthN = credentials.find((cred) => cred.type === WEB_AUTH_N);
  const hasWebAuthNPasswordLess = credentials.find(
    (cred) => cred.type === WEB_AUTH_N_PASSWORDLESS
  );
  const hasOTP = credentials.find((cred) => cred.type === OTP);

  const removeCredential = (
    credential: CredentialMetadataRepresentation
  ): void => {
    if (credential === undefined) return;
    deleteCredential({
      realm,
      credentialId: credential.id!,
    })
      .then(() => {
        P2Toast({
          success: true,
          title: t(`credentialsSuccessfullyRemoved`, [
            credential.userLabel ?? "",
          ]),
        });
      })
      .catch((e) => {
        P2Toast({
          error: true,
          title: t(`credentialsRemovingError`, [credential.userLabel ?? ""]),
        });
        console.error(e);
      });
  };

  const setUpCredential = (action: string): void => {
    updateAIA(action);
  };

  const updateAIA = (action: string): void => {
    new AIACommand(keycloakService, action).execute();
  };

  const cols: TableColumns = [
    { key: "name", data: t("name") },
    { key: "created", data: t("created") },
    { key: "action", data: "", columnClasses: "flex justify-end" },
  ];

  const rowsForType = (
    credentialType: string,
    credentials: CredentialRepresentation[]
  ): TableRows => {
    const metadatas: UserCredentialMetadataRepresentation[] = [];
    credentials
      .filter((credential) => credential.type === credentialType)
      .forEach((credential) => {
        if (credential.userCredentialMetadatas) {
          metadatas.push(...credential.userCredentialMetadatas);
        }
      });
    return metadatas.map((metadata) => ({
      name: metadata.credential?.userLabel ?? credentialType,
      created: time(metadata.credential?.createdDate),
      action: (
        <>
          {featureFlags.passwordUpdateAllowed && (
            <Button
              isCompact
              className="inline-flex w-full justify-center sm:ml-3 sm:w-auto"
              onClick={() => {
                if (credentialType === "password") {
                  updateAIA("UPDATE_PASSWORD");
                } else {
                  removeCredential(metadata.credential!);
                }
              }}
            >
              {t("update")}
            </Button>
          )}
        </>
      ),
    }));
  };

  return (
    <div className="space-y-10">
      <div>
        <SectionHeader
          title={t("signingIn")}
          description={t("configureWaysToSignIn")}
        />
      </div>
      <div className="space-y-16">
        <div>
          <div className="space-y-5">
            <div className="flex items-center space-x-4">
              <div className="flex h-12 w-12 items-center justify-center rounded-lg border-2 border-primary-700 dark:text-zinc-200">
                <Lock className="h-5 w-5" />
              </div>
              <SectionHeader
                title={t("basicAuthentication")}
                variant="medium"
              />
            </div>
            <SectionHeader
              title={t("password")}
              description={t("signInByEnteringYourPassword")}
              variant="small"
            />
            <Table
              columns={cols}
              rows={rowsForType("password", credentials)}
              isLoading={isLoading}
            />
          </div>
        </div>
        {featureFlags.twoFactorUpdateAllowed && (
          <div>
            <div className="space-y-8">
              <div className="flex items-center space-x-4">
                <div className="flex h-12 w-12 items-center justify-center rounded-lg border-2 border-primary-700 dark:text-zinc-200">
                  <Smartphone className="h-5 w-5" />
                </div>
                <SectionHeader
                  title={t("twoFactorAuthentication")}
                  variant="medium"
                />
              </div>

              {isLoading && (
                <Table
                  columns={cols}
                  rows={rowsForType("password", credentials)}
                  isLoading={isLoading}
                />
              )}

              {hasOTP && (
                <>
                  <div className="items-center justify-between space-y-4 md:flex md:space-y-0">
                    <SectionHeader
                      title={t("authenticatorApplication")}
                      description={t(
                        "enterAVerificationCodeFromAuthenticatorApplication"
                      )}
                      variant="small"
                    />

                    {rowsForType(OTP, credentials).length !== 0 && (
                      <Button
                        isBlackButton
                        onClick={() => setUpCredential("CONFIGURE_TOTP")}
                      >
                        {t("setUpAuthenticator")}
                      </Button>
                    )}
                  </div>
                  <Table
                    columns={cols}
                    rows={rowsForType(OTP, credentials)}
                    isLoading={isLoading}
                    emptyState={
                      <div>
                        <Button
                          isBlackButton
                          onClick={() => setUpCredential("CONFIGURE_TOTP")}
                        >
                          {t("setUpAuthenticator")}
                        </Button>
                      </div>
                    }
                  />
                </>
              )}

              {hasWebAuthN && (
                <>
                  <div className="items-center justify-between space-y-4 md:flex md:space-y-0">
                    <SectionHeader
                      title={t("securityKey")}
                      description={t("useYourSecurityKeyToSignIn")}
                      variant="small"
                    />
                    {rowsForType("webauthn", credentials).length !== 0 && (
                      <Button
                        isBlackButton
                        onClick={() => setUpCredential(WEB_AUTH_N_REGISTER)}
                      >
                        {t("setUpSecurityKey")}
                      </Button>
                    )}
                  </div>
                  <Table
                    columns={cols}
                    rows={rowsForType("webauthn", credentials)}
                    isLoading={isLoading}
                    emptyState={
                      <div>
                        <Button
                          isBlackButton
                          onClick={() => setUpCredential(WEB_AUTH_N_REGISTER)}
                        >
                          {t("setUpSecurityKey")}
                        </Button>
                      </div>
                    }
                  />
                </>
              )}
            </div>
          </div>
        )}
        {featureFlags.passwordlessUpdateAllowed && hasWebAuthNPasswordLess && (
          <div>
            <div className="space-y-5">
              <div className="flex items-center space-x-4">
                <div className="flex h-12 w-12 items-center justify-center rounded-lg border-2 border-primary-700 dark:text-zinc-200">
                  <Key className="h-5 w-5" />
                </div>
                <SectionHeader
                  title={t("password-less-title")}
                  variant="medium"
                />
              </div>
              <div className="items-center justify-between space-y-4 md:flex md:space-y-0">
                <SectionHeader
                  title={t("securityKey")}
                  description={t("useYourSecurityKeyForPasswordlessSignIn")}
                  variant="small"
                />
                {rowsForType(WEB_AUTH_N_PASSWORDLESS, credentials).length !==
                  0 && (
                  <Button
                    isBlackButton
                    onClick={() =>
                      setUpCredential(WEB_AUTH_N_PASSWORDLESS_REGISTER)
                    }
                  >
                    {t("setUpSecurityKey")}
                  </Button>
                )}
              </div>
              <Table
                columns={cols}
                rows={rowsForType(WEB_AUTH_N_PASSWORDLESS, credentials)}
                isLoading={isLoading}
                emptyState={
                  <div>
                    <Button
                      isBlackButton
                      onClick={() =>
                        setUpCredential(WEB_AUTH_N_PASSWORDLESS_REGISTER)
                      }
                    >
                      {t("setUpSecurityKey")}
                    </Button>
                  </div>
                }
              />
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default SigninProfile;
