const SecondaryMainContentMenu: React.FC<{
  children: React.ReactElement | React.ReactElement[];
}> = ({ children }) => {
  return (
    <aside className="order-first block flex-shrink-0">
      <div className="relative flex h-full w-72 flex-col overflow-y-auto border-r border-gray-200 bg-white px-4">
        {children}
      </div>
    </aside>
  );
};

export default SecondaryMainContentMenu;
