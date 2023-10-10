import ProfileData from "./components/profile";
import DeleteProfile from "./components/delete";
import { config } from "config";
import Internationalization from "./components/internationalization";

const GeneralProfile = () => {
  const { features: featureFlags } = config.env;

  // Preserving for later use
  // {
  //   (data?.userProfileMetadata?.attributes || [])
  //     .filter((attribute) => !isRootAttribute(attribute.name))
  //     .map((attribute) => (
  //       <FormTextInputWithLabel
  //         slug={attribute.name!}
  //         label={
  //           (isBundleKey(attribute.displayName)
  //             ? t(unWrap(attribute.displayName!))
  //             : attribute.displayName) || attribute.name!
  //         }
  //         inputArgs={{ value: data ? data[attribute.name!] : "" }}
  //       />
  //     ));
  // }

  return (
    <div>
      <ProfileData />
      {featureFlags.internationalizationEnabled && <Internationalization />}
      {featureFlags.deleteAccountAllowed && <DeleteProfile />}
    </div>
  );
};

export default GeneralProfile;
