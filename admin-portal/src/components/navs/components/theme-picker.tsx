import { Listbox } from "@headlessui/react";
import { Theme as ThemeType, Themes } from "@/components/utils/useTheme";
import { useTranslation } from "react-i18next";

export default function ThemePicker({
  theme,
  changeTheme,
}: {
  theme: ThemeType;
  changeTheme: (theme: ThemeType) => void;
}) {
  const { t } = useTranslation();
  const currentTheme = Themes.find((t) => t.key === theme)!;

  return (
    <>
      <div className="text-sm dark:text-zinc-200">{t("theme")}</div>
      <Listbox
        value={currentTheme}
        onChange={(themeChoice) => changeTheme(themeChoice.key)}
      >
        <Listbox.Button className="flex items-center space-x-2 rounded border px-2 py-1 text-sm hover:border-gray-500 dark:border-zinc-600 dark:text-zinc-200 dark:hover:border-zinc-400">
          <div>{currentTheme.icon}</div>
          <div>{t(currentTheme.name)}</div>
        </Listbox.Button>
        <Listbox.Options className="absolute bottom-0 right-0 rounded border bg-white shadow-md dark:border-zinc-600 dark:bg-p2dark-900">
          {Themes.map((item) => (
            <Listbox.Option
              key={item.key}
              value={item}
              className="flex cursor-pointer items-center space-x-2 px-2 py-1 text-sm hover:bg-gray-100 dark:text-zinc-200 dark:hover:bg-zinc-600"
            >
              <div>{item.icon}</div>
              <div>{t(item.name)}</div>
            </Listbox.Option>
          ))}
        </Listbox.Options>
      </Listbox>
    </>
  );
}
