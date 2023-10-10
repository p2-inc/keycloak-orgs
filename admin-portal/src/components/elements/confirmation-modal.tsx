import { FC, Fragment } from "react";
import { Dialog, Transition } from "@headlessui/react";
import Button from "components/elements/forms/buttons/button";
import { CheckCircleIcon } from "@heroicons/react/24/outline";
import { useTranslation } from "react-i18next";

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
  open: boolean;
  close: () => void;
};

const ConfirmationModal: FC<ConfirmationModalProps> = ({
  children,
  modalTitle,
  modalMessage,
  modalCancelButtonLabel,
  modalContinueButtonLabel,
  onContinue,
  onClose,
  open,
  close,
}) => {
  const { t } = useTranslation();
  if (!modalCancelButtonLabel) {
    modalCancelButtonLabel = t("cancel");
  }
  if (!modalContinueButtonLabel) {
    modalContinueButtonLabel = t("confirm");
  }
  const handleModalToggle = () => {
    close();
    if (onClose) onClose();
  };

  const handleContinue = () => {
    handleModalToggle();
    onContinue();
  };

  return (
    <Transition.Root show={open} as={Fragment}>
      <Dialog as="div" className="relative z-50" onClose={close}>
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-300"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-200"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" />
        </Transition.Child>

        <div className="fixed inset-0 z-10 overflow-y-auto">
          <div className="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-300"
              enterFrom="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
              enterTo="opacity-100 translate-y-0 sm:scale-100"
              leave="ease-in duration-200"
              leaveFrom="opacity-100 translate-y-0 sm:scale-100"
              leaveTo="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
            >
              <Dialog.Panel className="relative transform overflow-hidden rounded-lg bg-white px-4 pt-5 pb-4 text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg sm:p-6">
                <div className="sm:flex sm:items-start">
                  <div className="mx-auto flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full bg-p2blue-200 sm:mx-0 sm:h-10 sm:w-10">
                    <CheckCircleIcon className="h-6 w-6" aria-hidden="true" />
                  </div>
                  <div className="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
                    <Dialog.Title
                      as="h3"
                      className="text-base font-semibold leading-6 text-gray-900"
                    >
                      {modalTitle}
                    </Dialog.Title>
                    {modalMessage && (
                      <div className="mt-2">
                        <p className="text-sm text-gray-500">{modalMessage}</p>
                      </div>
                    )}
                    {children && (
                      <div className="mt-2">
                        <p className="text-sm text-gray-500">{children}</p>
                      </div>
                    )}
                  </div>
                </div>
                <div className="mt-5 sm:mt-4 sm:flex sm:flex-row-reverse">
                  <Button
                    isBlackButton
                    className="inline-flex w-full justify-center sm:ml-3 sm:w-auto"
                    onClick={handleContinue}
                  >
                    {modalContinueButtonLabel}
                  </Button>
                  <Button
                    onClick={handleModalToggle}
                    className="mt-3 inline-flex w-full sm:mt-0 sm:w-auto"
                  >
                    {modalCancelButtonLabel}
                  </Button>
                </div>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition.Root>
  );
};

export default ConfirmationModal;
