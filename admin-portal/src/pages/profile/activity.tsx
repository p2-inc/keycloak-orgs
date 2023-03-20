import { Fragment, useState } from "react";
import SectionHeader from "components/navs/section-header";
import ActivityLoader from "components/loaders/activity";
import ActivityItem from "components/elements/activity/item";
import ConfirmationModal from "components/elements/confirmation-modal";
import {
  useDeleteCurrentSessionMutation,
  useDeleteSessionMutation,
  useGetDevicesQuery,
  DeviceRepresentation,
  SessionRepresentation,
  ClientRepresentation,
} from "store/apis/profile";
import { config } from "config";
import { keycloakService } from "keycloak";
import TimeUtil from "services/time-util";
import Button from "components/elements/forms/buttons/button";
import { Smartphone, Monitor } from "lucide-react";

type SignOutSessionState = {
  device: DeviceRepresentation;
  session: SessionRepresentation;
};

const ActivityProfile = () => {
  const [showSignOutAllConfModal, setShowSignOutAllConfModal] = useState(false);
  const [showSignOutSession, setShowSignOutSession] = useState<boolean>(false);
  const [signOutSessionData, setSignOutSessionData] =
    useState<SignOutSessionState>();
  const { data: devices = [], isFetching } = useGetDevicesQuery({
    realm: config.env.realm,
  });
  const [deleteSessions] = useDeleteCurrentSessionMutation();
  const [deleteSession, { isSuccess }] = useDeleteSessionMutation();

  const signOutAll = () => {
    deleteSessions({
      realm: config.env.realm,
    }).then(() => {
      keycloakService.logout();
    });
  };

  const signOutSession = (
    device: DeviceRepresentation,
    session: SessionRepresentation
  ) => {
    deleteSession({
      realm: config.env.realm,
      sessionId: session.id!,
    }).then(() => {
      //refresh devices? automatic?
      //ContentAlert.success('signedOutSession', [session.browser, device.os]);
    });
  };

  const time = (time: number | undefined): string => {
    if (time === undefined) return "";
    else return TimeUtil.format(time * 1000);
  };

  const elementId = (
    item: string,
    session: SessionRepresentation,
    element: string = "session"
  ): string => {
    return `${element}-${session.id?.substring(0, 7)}-${item}`;
  };

  const findDeviceTypeIcon = (
    session: SessionRepresentation,
    device: DeviceRepresentation
  ): React.ReactNode => {
    if (device.mobile) {
      return <Smartphone id={elementId("icon-mobile", session, "device")} />;
    }
    return <Monitor id={elementId("icon-desktop", session, "device")} />;
  };

  const findOS = (device: DeviceRepresentation): string => {
    if (device.os?.toLowerCase().includes("unknown")) {
      //return Msg.localize('unknownOperatingSystem');
      return "Unknown";
    }
    return device.os ?? "";
  };

  const findOSVersion = (device: DeviceRepresentation): string => {
    if (device.osVersion?.toLowerCase().includes("unknown")) return "";

    return device.osVersion ?? "";
  };

  const makeClientsString = (clients: ClientRepresentation[]): string => {
    let clientsString = "";
    clients.forEach((client: ClientRepresentation, index: number) => {
      let clientName: string;
      if (
        client.hasOwnProperty("clientName") &&
        client.clientName !== undefined &&
        client.clientName !== ""
      ) {
        //clientName = Msg.localize(client.clientName);
        clientName = client.clientName;
      } else {
        clientName = client.clientId ?? "";
      }
      clientsString += clientName;
      if (clients.length > index + 1) clientsString += ", ";
    });
    return clientsString;
  };

  const isShowSignOutAll = (devices: DeviceRepresentation[]): boolean => {
    if (devices.length === 0) return false;
    if (devices.length > 1) return true;
    if (devices[0]?.sessions && devices[0]?.sessions.length > 1) return true;
    return false;
  };

  return (
    <div>
      {showSignOutAllConfModal && (
        <ConfirmationModal
          open={showSignOutAllConfModal}
          close={() => setShowSignOutAllConfModal(false)}
          buttonTitle="Sign out all devices"
          buttonId="sign-out-all"
          modalTitle="Sign out all devices"
          modalMessage="This action will sign out all the devices that have signed in to your account, including the current device you are using."
          onContinue={() => signOutAll()}
        />
      )}
      {showSignOutSession && signOutSessionData && (
        <ConfirmationModal
          open={!!showSignOutSession}
          close={() => {
            setShowSignOutSession(false);
            setSignOutSessionData(undefined);
          }}
          modalTitle="Sign out"
          modalMessage="Sign out this session?"
          onContinue={() =>
            signOutSession(
              signOutSessionData.device,
              signOutSessionData.session
            )
          }
        />
      )}
      <div className="mb-12">
        <SectionHeader
          title="Device activity"
          description="Sign out of any unfamiliar devices."
        />
        {isShowSignOutAll(devices) && (
          <div className="mt-3">
            <Button onClick={() => setShowSignOutAllConfModal(true)}>
              Sign out all devices
            </Button>
          </div>
        )}
      </div>
      <div className="w-full rounded border border-gray-200 bg-gray-50">
        {isFetching && <ActivityLoader />}
        {!isFetching &&
          devices.map((device: DeviceRepresentation, deviceIndex: number) => (
            <div className="divide-y">
              {device.sessions!.map(
                (session: SessionRepresentation, sessionIndex: number) => (
                  <Fragment
                    key={"device-" + deviceIndex + "-session-" + sessionIndex}
                  >
                    <div>
                      <div className="items-center space-y-2 px-4 pt-3 md:flex md:justify-between md:space-y-0">
                        <div className="md:flex md:items-center">
                          <div className="py-2 md:py-0">
                            {findDeviceTypeIcon(session, device)}
                          </div>
                          <div className="space-y-2 text-sm font-semibold text-p2gray-900 md:pl-2">
                            <span
                              id={elementId("browser", session)}
                              className="pf-u-mr-md session-title"
                            >
                              {findOS(device)} {findOSVersion(device)} /{" "}
                              {session.browser}
                            </span>
                          </div>
                        </div>
                        {!session.current && (
                          <Button
                            onClick={() => {
                              setShowSignOutSession(true);
                              setSignOutSessionData({
                                device,
                                session,
                              });
                            }}
                            isCompact
                          >
                            Sign out session
                          </Button>
                        )}
                        {session.current && (
                          <span
                            id={elementId("current-badge", session)}
                            className="flex items-center space-x-2 rounded border border-p2blue-700/30 bg-p2blue-700/10 px-3 py-1 text-xs font-medium text-p2blue-700"
                          >
                            <span className="relative flex h-2 w-2">
                              <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-p2blue-700 opacity-75"></span>
                              <span className="relative inline-flex h-2 w-2 rounded-full bg-p2blue-700"></span>
                            </span>
                            <span>Current session</span>
                          </span>
                        )}
                      </div>
                      <div className="p-4 md:grid md:grid-cols-5">
                        <div className="">
                          <ActivityItem title="IP address">
                            {session.ipAddress}
                          </ActivityItem>
                        </div>
                        <div className="">
                          <ActivityItem title="Last accessed">
                            {time(session.lastAccess)}
                          </ActivityItem>
                        </div>
                        <div className="">
                          <ActivityItem title="Clients">
                            {session.clients &&
                              makeClientsString(session.clients)}
                          </ActivityItem>
                        </div>
                        <div className="">
                          <ActivityItem title="Started">
                            {time(session.started)}
                          </ActivityItem>
                        </div>
                        <div className="">
                          <ActivityItem title="Expires">
                            {time(session.expires)}
                          </ActivityItem>
                        </div>
                      </div>
                    </div>
                  </Fragment>
                )
              )}
            </div>
          ))}
      </div>
    </div>
  );
};

export default ActivityProfile;
