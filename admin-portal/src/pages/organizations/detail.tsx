import Button from "components/elements/forms/buttons/button";
import MainContentArea from "components/layouts/main-content-area";
import TopHeader from "components/navs/top-header";

export default function OrganizationDetail() {
  return (
    <>
      <TopHeader
        header="Organization Detail"
        rightAreaItems={
          <>
            <Button>Settings</Button>
          </>
        }
      />
      <MainContentArea>
        {/* Primary content */}
        <section
          aria-labelledby="primary-heading"
          className="flex h-full min-w-0 flex-1 flex-col overflow-y-auto px-4"
        >
          Main Content
        </section>
      </MainContentArea>
    </>
  );
}
