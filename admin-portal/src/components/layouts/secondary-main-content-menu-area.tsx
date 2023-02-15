const SecondaryMainContentMenuArea: React.FC<{
  children: React.ReactElement | React.ReactElement[];
}> = ({ children }) => {
  return (
    <aside className="order-first block flex-shrink-0">
      <div className="relative flex md:h-full md:w-72 flex-col overflow-y-auto bg-white px-2 md:px-7">
        {children}
      </div>
    </aside>
  );
};

export default SecondaryMainContentMenuArea;
