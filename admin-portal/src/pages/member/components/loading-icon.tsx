import { User } from "lucide-react";

export const LoadingIcon = (
  <div>
    <div className="relative h-12 w-12 overflow-hidden rounded-md">
      <div className="absolute -inset-10 z-10 bg-primary-gradient"></div>
      <div className="absolute inset-[2px] z-20 flex items-center justify-center rounded bg-white dark:bg-p2dark-1000 dark:text-zinc-200">
        <User />
      </div>
    </div>
  </div>
);
