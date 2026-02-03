export const BasicFormClasses =
  "block rounded-md border-gray-300 border-gray-300 bg-white transition hover:border-gray-500 focus:border-transparent focus:ring-primary-700 disabled:cursor-not-allowed disabled:opacity-50 dark:border-zinc-600 dark:bg-p2dark-1000 dark:text-zinc-200 sm:text-sm";

const FormTextInput = () => {
  return (
    <input
      type="text"
      name="text"
      id="text"
      className={BasicFormClasses}
      placeholder="placeholder"
    />
  );
};

export default FormTextInput;
