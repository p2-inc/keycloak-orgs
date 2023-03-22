import Button from "components/elements/forms/buttons/button";
import RHFFormTextInputWithLabel from "components/elements/forms/inputs/rhf-text-input-with-label";
import RoundedIcon from "components/elements/rounded-icon";
import { GlobeIcon } from "components/icons";
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

const addIcon = (
  <RoundedIcon className="my-4">
    <GlobeIcon />
  </RoundedIcon>
);

const DomainsAdd = () => {
  let { orgId } = useParams();
  const navigate = useNavigate();

  const { data: org = {} } = useGetOrganizationByIdQuery({
    orgId: orgId!,
    realm: config.env.realm,
  });

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

      const resp = await updateOrg({
        orgId: orgId!,
        realm: config.env.realm,
        organizationRepresentation: orgCopy,
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
        title: `${domain} has been added to organization. Please verify domain.`,
      });
      navigate(`/organizations/${orgId}/settings`);
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
              className="inline-block rounded-lg px-4 py-2 font-medium opacity-60 transition hover:bg-gray-100 hover:opacity-100"
            >
              Details
            </Link>
            <Link
              to={`/organizations/${orgId}/settings`}
              className="inline-block rounded-lg px-4 py-2 font-medium opacity-60 transition hover:bg-gray-100 hover:opacity-100"
            >
              Settings
            </Link>
          </div>
        }
      />
      {org.domains && org.domains?.length > 0 && (
        <div className="space-y-2 py-5">
          <div className="text-md">Current registered domains</div>
          {org.domains.map((domain) => (
            <div key={domain}>{domain}</div>
          ))}
        </div>
      )}

      <div className="space-y-5 py-10">
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
            }}
          />
          <div className="pt-3">
            <Button isBlackButton={true} type="submit">
              Add domain
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default DomainsAdd;
