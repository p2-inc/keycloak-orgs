import { Fragment } from 'react';
import { ComputerIcon } from "components/icons/computer";
import SectionHeader from "components/navs/section-header";
import { useDeleteCurrentSessionMutation, useDeleteSessionMutation, useGetDevicesQuery, DeviceRepresentation, SessionRepresentation, ClientRepresentation } from "store/apis/profile";
import { apiRealm } from "store/apis/helpers";
import { keycloakService } from "keycloak";
import TimeUtil from "services/time-util";

const ActivityProfile = () => {
  const { data: devices = [] } = useGetDevicesQuery({ realm: apiRealm });
  const [ deleteSessions ] = useDeleteCurrentSessionMutation();
  const [ deleteSession, { isSuccess }] = useDeleteSessionMutation();

  const signOutAll = () => {
    deleteSessions({
      realm: apiRealm
    }).then( () => {
      keycloakService.logout();
    });
  };

  const signOutSession = (device: DeviceRepresentation, session: SessionRepresentation) => {
    deleteSession({
      realm: apiRealm,
      sessionId: session.id!,
    }).then( () => {
      //refresh devices? automatic?
      //ContentAlert.success('signedOutSession', [session.browser, device.os]);
    });
  };

  const time = (time: number | undefined): string => {
    if (time === undefined) return "";
    else return TimeUtil.format(time * 1000);
  }

  //findDeviceTypeIcon
  //findOS
  //findOSVersion
  //makeClientsString
  //isShowSignOutAll

  return (
    <div>
      <div className="mb-12">
        <SectionHeader
          title="Device activity"
          description="Sign out of any unfamiliar devices."
        />
      </div>
      <div className="w-full rounded border border-gray-200 bg-gray-50">
        {devices.map((device: DeviceRepresentation, deviceIndex: number) => (
          <>
          {device.sessions!.map((session: SessionRepresentation, sessionIndex: number) => (
            <Fragment key={'device-' + deviceIndex + '-session-' + sessionIndex}>
            <div className="md:flex md:items-center">
              <div className="p-4 pr-0 pb-0">
                <ComputerIcon className="fill-p2gray-800" />
              </div>
              <div className="text-sm font-semibold text-p2gray-900 p-4 md:pl-2 pb-0">
                {device.os} / {device.browser} / {device.osVersion}
              </div>
            </div>
            <div className="md:grid md:grid-cols-5 border-b border-gray-300">
            <div className="space-y-1 p-4">
                  <div className="text-xs font-medium leading-3 text-p2gray-800">
                    {session.ipAddress}
                  </div>
                  <div className="text-xs leading-3 text-p2gray-800/50">
                    IP address
                  </div>
                </div>
                <div className="space-y-1 p-4">
                  <div className="text-xs font-medium leading-3 text-p2gray-800">
                   {time(session.lastAccess)}
                  </div>
                  <div className="text-xs leading-3 text-p2gray-800/50">
                    Last accessed
                  </div>
                </div>
                <div className="space-y-1 p-4">
                  <div className="text-xs font-medium leading-3 text-p2gray-800">
                    193.248.139.5
                  </div>
                  <div className="text-xs leading-3 text-p2gray-800/50">
                    Clients
                  </div>
                </div>
                <div className="space-y-1 p-4">
                  <div className="text-xs font-medium leading-3 text-p2gray-800">
                  {time(session.started)}
                  </div>
                  <div className="text-xs leading-3 text-p2gray-800/50">
                    Started
                  </div>
                </div>
                <div className="space-y-1 p-4">
                  <div className="text-xs font-medium leading-3 text-p2gray-800">
                    {time(session.expires)}
                  </div>
                  <div className="text-xs leading-3 text-p2gray-800/50">
                    Expires
                  </div>
                </div>
            </div>
            </Fragment>
          ))}
          </>
        ))}
      </div>
    </div>
  );
};

export default ActivityProfile;
