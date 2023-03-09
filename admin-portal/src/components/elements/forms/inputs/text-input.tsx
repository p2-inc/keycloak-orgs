export const BasicFormClasses =
  "block rounded border-gray-300 focus:border-gray-400 focus:ring-gray-400 sm:text-sm bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed";

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
