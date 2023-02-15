type Props = {
  value: string;
  label: string;
  percent?: number;
};

const ProgressBar = ({ percent = 20 }) => {
  const radius = 10;
  const circumference = radius * 2 * Math.PI;
  return (
    <svg className="h-6 w-6">
      <circle
        className="text-gray-300"
        strokeWidth="3"
        stroke="currentColor"
        fill="transparent"
        r={radius}
        cx="12"
        cy="12"
      />
      <circle
        className=" text-p2blue-700"
        strokeWidth="3"
        strokeDasharray={circumference}
        strokeDashoffset={circumference - (percent / 100) * circumference}
        strokeLinecap="round"
        stroke="currentColor"
        fill="transparent"
        r={radius}
        cx="12"
        cy="12"
      />
    </svg>
  );
};

const Stat: React.FC<Props> = ({ percent, value, label }) => {
  return (
    <div className="flex flex-row">
      {percent && (
        <div className="place-content-end pr-2 pt-1">
          <ProgressBar percent={50} />
        </div>
      )}
      <div>
        <div className="font-semibold">{value}</div>
        <div className="text-xs font-semibold text-gray-500">{label}</div>
      </div>
    </div>
  );
};

export default Stat;
