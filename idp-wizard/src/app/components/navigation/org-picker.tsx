import { useRoleAccess } from "@app/hooks";
import { useAppDispatch, useAppSelector } from "@app/hooks/hooks";
import { setOrganization } from "@app/services";
import {
  Button,
  Checkbox,
  Dropdown,
  DropdownItem,
  DropdownToggle,
  FormGroup,
  Modal,
  ModalVariant,
} from "@patternfly/react-core";
import { CaretDownIcon } from "@patternfly/react-icons";
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
  const currentOrg = useAppSelector((state) => state.settings.selectedOrg);
  const { keycloak } = useKeycloak();
  const orgs = keycloak?.tokenParsed?.organizations;
  console.log("[orgs]", orgs, keycloak?.tokenParsed);

  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [selectedOrg, setSelectedOrg] = useState<string>();

  useEffect(() => {
    // TODO the App needs to determine this when it starts up
    dispatch(setOrganization("Chicken Kablooey"));
  }, []);

  const handleModalConfirm = () => {
    dispatch(
      setOrganization((selectedOrg && orgs[selectedOrg].name) || currentOrg!)
    );
    handleModalToggle();
  };

  const handleModalToggle = () => {
    setIsModalOpen(!isModalOpen);
    setSelectedOrg(undefined);
    setIsDropdownOpen(false);
  };

  const onFocus = () => {
    const element = document.getElementById("modal-dropdown-toggle");
    (element as HTMLElement).focus();
  };

  const onEscapePress = () => {
    if (isDropdownOpen) {
      setIsDropdownOpen(!isDropdownOpen);
      onFocus();
    } else {
      handleModalToggle();
    }
  };

  // Check realm access for a full global state
  // if not full global then a per org basis is required

  const OrgCheckboxGroups = Object.keys(orgs).map((orgId) => {
    // each org has a checkbox
    // with the right roles for the org
    // it will also have the Global option for that org
    const orgName = orgs[orgId].name;

    const hasAdminRole = hasOrganizationRoles("admin", orgId);

    // has role
    return (
      <FormGroup
        role="group"
        fieldId={`basic-form-checkbox-group-${orgId}`}
        label={orgName}
        className="pf-u-mb-md"
        key={orgId}
      >
        {hasAdminRole && (
          <Checkbox label="Global" aria-label="Global" id={`${orgId}_global`} />
        )}
        <Checkbox label={orgName} aria-label={orgName} id={orgId} />
      </FormGroup>
    );
  });

  return (
    <Modal
      title="Organization Selector"
      variant={ModalVariant.small}
      isOpen={isModalOpen}
      onClose={handleModalToggle}
      actions={[
        <Button key="confirm" variant="primary" onClick={handleModalConfirm}>
          Confirm
        </Button>,
        <Button key="cancel" variant="link" onClick={handleModalToggle}>
          Cancel
        </Button>,
      ]}
      onEscapePress={onEscapePress}
    >
      <div>
        For which Organization are you configuring an Identity Provider?
      </div>
      <br />
      <div>
        {hasRealmRoles("admin") && (
          <FormGroup
            role="group"
            fieldId={`basic-form-checkbox-group-realm`}
            label="Global Realm Setting"
            className="pf-u-mb-md"
          >
            <Checkbox label="Global" aria-label="Global" id={`realm_global`} />
          </FormGroup>
        )}
      </div>
      <div>{OrgCheckboxGroups.map((grp) => grp)}</div>
    </Modal>
  );
};

export { OrgPicker };
