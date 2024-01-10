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
import { User } from "lucide-react";
import useUser from "components/utils/useUser";
import Alert from "components/elements/alerts/alert";
import { useTranslation } from "react-i18next";
import { DecoratedRole, RolesList } from "pages/member/components";

const { realm } = config.env;

const loadingIcon = (
  <div>
    <div className={cs("relative h-12 w-12 overflow-hidden rounded-md")}>
      <div className="absolute -inset-10 z-10 bg-primary-gradient"></div>
      <div className="absolute inset-[2px] z-20 flex items-center justify-center rounded bg-white dark:bg-p2dark-1000 dark:text-zinc-200">
        <User />
      </div>
    </div>
  </div>
);

const NewInvitation = () => {
  const { keycloak } = useKeycloak();
  const navigate = useNavigate();
  let { orgId } = useParams();
  const { t } = useTranslation();

  const { hasManageInvitationsRole: hasManageInvitationsRoleCheck } = useUser();

  const { data: org } = useGetOrganizationByIdQuery({
    orgId: orgId!,
    realm: config.env.realm,
  });

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm();

  const [addOrganizationInvitation] = useAddOrganizationInvitationMutation();
  const [selectedRoles, setSelectedRoles] = useState<DecoratedRole[]>();

  const onSubmit = async (data) => {
    if (data.email) {
      P2Toast({
        title: `${data.email} is being sent an invitation.`,
        information: true,
      });
      await addOrganizationInvitation({
        orgId: orgId!,
        realm,
        invitationRequestRepresentation: {
          email: data.email,
          inviterId: keycloak.tokenParsed?.sub,
          roles: selectedRoles?.filter((r) => r.isChecked).map((r) => r.name),
          send: true,
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

  const isSendButtonDisabled = !hasManageInvitationsRole || isSubmitting;

  return (
    <div className="mt-4 md:mt-16">
      <SectionHeader
        title={t("invitationTitle", [org?.displayName || "Organization"])}
        description={t("invitationInstructionsDescription")}
        icon={loadingIcon}
        rightContent={
          <Link
            to={`/organizations/${orgId}/details`}
            className="inline-block rounded-lg px-4 py-2 font-medium opacity-60 transition hover:bg-gray-100 hover:opacity-100 dark:text-zinc-200 dark:hover:bg-p2dark-1000"
          >
            {t("cancel")}
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
      <RolesList orgId={orgId!} setSelectedRoles={setSelectedRoles} />
      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="mt-8 space-y-4">
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
              {t("sendInvitation")}
            </Button>
          </div>
        </div>
      </form>
    </div>
  );
};

export default NewInvitation;
