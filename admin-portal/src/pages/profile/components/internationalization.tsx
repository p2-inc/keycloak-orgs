import { useEffect, useState } from "react";
import Button from "components/elements/forms/buttons/button";
import SectionHeader from "components/navs/section-header";
import { config } from "config";
import {
  useGetAccountQuery,
  useUpdateAccountMutation,
} from "store/apis/profile";
import { useTranslation } from "react-i18next";
import P2Toast from "components/utils/toast";
import Dropdown from "components/elements/forms/dropdown/hui-dropdown";
import { first } from "lodash";
import { setLanguage } from "../../../i18n";

const { realm, supportedLocales } = config.env;

const Internationalization = () => {
  const { t } = useTranslation();

  const noSelection = { id: "none", name: t("selectLocale"), disabled: true };
  const localeOptions = [
    noSelection,
    ...Object.keys(supportedLocales).map((key) => ({
      id: key,
      name: supportedLocales[key],
    })),
  ];

  const [selectedLocale, setSelectedLocale] = useState<{
    id: string;
    name: string;
  }>(localeOptions[0]);

  const { data: account, isLoading: isLoadingAccount } = useGetAccountQuery({
    userProfileMetadata: true,
    realm,
  });

  useEffect(() => {
    const hasLocale = localeOptions.find(
      (l) => l.id === first(account?.attributes?.locale)
    );
    if (hasLocale) {
      const locale =
        localeOptions[
          localeOptions.findIndex(
            (l) => l.id === first(account?.attributes?.locale)
          )
        ];

      setSelectedLocale(locale);
      setLanguage(locale.id);
    }
  }, [account]);

  const [updateAccount, { isLoading: isUpdatingAccount }] =
    useUpdateAccountMutation();

  const onSubmit = async (e) => {
    e.preventDefault();

    if (selectedLocale.id !== "none") {
      const updatedAccount = {
        ...account,
        attributes: { ...account?.attributes, locale: [selectedLocale.id] },
      };
      updateAccount({
        accountRepresentation: updatedAccount,
        realm,
      })
        .unwrap()
        .then(() => {
          P2Toast({
            success: true,
            title: t("localeUpdatedSuccessfully"),
          });
        })
        .catch((err) => {
          return P2Toast({
            error: true,
            title: `${t("localeUpdateFailed")} ${err.data.error}`,
          });
        });
    }
  };

  const reset = () => {
    setSelectedLocale(
      localeOptions.find((l) => l.id === first(account?.attributes?.locale)) ||
        noSelection
    );
  };

  const isSameLocale = first(account?.attributes?.locale) === selectedLocale.id;
  return (
    <>
      <div className="mb-6 mt-12">
        <SectionHeader
          title={t("localization")}
          description={t("profile-localization-description")}
        />
      </div>
      <form className="mb-6 max-w-xl space-y-4" onSubmit={onSubmit}>
        <>
          <Dropdown
            items={localeOptions}
            selectedItem={selectedLocale}
            name="locale"
            onChange={setSelectedLocale}
            listBoxProps={{
              disabled: isLoadingAccount,
            }}
          />

          <div className="space-x-2">
            <Button
              isBlackButton
              type="submit"
              disabled={
                isUpdatingAccount ||
                isLoadingAccount ||
                selectedLocale.id === "none" ||
                isSameLocale
              }
            >
              {t("updateLocale")}
            </Button>
            <Button
              type="button"
              onClick={reset}
              disabled={isUpdatingAccount || isSameLocale}
            >
              {t("reset")}
            </Button>
          </div>
        </>
      </form>
    </>
  );
};

export default Internationalization;
