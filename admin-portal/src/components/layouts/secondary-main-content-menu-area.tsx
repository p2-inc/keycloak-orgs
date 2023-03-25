const SecondaryMainContentMenuArea: React.FC<{
  children: React.ReactElement | React.ReactElement[];
}> = ({ children }) => {
  return (
    <aside className="order-first block flex-shrink-0">
      <div className="relative flex flex-col overflow-y-auto px-2 md:h-full md:w-72 md:px-7">
        {children}
      </div>
    </aside>
  );
};

export default SecondaryMainContentMenuArea;
