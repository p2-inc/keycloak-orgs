import { useAppDispatch, useAppSelector } from "@app/hooks/hooks";
import { setOrganization } from "@app/services";
import {
  Button,
  Dropdown,
  DropdownItem,
  DropdownToggle,
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
  const dispatch = useAppDispatch();
  const currentOrg = useAppSelector((state) => state.settings.selectedOrg);
  const { keycloak } = useKeycloak();
  const orgs = keycloak?.tokenParsed?.organizations;
  console.log("[orgs]", orgs, keycloak?.tokenParsed);

  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [selectedOrg, setSelectedOrg] = useState<string>();

  useEffect(() => {
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

  const handleDropdownToggle = (isDropdownOpen: boolean) => {
    setIsDropdownOpen(isDropdownOpen);
  };

  const onSelect = (evt: React.SyntheticEvent<HTMLDivElement, Event>) => {
    setSelectedOrg(evt.target.value);
    setIsDropdownOpen(!isDropdownOpen);
    onFocus();
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

  // orgs.map...

  const dropdownItems = Object.keys(orgs).map((orgId) => (
    <DropdownItem key={orgId} component="button" value={orgId}>
      {orgs[orgId].name}
    </DropdownItem>
  ));

  // const dropdownItems = [
  //   <DropdownItem key="org1" component="button" value="Garth">
  //     Org 1
  //   </DropdownItem>,
  //   <DropdownItem key="org2" component="button" value="Wayne">
  //     Org 2
  //   </DropdownItem>,
  // ];

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
        <Dropdown
          onSelect={onSelect}
          toggle={
            <DropdownToggle
              id="modal-dropdown-toggle"
              onToggle={handleDropdownToggle}
              toggleIndicator={CaretDownIcon}
            >
              {selectedOrg
                ? `Selected Org: ${orgs[selectedOrg].name}`
                : currentOrg
                ? `Current Org: ${currentOrg}`
                : "Select Organization"}
            </DropdownToggle>
          }
          isOpen={isDropdownOpen}
          dropdownItems={dropdownItems}
          menuAppendTo="parent"
        />
      </div>
    </Modal>
  );
};

export { OrgPicker };
