const PrimaryContentArea: React.FC<{
  children: React.ReactElement | React.ReactElement[];
}> = ({ children }) => {
  return (
    <div className="flex h-full min-w-0 flex-1 flex-col overflow-y-auto px-4 py-4 md:px-10 md:py-6">
      {children}
    </div>
  );
};

export default PrimaryContentArea;
