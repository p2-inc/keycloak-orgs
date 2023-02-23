import { Fragment } from "react";
import { ComputerIcon } from "components/icons/computer";
import SectionHeader from "components/navs/section-header";
import ActivityLoader from "components/loaders/activity";
import ActivityItem from "components/elements/activity/item";
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
                        <ComputerIcon className="fill-p2gray-800" />
                      </div>
                      <div className="p-4 pb-0 text-sm font-semibold text-p2gray-900 md:pl-2">
                        {device.os} / {device.browser} / {device.osVersion}
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
                          193.248.139.5
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
