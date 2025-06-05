import Button from "@/components/elements/forms/buttons/button";
import RHFFormTextInputWithLabel from "@/components/elements/forms/inputs/rhf-text-input-with-label";
import RoundedIcon from "@/components/elements/rounded-icon";
import SectionHeader from "@/components/navs/section-header";
import P2Toast from "@/components/utils/toast";
import { useForm } from "react-hook-form";
import { Link, useNavigate, useParams } from "react-router-dom";
import { config } from "@/config";
import {
  useGetOrganizationByIdQuery,
  useGetOrganizationDomainsQuery,
  useUpdateOrganizationMutation,
} from "@/store/apis/orgs";
import isValidDomain from "is-valid-domain";
import { Globe } from "lucide-react";
import useUser from "@/components/utils/useUser";
import { useTranslation } from "react-i18next";
import TopHeader from "@/components/navs/top-header";
import Breadcrumbs from "@/components/navs/breadcrumbs";
import RoundBadge from "@/components/elements/badges/round-badge";

const addIcon = (
  <RoundedIcon className="my-4">
    <Globe className="h-5 w-5" />
  </RoundedIcon>
);

const { realm } = config.env;

const DomainsAdd = () => {
  const { t } = useTranslation();
  const { hasManageOrganizationRole: hasManageOrganizationRoleCheck } =
    useUser();
  let { orgId } = useParams();
  const navigate = useNavigate();

  const { data: org = {} } = useGetOrganizationByIdQuery({
    orgId: orgId!,
    realm,
  });
  console.log("🚀 ~ DomainsAdd ~ org:", org);
  const { refetch: refetchDomains } = useGetOrganizationDomainsQuery({
    realm,
    orgId: orgId!,
  });
  const hasManageOrganizationRole = hasManageOrganizationRoleCheck(orgId);

  const [updateOrg] = useUpdateOrganizationMutation();
  const {
    register,
    handleSubmit,
    formState: { errors },
    setError,
    reset,
  } = useForm();

  const onSubmit = async (data) => {
    const { domain } = data;

    if (org.domains?.includes(domain)) {
      P2Toast({
        error: true,
        title: `${domain} ${t("error-domain-already-present")}`,
      });
    } else if (!isValidDomain(domain)) {
      setError(
        "domain",
        { message: t("error-domain-invalid") },
        { shouldFocus: true }
      );
      P2Toast({
        error: true,
        title: t("error-domain-invalid"),
      });
    } else {
      const orgCopy = { ...org };
      if (orgCopy.domains) {
        orgCopy.domains = [...orgCopy.domains, domain];
      } else {
        orgCopy.domains = [domain];
      }

      await updateOrg({
        orgId: orgId!,
        realm,
        organizationRepresentation: orgCopy,
      })
        .unwrap()
        .then(() => {
          reset();
          P2Toast({
            success: true,
            title: t("domains-add-success", { domain }),
          });
          refetchDomains();
          return navigate(
            `/organizations/${orgId}/settings?verifyDomain=${domain}`
          );
        })
        .catch((e) => {
          P2Toast({
            error: true,
            title: e.data.error,
          });
        });
    }
  };

  const registeredDomainCount =
    org?.domains?.filter((domain) => domain.length > 0).length || "0";

  return (
    <div className="md:py-20">
      <SectionHeader
        title={t("addNewDomain")}
        description={t("addANewDomainToThisOrganization")}
        icon={addIcon}
        rightContent={
          <div className="space-x-2">
            <Link
              to={`/organizations/${orgId}/details`}
              className="inline-block rounded-lg px-4 py-2 font-medium opacity-60 transition hover:bg-gray-100 hover:opacity-100 dark:text-zinc-200 dark:hover:bg-p2dark-1000"
            >
              {t("orgDetails")}
            </Link>
            <Link
              to={`/organizations/${orgId}/settings`}
              className="inline-block rounded-lg px-4 py-2 font-medium capitalize opacity-60 transition hover:bg-gray-100 hover:opacity-100 dark:text-zinc-200 dark:hover:bg-p2dark-1000"
            >
              {t("manage")}
            </Link>
          </div>
        }
        breadCrumbs={
          <Breadcrumbs
            items={[
              { title: t("organizations"), link: `/organizations` },
              {
                title: org?.name || "",
                link: `/organizations/${orgId}/details`,
              },
            ]}
            dropLastSlash
          />
        }
      />
      <div className="space-y-5 py-10">
        {org.domains && org.domains?.length > 0 && (
          <div className="divide-y rounded-md border border-gray-200 dark:divide-zinc-600 dark:border-zinc-600">
            <div className="rounded-t-md bg-gray-50 px-3 py-2 text-sm font-semibold dark:bg-zinc-900 dark:text-zinc-200">
              {t("currentRegisteredDomains")}{" "}
              <RoundBadge>{registeredDomainCount}</RoundBadge>
            </div>
            <div className="divide-y dark:divide-zinc-600">
              {org.domains
                .filter((domain) => domain.length > 0)
                .map((domain) => (
                  <div
                    key={domain}
                    className="flex items-center space-x-2 px-3 py-2 text-sm dark:text-zinc-200"
                  >
                    <div>{domain}</div>
                  </div>
                ))}
            </div>
          </div>
        )}

        <div className="space-y-5">
          <form onSubmit={handleSubmit(onSubmit)}>
            <RHFFormTextInputWithLabel
              slug="domain"
              label={t("domainName")}
              register={register}
              registerArgs={{
                pattern:
                  /^(?!-)(?:[a-zA-Z\d-]{0,62}[a-zA-Z\d]\.)+(?:[a-zA-Z]{2,})$/,
              }}
              error={errors.domain}
              inputArgs={{
                type: "text",
                placeholder: t("domains-add-placeholder"),
                required: true,
                disabled: !hasManageOrganizationRole,
              }}
            />
            <div className="pt-3">
              <Button
                isBlackButton={true}
                type="submit"
                disabled={!hasManageOrganizationRole}
              >
                {t("addDomain")}
              </Button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default DomainsAdd;
