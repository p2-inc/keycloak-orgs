import MainContentArea from "components/layouts/main-content-area";
import SecondaryMainContentMenu from "components/navs/secondary-main-content-menu";
import FormTextInputWithLabel from "components/elements/forms/inputs/text-input-with-label";
import TopHeader from "components/navs/top-header";
import cs from "classnames";

import { KeyIcon, SquaresPlusIcon, UserCircleIcon, DevicePhoneMobileIcon } from '@heroicons/react/24/outline'

const navigation = [
  { name: 'General', href: '/profile', icon: UserCircleIcon, current: true },
  { name: 'Signing in', href: '/profile/signin', icon: KeyIcon, current: false },
  { name: 'Device activity', href: '/profile/activity', icon: DevicePhoneMobileIcon, current: false },
  { name: 'Linked accounts', href: '/profile/linked', icon: SquaresPlusIcon, current: false },
]

export default function Profile() {
  return (
    <>
      <TopHeader header="Profile" />
      <MainContentArea>
        {/* Secondary menu */}
        <SecondaryMainContentMenu>
          <nav className="space-y-1">
          {navigation.map((item) => (
            <a
              key={item.name}
              href={item.href}
              className={cs(
                item.current
                  ? 'bg-gray-50 text-indigo-700 hover:text-indigo-700 hover:bg-white'
                  : 'text-gray-900 hover:text-gray-900 hover:bg-gray-50',
                'group rounded-md px-3 py-2 flex items-center text-sm font-medium'
              )}
              aria-current={item.current ? 'page' : undefined}
            >
              <item.icon
                className={cs(
                  item.current
                    ? 'text-indigo-500 group-hover:text-indigo-500'
                    : 'text-gray-400 group-hover:text-gray-500',
                  'flex-shrink-0 -ml-1 mr-3 h-6 w-6'
                )}
                aria-hidden="true"
              />
              <span className="truncate">{item.name}</span>
            </a>
          ))}
        </nav>


        </SecondaryMainContentMenu>

        {/* Primary content */}
        <section
          aria-labelledby="primary-heading"
          className="flex h-full min-w-0 flex-1 flex-col overflow-y-auto px-4"
        >
          <FormTextInputWithLabel slug="email" label="Email"></FormTextInputWithLabel>  
          <FormTextInputWithLabel slug="firstName" label="First Name"></FormTextInputWithLabel>  
          <FormTextInputWithLabel slug="lastName" label="Last Name"></FormTextInputWithLabel>  

        </section>
      </MainContentArea>
    </>
  );
}