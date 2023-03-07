import { Fragment } from "react";
import { ComputerIcon } from "components/icons/computer";
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
import { apiRealm } from "store/apis/helpers";
import { keycloakService } from "keycloak";
import TimeUtil from "services/time-util";

const ActivityProfile = () => {
  const { data: devices = [], isFetching } = useGetDevicesQuery({
    realm: apiRealm,
  });
  const [deleteSessions] = useDeleteCurrentSessionMutation();
  const [deleteSession, { isSuccess }] = useDeleteSessionMutation();

  const signOutAll = () => {
    deleteSessions({
      realm: apiRealm,
    }).then(() => {
      keycloakService.logout();
    });
  };

  const signOutSession = (
    device: DeviceRepresentation,
    session: SessionRepresentation
  ) => {
    deleteSession({
      realm: apiRealm,
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

  const elementId = (item: string, session: SessionRepresentation, element: string='session'): string => {
    return `${element}-${session.id?.substring(0,7)}-${item}`;
  };

  const findDeviceTypeIcon = (session: SessionRepresentation, device: DeviceRepresentation): React.ReactNode => {
    const deviceType: boolean = device.mobile ?? false;
    if (deviceType === true) {
      //return (<MobileIcon className="fill-p2gray-800" id={elementId('icon-mobile', session, 'device')} />);
      return (<ComputerIcon className="fill-p2gray-800" />);
    }
    //return (<ComputerIcon className="fill-p2gray-800" id={elementId('icon-desktop', session, 'device')} />);
    return (<ComputerIcon className="fill-p2gray-800" />);
  };

  const findOS = (device: DeviceRepresentation): string => {
    if (device.os?.toLowerCase().includes('unknown')) {
      //return Msg.localize('unknownOperatingSystem');
      return "Unknown";
    }
    return device.os ?? "";
  };

  const findOSVersion = (device: DeviceRepresentation): string => {
    if (device.osVersion?.toLowerCase().includes('unknown')) return '';

    return device.osVersion ?? "";
  };

  const makeClientsString = (clients: ClientRepresentation[]): string => {
    let clientsString = "";
    clients.forEach( (client: ClientRepresentation, index: number) => {
      let clientName: string;
      if (client.hasOwnProperty('clientName') && (client.clientName !== undefined) && (client.clientName !== '')) {
        //clientName = Msg.localize(client.clientName);
        clientName = client.clientName;
      } else {
        clientName = client.clientId ?? "";
      }
      clientsString += clientName;
      if (clients.length > index + 1) clientsString += ', ';
    })
    return clientsString;
  };

  const isShowSignOutAll = (devices: DeviceRepresentation[]): boolean => {
    if (devices.length === 0) return false;
    if (devices.length > 1) return true;
    if (devices[0].sessions && devices[0].sessions.length > 1) return true;
    return false;
  };


  return (
    <div>
      <div className="mb-12">
        <SectionHeader
          title="Device activity"
          description="Sign out of any unfamiliar devices."
        />
      </div>
      { isShowSignOutAll(devices) &&
      <ConfirmationModal buttonTitle='Sign out all devices' buttonId='sign-out-all' modalTitle='Sign out all devices' modalMessage='This action will sign out all the devices that have signed in to your account, including the current device you are using.' onContinue={() => signOutAll()}/>
      }
      <div className="w-full rounded border border-gray-200 bg-gray-50">
        {isFetching && <ActivityLoader />}
        {!isFetching &&
          devices.map((device: DeviceRepresentation, deviceIndex: number) => (
            <>
              {device.sessions!.map(
                (session: SessionRepresentation, sessionIndex: number) => (
                  <Fragment
                    key={"device-" + deviceIndex + "-session-" + sessionIndex}
                  >
                    <div className="md:flex md:items-center">
                      <div className="p-4 pr-0 pb-0">
                      {findDeviceTypeIcon(session, device)}
                      </div>
                      <div className="p-4 pb-0 text-sm font-semibold text-p2gray-900 md:pl-2">
                        <span id={elementId('browser', session)} className="pf-u-mr-md session-title">{findOS(device)} {findOSVersion(device)} / {session.browser}</span>
                        {session.current &&
                        <><label id={elementId('current-badge', session)} className="block text-sm font-medium text-green-700">Current session</label></>}
                      </div>
                      <div>
                      {!session.current &&
                      <ConfirmationModal buttonTitle='Sign out' buttonId={elementId('sign-out', session)} modalTitle='Sign out' modalMessage='Sign out this session?' onContinue={() => signOutSession(device, session)}/>
                      }
                      </div>
                    </div>
                    <div className="md:grid md:grid-cols-5">
                      <div className="space-y-1 p-4">
                        <ActivityItem title="IP address">
                          {session.ipAddress}
                        </ActivityItem>
                      </div>
                      <div className="space-y-1 p-4">
                        <ActivityItem title="Last accessed">
                          {time(session.lastAccess)}
                        </ActivityItem>
                      </div>
                      <div className="space-y-1 p-4">
                        <ActivityItem title="Clients">
                          {session.clients && makeClientsString(session.clients)}
                        </ActivityItem>
                      </div>
                      <div className="space-y-1 p-4">
                        <ActivityItem title="Started">
                          {time(session.started)}
                        </ActivityItem>
                      </div>
                      <div className="space-y-1 p-4">
                        <ActivityItem title="Expires">
                          {time(session.expires)}
                        </ActivityItem>
                      </div>
                    </div>
                  </Fragment>
                )
              )}
            </>
          ))}
      </div>
    </div>
  );
};

export default ActivityProfile;
