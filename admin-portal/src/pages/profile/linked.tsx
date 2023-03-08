import Button from "components/elements/forms/buttons/button";
import SectionHeader from "components/navs/section-header";
import { apiRealm } from "store/apis/helpers";
import { keycloakService } from "keycloak";
import {
  useGetLinkedAccountsQuery,
  useDeleteLinkedProviderMutation,
  LinkedAccountRepresentation,
} from "store/apis/profile";

const LinkedProfile = () => {
  const { data: accounts = [] } = useGetLinkedAccountsQuery({
    realm: apiRealm,
  });
  const [deleteAccount, { isSuccess }] = useDeleteLinkedProviderMutation();

  const unLinkAccount = (account: LinkedAccountRepresentation): void => {
    deleteAccount({
      realm: apiRealm,
      providerId: account.providerName!,
    }).then(() => {
      //refresh accoutns? automatic?
      //ContentAlert.
    });
  };

  /*
  private linkAccount(account: LinkedAccount): void {
    const url = '/linked-accounts/' + account.providerName;
 
    const redirectUri: string = createRedirect(this.props.location.pathname);

    this.context!.doGet<{accountLinkUri: string}>(url, { params: {providerId: account.providerName, redirectUri}})
        .then((response: HttpResponse<{accountLinkUri: string}>) => {
            console.log({response});
            window.location.href = response.data!.accountLinkUri;
        });
  }
  */

  return (
    <div>
      <div className="mb-12">
        <SectionHeader
          title="Linked accounts"
          description="Manage logins through third-party accounts."
        />
      </div>
      <div className="w-full divide-y rounded border border-gray-200 bg-gray-50">
        {accounts.map((account: LinkedAccountRepresentation) => (
          <div className="flex items-center justify-between p-2">
            <div className="px-2">
              <span className="font-medium">{account.displayName}</span>
            </div>
            <div>
              <Button isBlackButton>Link account</Button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default LinkedProfile;
