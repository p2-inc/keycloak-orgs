const people = [
  {
    name: "Garth Patil",
    title: "Grand Master",
    email: "gpatil@phasetwo.com",
    role: "Master",
  },
  {
    name: "Garth Patil",
    title: "Grand Master",
    email: "gpatil@phasetwo.com",
    role: "Master",
  },
  {
    name: "Garth Patil",
    title: "Grand Master",
    email: "gpatil@phasetwo.com",
    role: "Master",
  },
  {
    name: "Garth Patil",
    title: "Grand Master",
    email: "gpatil@phasetwo.com",
    role: "Master",
  },
  {
    name: "Garth Patil",
    title: "Grand Master",
    email: "gpatil@phasetwo.com",
    role: "Master",
  },
];

const Table = () => {
  return (
    <div className="overflow-hidden rounded-md">
      <table className="min-w-full divide-y divide-gray-200  border border-gray-200 bg-gray-50 text-sm font-medium text-black">
        <thead>
          <tr>
            <th
              scope="col"
              className="py-3.5 pl-4 pr-3 text-left font-medium text-black sm:pl-6"
            >
              Name
            </th>
            <th
              scope="col"
              className="px-3 py-3.5 text-left font-medium text-black"
            >
              Title
            </th>
            <th
              scope="col"
              className="px-3 py-3.5 text-left font-medium text-black"
            >
              Email
            </th>
            <th
              scope="col"
              className="px-3 py-3.5 text-left font-medium text-black"
            >
              Role
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200">
          {people.map((person) => (
            <tr key={person.email}>
              <td className="whitespace-nowrap py-4 pl-4 pr-3 font-medium text-black sm:pl-6">
                {person.name}
              </td>
              <td className="whitespace-nowrap px-3 py-4 ">{person.title}</td>
              <td className="whitespace-nowrap px-3 py-4 ">{person.email}</td>
              <td className="whitespace-nowrap px-3 py-4 ">{person.role}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default Table;
