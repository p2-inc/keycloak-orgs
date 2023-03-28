import Button from "components/elements/forms/buttons/button";
import RHFFormTextInputWithLabel from "components/elements/forms/inputs/rhf-text-input-with-label";
import RoundedIcon from "components/elements/rounded-icon";
import SectionHeader from "components/navs/section-header";
import P2Toast from "components/utils/toast";
import { useForm } from "react-hook-form";
import { Link, useNavigate, useParams } from "react-router-dom";
import { config } from "config";
import {
  useGetOrganizationByIdQuery,
  useUpdateOrganizationMutation,
} from "store/apis/orgs";
import isValidDomain from "is-valid-domain";
import { Globe } from "lucide-react";
import useUser from "components/utils/useUser";

const addIcon = (
  <RoundedIcon className="my-4">
    <Globe className="h-5 w-5" />
  </RoundedIcon>
);

const { realm } = config.env;

const DomainsAdd = () => {
  const { hasManageOrganizationRole: hasManageOrganizationRoleCheck } =
    useUser();
  let { orgId } = useParams();
  const navigate = useNavigate();

  const { data: org = {} } = useGetOrganizationByIdQuery({
    orgId: orgId!,
    realm,
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
        title: `${domain} is already present for this org.`,
      });
    } else if (!isValidDomain(domain)) {
      setError(
        "domain",
        { message: "Domain is not valid" },
        { shouldFocus: true }
      );
      P2Toast({
        error: true,
        title: `${domain} is not valid.`,
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
            title: `${domain} has been added to organization. Please verify domain.`,
          });
          return navigate(`/organizations/${orgId}/settings`);
        })
        .catch((e) => {
          P2Toast({
            error: true,
            title: e.data.error,
          });
        });
    }
  };

  return (
    <div className="md:py-20">
      <SectionHeader
        title="Add new domain"
        description="Add a new domain to this organization."
        icon={addIcon}
        rightContent={
          <div className="space-x-2">
            <Link
              to={`/organizations/${orgId}/details`}
              className="inline-block rounded-lg px-4 py-2 font-medium opacity-60 transition hover:bg-gray-100 hover:opacity-100 dark:text-zinc-200 dark:hover:bg-p2dark-1000"
            >
              Details
            </Link>
            <Link
              to={`/organizations/${orgId}/settings`}
              className="inline-block rounded-lg px-4 py-2 font-medium opacity-60 transition hover:bg-gray-100 hover:opacity-100 dark:text-zinc-200 dark:hover:bg-p2dark-1000"
            >
              Settings
            </Link>
          </div>
        }
      />
      <div className="space-y-5 py-10">
        {org.domains && org.domains?.length > 0 && (
          <div className="divide-y rounded-md border border-gray-200 dark:border-zinc-600 dark:divide-zinc-600">
            <div className="rounded-t-md bg-gray-50 px-3 py-2 text-sm font-semibold dark:bg-zinc-900 dark:text-zinc-200">
              Current registered domains
            </div>
            <div className="divide-y dark:divide-zinc-600">
              {org.domains.map((domain) => (
                <div key={domain} className="px-3 py-2 text-sm flex items-center space-x-2 dark:text-zinc-200">
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
              label="Domain name"
              register={register}
              registerArgs={{
                pattern:
                  /^(?!-)[A-Za-z0-9-]+([\-\.]{1}[a-z0-9]+)*\.[A-Za-z]{2,6}$/,
              }}
              error={errors.domain}
              inputArgs={{
                type: "text",
                placeholder: "www.your-domain.com",
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
                Add domain
              </Button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default DomainsAdd;
