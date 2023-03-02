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
import { useNavigate, useParams } from "react-router-dom";
import { useKeycloak } from "@react-keycloak/web";
import SquareBadge from "components/elements/badges/square-badge";
import P2Toast from "components/utils/toast";

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
  <div className="mb-8">
    <div className={cs("relative h-12 w-12 overflow-hidden rounded-md")}>
      <div className="absolute -inset-10 z-10 bg-gradient-to-tr from-[#C7DFF0] to-[#1476B7]"></div>
      <div className="absolute inset-[2px] z-20 flex items-center justify-center rounded bg-white">
        <UserIcon className="h-5 w-5"></UserIcon>
      </div>
    </div>
  </div>
);

const admin = (
  <div>
    <div className="text-sm font-medium">Admin</div>
    <div className="flex flex-wrap justify-start">
      {adminRoles.map((ar) => (
        <SquareBadge className="mt-1 mr-1">{ar}</SquareBadge>
      ))}
    </div>
  </div>
);
const member = (
  <div>
    <div className="text-sm font-medium">Member</div>
    <div className="flex flex-wrap justify-start">
      {adminRoles.map((ar) => (
        <SquareBadge className="mt-1 mr-1">{ar}</SquareBadge>
      ))}
    </div>
  </div>
);

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

  const [selectedRoles, setSelectedRoles] = useState([]);
  const [addOrganizationInvitation] = useAddOrganizationInvitationMutation();

  const onSubmit = async (data) => {
    console.log("🚀 ~ file: new.tsx:79 ~ onSubmit ~ onSubmit:", data);
    if (selectedRoles.length > 0 && data.email) {
      const resp = await addOrganizationInvitation({
        orgId: orgId!,
        realm: apiRealm,
        invitationRequestRepresentation: {
          email: data.email,
          inviterId: keycloak.tokenParsed?.sub,
          roles: selectedRoles,
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

  const isSendButtonDisabled = !selectedRoles;
  return (
    <div className="mt-16">
      <SectionHeader
        title="Invite new member"
        description="Add a new member to the organization by entering their email and assigning them a role within the organization. An email will be sent to them with instructions on how to join."
        icon={loadingIcon}
      />
      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="mt-8 space-y-4">
          {/* TODO: Update this component to a headless UI then integrate with form */}
          <Dropdown
            items={[
              { content: admin, value: adminRoles, id: 1 },
              { content: member, value: memberRoles, id: 2 },
              {
                content: <div className="text-sm font-medium">Custom</div>,
                value: "Canada",
                id: 2,
              },
            ]}
            emptyContent={<span>Select role</span>}
            className="block w-full"
            onChange={(selection) => setSelectedRoles(selection.value)}
          />
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
