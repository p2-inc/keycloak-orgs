import { useRoleAccess } from "@app/hooks";
import { useAppDispatch, useAppSelector } from "@app/hooks/hooks";
import { setOrganization, setApiMode } from "@app/services";
import {
  Button,
  Checkbox,
  Divider,
  FormGroup,
  Modal,
  ModalVariant,
  Radio,
} from "@patternfly/react-core";
import { useKeycloak } from "@react-keycloak/web";
import React, { useState, useEffect } from "react";

type Props = {
  open: boolean;
  toggleModal: (modalOpen: boolean) => void;
};

const OrgPicker: React.FC<Props> = ({
  open: isModalOpen,
  toggleModal: setIsModalOpen,
}) => {
  const { hasOrganizationRoles, hasRealmRoles } = useRoleAccess();
  const dispatch = useAppDispatch();
  const currentOrg = useAppSelector((state) => state.settings.currentOrg);
  const { keycloak } = useKeycloak();
  const orgs = keycloak?.tokenParsed?.organizations || {};

  const [selectedOrg, setSelectedOrg] = useState<string>();

  useEffect(() => {
    if (currentOrg) setSelectedOrg(currentOrg);
  }, [currentOrg]);

  useEffect(() => {
    if (isModalOpen && currentOrg) setSelectedOrg(currentOrg);
  }, [isModalOpen]);

  const handleModalConfirm = () => {
    dispatch(setOrganization(selectedOrg || currentOrg!));
    dispatch(
      selectedOrg === "global" ? setApiMode("onprem") : setApiMode("cloud")
    );

    handleModalToggle();
  };

  const handleModalToggle = () => {
    setIsModalOpen(!isModalOpen);
    setSelectedOrg(undefined);
  };

  const onEscapePress = () => false;

  const OrgRadioGroups = Object.keys(orgs).map((orgId) => {
    const orgName = orgs[orgId].name;
    const hasAdminRole = hasOrganizationRoles("admin", orgId);

    if (!hasAdminRole) return <></>;

    return (
      <div className="radio-group" key={orgId}>
        <FormGroup role="group" fieldId={`basic-form-radio-group-${orgId}`}>
          <Radio
            label={orgName}
            aria-label={orgName}
            id={orgId}
            name={orgId}
            description={`Configure the ${orgName} organziation.`}
            isChecked={orgId === selectedOrg}
            onChange={() => setSelectedOrg(orgId)}
          />
        </FormGroup>
      </div>
    );
  });

  return (
    <Modal
      title="Choose Your Organization"
      variant={ModalVariant.small}
      isOpen={isModalOpen}
      onClose={handleModalToggle}
      actions={[
        <Button key="confirm" variant="primary" onClick={handleModalConfirm}>
          Confirm
        </Button>,
      ]}
      showClose={false}
      onEscapePress={onEscapePress}
    >
      <div>
        For which Organization are you configuring an Identity Provider?
      </div>
      <br />
      <div>{OrgRadioGroups.map((grp) => grp)}</div>
      {hasRealmRoles() && (
        <>
          <Divider className="pf-u-mt-lg pf-u-mb-lg" />
          <div className="radio-group">
            <FormGroup role="group" fieldId={`basic-form-radio-group-realm`}>
              <Radio
                label="Global"
                aria-label="Global"
                id="global"
                name="global"
                description="No organization selected. Site administration config."
                isChecked={"global" === selectedOrg}
                onChange={() => setSelectedOrg("global")}
              />
            </FormGroup>
          </div>{" "}
        </>
      )}
    </Modal>
  );
};

export { OrgPicker };
