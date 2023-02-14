import { ComputerIcon } from "components/icons/computer";
import SectionHeader from "components/navs/section-header";

const ActivityProfile = () => {
  return (
    <div>
      <div className="mb-12">
        <SectionHeader
          title="Device activity"
          description="One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin."
        />
      </div>
      <div className="w-full rounded border border-gray-200 bg-gray-50">
        {[...Array(10)].map((e, i) => (
          <>
            <div className="md:flex md:items-center">
              <div className="p-4 pr-0 pb-0">
                <ComputerIcon className="fill-p2gray-800" />
              </div>
              <div className="text-sm font-semibold text-p2gray-900 p-4 md:pl-2 pb-0">
                Linux / Chrome / 107.0.0
              </div>
            </div>
            <div className="md:grid md:grid-cols-5 border-b border-gray-300">
              {[...Array(5)].map((d, j) => (
                <div className="space-y-1 p-4">
                  <div className="text-xs font-medium leading-3 text-p2gray-800">
                    193.248.139.5
                  </div>
                  <div className="text-xs leading-3 text-p2gray-800/50">
                    IP address
                  </div>
                </div>
              ))}
            </div>
          </>
        ))}
      </div>
    </div>
  );
};

export default ActivityProfile;
