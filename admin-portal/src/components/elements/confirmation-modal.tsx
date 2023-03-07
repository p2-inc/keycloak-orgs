import { FC, useState } from "react";
import { Dialog } from '@headlessui/react';
import Button from "components/elements/forms/buttons/button";

type ConfirmationModalProps = {
    children?: React.ReactNode;
    buttonTitle?: string;
    buttonId?: string;
    render?(toggle: () => void): React.ReactNode;
    modalTitle: string;
    modalMessage?: string;
    modalContinueButtonLabel?: string;
    modalCancelButtonLabel?: string;
    onContinue: () => void;
    onClose?: () => void;
    isDisabled?: boolean;
};

const ConfirmationModal: FC<ConfirmationModalProps> = (props) => {
  let [isOpen, setIsOpen] = useState(false)

  const handleModalToggle = () => {
    setIsOpen(!isOpen);
    if (props.onClose) props.onClose();
  };

  const handleContinue = () => {
    handleModalToggle();
    props.onContinue();
  };

  return (
    <>
    {!props.render &&
    <Button id={props.buttonId} onClick={handleModalToggle} disabled={props.isDisabled}>
        {props.buttonTitle}
    </Button>}
    {props.render && props.render(handleModalToggle)}
    <Dialog open={isOpen} onClose={() => setIsOpen(false)} className="relative z-50">
        <Dialog.Panel>
            <Dialog.Title>{props.modalTitle}</Dialog.Title>
            <Dialog.Description>{props.modalMessage}</Dialog.Description>
            {props.children}
            <Button onClick={() => handleContinue()}>{props.modalContinueButtonLabel}</Button>
            <Button onClick={() => handleModalToggle()}>{props.modalCancelButtonLabel}</Button>
        </Dialog.Panel>
    </Dialog>
  </>
  );
};

export default ConfirmationModal;
