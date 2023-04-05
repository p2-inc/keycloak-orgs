import SectionHeader from "components/navs/section-header";
import cs from "classnames";
import Button from "components/elements/forms/buttons/button";
import {
  useAddOrganizationInvitationMutation,
  useGetOrganizationByIdQuery,
} from "store/apis/orgs";
import { useState } from "react";
import RHFFormTextInputWithLabel from "components/elements/forms/inputs/rhf-text-input-with-label";
import { useForm } from "react-hook-form";
import { config } from "config";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useKeycloak } from "@react-keycloak/web";
import P2Toast from "components/utils/toast";
import { Listbox } from "@headlessui/react";
import RoleBadge from "components/elements/badges/role-badge";
import { User, ChevronDown } from "lucide-react";
import { Roles, viewRoles } from "services/role";
import useUser from "components/utils/useUser";
import Alert from "components/elements/alerts/alert";

const { realm } = config.env;

const loadingIcon = (
  <div>
    <div className={cs("relative h-12 w-12 overflow-hidden rounded-md")}>
      <div className="absolute -inset-10 z-10 bg-gradient-to-tr from-[#C7DFF0] to-[#1476B7]"></div>
      <div className="absolute inset-[2px] z-20 flex items-center justify-center rounded bg-white dark:bg-p2dark-1000 dark:text-zinc-200">
        <User />
      </div>
    </div>
  </div>
);

const roles = [
  { id: 1, name: "Admin", items: Roles },
  { id: 2, name: "Member", items: viewRoles },
];

const NewInvitation = () => {
  const { keycloak } = useKeycloak();
  const navigate = useNavigate();
  let { orgId } = useParams();

  const { hasManageInvitationsRole: hasManageInvitationsRoleCheck } = useUser();
  const { data: org } = useGetOrganizationByIdQuery({
    orgId: orgId!,
    realm: config.env.realm,
  });

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm();
  const [addOrganizationInvitation] = useAddOrganizationInvitationMutation();
  const [selectedRole, setSelectedRole] = useState(roles[0]);

  const onSubmit = async (data) => {
    if (selectedRole && data.email) {
      const roleItems = roles.find((r) => selectedRole.name === r.name);
      await addOrganizationInvitation({
        orgId: orgId!,
        realm,
        invitationRequestRepresentation: {
          email: data.email,
          inviterId: keycloak.tokenParsed?.sub,
          roles: Object.values(roleItems!.items),
        },
      })
        .unwrap()
        .then(() => {
          P2Toast({
            success: true,
            title: `${data.email} has been sent an invitation.`,
          });
          reset();
          return navigate(`/organizations/${orgId}/details`);
        })
        .catch((e) => {
          return P2Toast({
            error: true,
            title:
              e.status === 401
                ? "Insufficient roles to perform action."
                : e.data.error,
          });
        });
    }
  };

  const hasManageInvitationsRole = hasManageInvitationsRoleCheck(orgId!);

  const isSendButtonDisabled = !hasManageInvitationsRole || !selectedRole;

  return (
    <div className="mt-4 md:mt-16">
      <SectionHeader
        title={`Invite new member to ${org?.displayName || "Organization"}`}
        description="Add a new member to the organization by entering their email and assigning them a role within the organization. An email will be sent to them with instructions on how to join."
        icon={loadingIcon}
        rightContent={
          <Link
            to={`/organizations/${orgId}/details`}
            className="inline-block rounded-lg px-4 py-2 font-medium opacity-60 transition hover:bg-gray-100 hover:opacity-100 dark:text-zinc-200 dark:hover:bg-p2dark-1000"
          >
            Cancel
          </Link>
        }
      />
      {!hasManageInvitationsRole && (
        <div className="mt-4">
          <Alert
            title='You lack the "manage-invitations" role.'
            body="Speak to an admin in order to be granted this role."
            type="info"
          />
        </div>
      )}
      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="mt-8 space-y-4">
          <Listbox
            value={selectedRole}
            onChange={setSelectedRole}
            disabled={!hasManageInvitationsRole}
          >
            <div className="relative z-50">
              <Listbox.Button className="flex w-full items-center justify-between space-x-3 rounded border border-neutral-300 bg-neutral-50 py-2 px-4 text-left hover:border-p2blue-700 hover:bg-white disabled:opacity-50 dark:border-zinc-600 dark:bg-p2dark-1000 dark:text-zinc-200 dark:hover:bg-p2dark-1000">
                <div>{selectedRole.name}</div>
                <ChevronDown className="dark:text-zinc-600" />
              </Listbox.Button>
              <Listbox.Options className="absolute w-full">
                <div className="pb-10">
                  <div className="relative bottom-0 z-30 max-h-96 divide-y overflow-auto rounded border border-neutral-300 bg-white dark:divide-zinc-600 dark:border-zinc-600 dark:bg-p2dark-900">
                    {roles.map((role) => (
                      <Listbox.Option
                        key={role.id}
                        value={role}
                        className="cursor-pointer space-y-2 p-4 hover:bg-neutral-50 dark:hover:bg-p2dark-1000"
                      >
                        <div className="font-semibold dark:text-zinc-200">
                          {role.name}
                        </div>
                        <div className="flex flex-wrap">
                          {Object.values(role.items)
                            .sort()
                            .map((ar) => (
                              <RoleBadge name={ar} key={ar} />
                            ))}
                        </div>
                      </Listbox.Option>
                    ))}
                  </div>
                  <div
                    className={cs(
                      "absolute inset-x-3 bottom-10 z-10 h-1/2 rounded-full bg-white drop-shadow-btn-light"
                    )}
                  ></div>
                </div>
              </Listbox.Options>
            </div>
          </Listbox>
          <RHFFormTextInputWithLabel
            slug="email"
            label="Email"
            register={register}
            registerArgs={{
              pattern:
                /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/,
            }}
            error={errors.email}
            inputArgs={{
              type: "email",
              placeholder: "you@email.com",
              required: true,
              disabled: !hasManageInvitationsRole,
            }}
          />

          <div className="mt-4">
            <Button isBlackButton disabled={isSendButtonDisabled} type="submit">
              Send invitation
            </Button>
          </div>
        </div>
      </form>
    </div>
  );
};

export default NewInvitation;
