const buttonClasses =
  "rounded bg-indigo-50 py-1 px-2 text-xs font-semibold text-primary-700 shadow-sm enabled:hover:bg-indigo-100 disabled:opacity-50 lowercase";

export const Button = ({
  onClick,
  disabled,
  text,
}: {
  onClick: (args: any) => void;
  disabled: boolean;
  text: string;
}) => {
  return (
    <button className={buttonClasses} onClick={onClick} disabled={disabled}>
      {text}
    </button>
  );
};
