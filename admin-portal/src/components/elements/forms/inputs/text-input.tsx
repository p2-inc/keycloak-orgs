export const BasicFormClasses =
  "block rounded border-gray-300 dark:border-zinc-600 dark:text-zinc-200 sm:text-sm bg-white dark:bg-p2dark-1000 disabled:opacity-50 disabled:cursor-not-allowed hover:border-gray-500 transition";

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
