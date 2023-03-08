import { UserIcon } from "@heroicons/react/20/solid";
import SectionHeader from "components/navs/section-header";
import cs from "classnames";
import Button from "components/elements/forms/buttons/button";
import Dropdown from "components/elements/forms/dropdown/dropdown";
import { useAddOrganizationInvitationMutation } from "store/apis/orgs";
import { useState } from "react";
import RHFFormTextInputWithLabel from "components/elements/forms/inputs/rhf-text-input-with-label";
import { useForm } from "react-hook-form";
import { apiRealm } from "store/apis/helpers";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useKeycloak } from "@react-keycloak/web";
import SquareBadge from "components/elements/badges/square-badge";
import P2Toast from "components/utils/toast";
import { Listbox } from "@headlessui/react";
import { ChevronIcon } from "components/icons";

export const defaultRoles = [
  "view-organization",
  "manage-organization",
  "view-members",
  "manage-members",
  "view-roles",
  "manage-roles",
  "view-invitations",
  "manage-invitations",
  "view-identity-providers",
  "manage-identity-providers",
] as const;

const adminRoles = [...defaultRoles];
const memberRoles = defaultRoles.filter((r) => r.includes("view"));

const loadingIcon = (
  <div>
    <div className={cs("relative h-12 w-12 overflow-hidden rounded-md")}>
      <div className="absolute -inset-10 z-10 bg-gradient-to-tr from-[#C7DFF0] to-[#1476B7]"></div>
      <div className="absolute inset-[2px] z-20 flex items-center justify-center rounded bg-white">
        <UserIcon className="h-5 w-5"></UserIcon>
      </div>
    </div>
  </div>
);

const roles = [
  { id: 1, name: "Admin", items: adminRoles },
  { id: 2, name: "Member", items: memberRoles },
];

const NewInvitation = () => {
  const { keycloak } = useKeycloak();
  const navigate = useNavigate();

  let { orgId } = useParams();
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm();

  const [addOrganizationInvitation] = useAddOrganizationInvitationMutation();
  const [selectedRole, setselectedRole] = useState(roles[0]);

  const onSubmit = async (data) => {
    console.log("🚀 ~ file: new.tsx:79 ~ onSubmit ~ onSubmit:", data);
    if (selectedRole && data.email) {
      const resp = await addOrganizationInvitation({
        orgId: orgId!,
        realm: apiRealm,
        invitationRequestRepresentation: {
          email: data.email,
          inviterId: keycloak.tokenParsed?.sub,
          roles: [selectedRole.name],
        },
      });
      //@ts-ignore
      if (resp.error) {
        return P2Toast({
          error: true,
          //@ts-ignore
          title: resp.error?.data?.error,
        });
      }

      reset();
      P2Toast({
        success: true,
        title: `${data.email} has been sent an invitation.`,
      });
      navigate(`/organizations/${orgId}/details`);
    }
  };

  const isSendButtonDisabled = !selectedRole;
  return (
    <div className="mt-4 md:mt-16">
      <SectionHeader
        title="Invite new member"
        description="Add a new member to the organization by entering their email and assigning them a role within the organization. An email will be sent to them with instructions on how to join."
        icon={loadingIcon}
        rightContent={
          <Link to={`/organizations/${orgId}/details`} className="font-medium inline-block px-4 py-2 opacity-60 rounded-lg transition hover:bg-gray-100 hover:opacity-100">Cancel</Link>
        }
      />
      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="mt-8 space-y-4">
          <Listbox value={selectedRole} onChange={setselectedRole}>
            <div className="relative z-50">
              <Listbox.Button className="flex w-full items-center justify-between space-x-3 rounded border border-neutral-300 bg-neutral-50 py-2 px-4 text-left hover:border-p2blue-700 hover:bg-white">
                <div>{selectedRole.name}</div>
                <ChevronIcon className="rotate-90 stroke-gray-800" />
              </Listbox.Button>
              <Listbox.Options className="absolute w-full">
                <div className="pb-10">
                  <div className="relative bottom-0 z-30 max-h-96 divide-y overflow-auto rounded border border-neutral-300 bg-white">
                    {roles.map((role) => (
                      <Listbox.Option
                        key={role.id}
                        value={role}
                        className="cursor-pointer space-y-2 p-4 hover:bg-neutral-50"
                      >
                        <div className="font-semibold">{role.name}</div>
                        <div>
                          {role.items.map((ar) => (
                            <SquareBadge className="mt-1 mr-1">
                              {ar}
                            </SquareBadge>
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
